package moe.sylvi.bitexchange.bit.registry.builder;

import com.google.common.collect.Lists;
import com.google.gson.*;
import moe.sylvi.bitexchange.BitExchange;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.Recursable;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.info.BitInfoResearchable;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import moe.sylvi.bitexchange.bit.research.CombinedResearchRequirement;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;

public abstract class DataRegistryBuilder<R, I extends BitInfo<R>> implements BitRegistryBuilder<R, I> {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final HashMap<BitRegistry<?,?>, List<JsonObject>> JSON_OBJECTS = new HashMap<>();
    private static final HashMap<JsonObject, Identifier> JSON_IDS = new HashMap<>();
    protected final HashMap<R, JsonObject> processing = new HashMap<>();
    protected final HashMap<JsonObject, Double> calculated = new HashMap<>();

    protected final int priority;
    protected final BitRegistry<R, I> registry;

    public DataRegistryBuilder(BitRegistry<R, I> registry, int priority) {
        this.registry = registry;
        this.priority = priority;
    }
    public DataRegistryBuilder(BitRegistry<R, I> registry) {
        this(registry, ItemPriorities.DATA);
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void prepare(MinecraftServer server) {
        if (!JSON_OBJECTS.containsKey(registry)) {
            return;
        }
        for (JsonObject json : JSON_OBJECTS.get(registry)) {
            try {
                boolean override = JsonHelper.getBoolean(json, "override", true);
                if (json.has("id")) {
                    String id = JsonHelper.getString(json, "id");
                    registerID(id, json, override);
                } else if (json.has("ids")) {
                    for (JsonElement elem : JsonHelper.getArray(json, "ids")) {
                        registerID(elem.getAsString(), json, override);
                    }
                }
            } catch (Exception e) {
                BitExchange.error("Error occured while preparing resource " + JSON_IDS.get(json).toString(), e);
            }
        }
    }

    @Override
    public I process(R resource) {
        if (!processing.containsKey(resource)) {
            return null;
        }
        JsonObject json = processing.get(resource);
        try {
            if (json.has("copy")) {
                return parseCopyResource(resource, json);
            } else {
                return parseJson(resource, json);
            }
        } catch (Throwable throwable) {
            BitExchange.error("Error occured while processing resource " + JSON_IDS.get(json).toString(), throwable);
            return null;
        }
    }

    @Override
    public void postProcess() {
        processing.clear();
        calculated.clear();
    }

    protected void registerID(String id, JsonObject json, boolean override) {
        Registry<R> resourceRegistry = registry.getResourceRegistry();
        if (id.startsWith("#")) {
            id = id.substring(1);
            Tag<R> tag = ServerTagManagerHolder.getTagManager().getTag(resourceRegistry.getKey(), new Identifier(id), t -> new JsonSyntaxException("Invalid or unsupported tag '" + t + "'"));
            for (R resource : tag.values()) {
                if (override || !processing.containsKey(resource)) {
                    processing.put(resource, json);
                    registry.prepareResource(resource, this);
                }
            }
        } else {
            String finalId = id;
            R resource = resourceRegistry.getOrEmpty(new Identifier(id)).orElseThrow(() -> new JsonSyntaxException("Invalid or unsupported id '" + finalId + "'"));
            if (override || !processing.containsKey(resource)) {
                processing.put(resource, json);
                registry.prepareResource(resource, this);
            }
        }
    }

    abstract I parseJson(R resource, JsonObject json) throws Throwable;

    abstract I copyResource(R resource, I source) throws Throwable;

    protected double parseJsonBits(R resource, JsonObject json) throws Throwable {
        if (calculated.containsKey(json)) {
            return calculated.get(json);
        }
        double value = JsonHelper.getDouble(json, "value", 0);
        if (json.has("value_ref")) {
            String addStr = JsonHelper.getString(json, "value_ref");
            String[] ids = addStr.split(",");
            for (String id : ids) {
                GenericBitResource parsed = parseResourceId(id);

                Recursable<BitInfo> result = parsed.registry.getOrProcess(parsed.resource);
                if (result.isRecursive()) {
                    throw new Exception("Found circular reference for " + id + "");
                }

                if (result.get() != null) {
                    value += result.get().getValue() * parsed.amount;
                }
            }
        }
        calculated.put(json, value);
        return value;
    }

    protected List<ResearchRequirement> parseResearchRequirements(JsonObject json) throws Throwable {
        List<ResearchRequirement> result = Lists.newArrayList();
        String field = json.has("required_research") ? "required_research" : (json.has("value_ref") ? "value_ref" : null);
        if (field != null) {
            String[] ids = JsonHelper.getString(json, field).split(",");
            for (String id : ids) {
                if (id.isEmpty()) {
                    continue;
                }
                List<GenericBitResource> parsed = parseMultiResourceId(id);

                List<ResearchRequirement> requirements = Lists.newArrayList();
                for (GenericBitResource resource : parsed) {
                    if (resource.registry.get(resource.resource) instanceof BitInfoResearchable researchable) {
                        ResearchRequirement requirement = researchable.createResearchRequirement();
                        if (!requirements.contains(requirement)) {
                            requirements.add(requirement);
                        }
                    }
                }

                if (!requirements.isEmpty()) {
                    result.add(CombinedResearchRequirement.of(requirements));
                }
            }
        }
        return result;
    }

    protected I parseCopyResource(R resource, JsonObject json) throws Throwable {
        String id = JsonHelper.getString(json, "copy");
        TypedBitResource<R, I> parsed = parseTypedResourceId(id);

        Recursable<I> result = parsed.registry.getOrProcess(parsed.resource);

        if (result.isRecursive()) {
            throw new Exception("Found circular reference for " + id + "");
        }
        if (result.get() == null) {
            throw new Exception("Copy failed, could not find " + id + "");
        }

        return copyResource(resource, result.get());
    }

    protected GenericBitResource parseResourceId(String id) throws Throwable {
        BitRegistry registryRef = registry;
        long count = 1;

        int registryIndex = id.indexOf("$");
        if (registryIndex >= 0) {
            registryRef = BitRegistries.REGISTRY.get(new Identifier(id.substring(0, registryIndex)));
            if (registryRef == null) {
                throw new Exception("Bit registry not found: " + id.substring(0, registryIndex));
            }
            id = id.substring(registryIndex + 1);
        }

        int multIndex = id.indexOf("*");
        if (multIndex >= 0) {
            count = Integer.parseInt(id.substring(multIndex + 1));
            id = id.substring(0, multIndex);
        }

        String finalId = id;
        Object resourceRef = registryRef.getResourceRegistry().getOrEmpty(new Identifier(id)).orElseThrow(() -> new JsonSyntaxException("Invalid or unsupported id '" + finalId + "'"));

        return new GenericBitResource(registryRef, resourceRef, count);
    }

    protected TypedBitResource<R, I> parseTypedResourceId(String id) throws Throwable {
        long count = 1;

        int multIndex = id.indexOf("*");
        if (multIndex >= 0) {
            count = Integer.parseInt(id.substring(multIndex + 1));
            id = id.substring(0, multIndex);
        }

        String finalId = id;
        Object resourceRef = registry.getResourceRegistry().getOrEmpty(new Identifier(id)).orElseThrow(() -> new JsonSyntaxException("Invalid or unsupported id '" + finalId + "'"));

        return new TypedBitResource(registry, resourceRef, count);
    }

    protected List<GenericBitResource> parseMultiResourceId(String fullId) throws Throwable {
        List<GenericBitResource> list = Lists.newArrayList();

        for (String id : fullId.split("\\|")) {
            BitRegistry registryRef = registry;
            long count = 1;

            int registryIndex = id.indexOf("$");
            if (registryIndex >= 0) {
                registryRef = BitRegistries.REGISTRY.get(new Identifier(id.substring(0, registryIndex)));
                if (registryRef == null) {
                    throw new Exception("Bit registry not found: " + id.substring(0, registryIndex));
                }
                id = id.substring(registryIndex + 1);
            }

            int multIndex = id.indexOf("*");
            if (multIndex >= 0) {
                count = Integer.parseInt(id.substring(multIndex + 1));
                id = id.substring(0, multIndex);
            }

            if (id.startsWith("#")) {
                Tag tag = ServerTagManagerHolder.getTagManager().getTag(registryRef.getResourceRegistry().getKey(), new Identifier(id), t -> new JsonSyntaxException("Invalid or unsupported tag '" + t + "'"));

                for (Object resourceRef : tag.values()) {
                    GenericBitResource resource = new GenericBitResource(registryRef, resourceRef, count);
                    if (!list.contains(resource)) {
                        list.add(resource);
                    }
                }
            } else {
                String finalId = id;
                Object resourceRef = registryRef.getResourceRegistry().getOrEmpty(new Identifier(id)).orElseThrow(() -> new JsonSyntaxException("Invalid or unsupported id '" + finalId + "'"));

                if (!list.contains(resourceRef)) {
                    list.add(new GenericBitResource(registryRef, resourceRef, count));
                }
            }
        }

        return list;
    }

    public static void loadResources(ResourceManager manager) {
        JSON_OBJECTS.clear();
        JSON_IDS.clear();
        for (Identifier id : manager.findResources("bit_registry", path -> path.endsWith(".json"))) {
            try (InputStream stream = manager.getResource(id).getInputStream()) {
                Reader reader = new BufferedReader(new InputStreamReader(stream));
                JsonObject root = JsonHelper.deserialize(GSON, reader, JsonObject.class);

                String registryType = JsonHelper.getString(root, "type");
                BitRegistry<?,?> registry = BitRegistries.REGISTRY.get(new Identifier(registryType));
                List<JsonObject> objectList = JSON_OBJECTS.computeIfAbsent(registry, (j) -> Lists.newArrayList());

                JsonArray array = root.getAsJsonArray("values");
                for (JsonElement elem : array) {
                    JsonObject object = elem.getAsJsonObject();
                    if (!object.has("id") && !object.has("ids")) {
                        throw new JsonSyntaxException("Json must have an 'id' or 'ids' field");
                    }
                    objectList.add(object);
                    JSON_IDS.put(object, id);
                }
            } catch (Exception e) {
                BitExchange.error("Error occured while loading resource " + id.toString(), e);
            }
        }
    }

    public record GenericBitResource(BitRegistry registry, Object resource, long amount) {}

    public record TypedBitResource<R, I extends BitInfo<R>>(BitRegistry<R, I> registry, R resource, long amount) {}
}
