package moe.sylvi.bitexchange.bit.registry.builder;

import com.google.common.collect.Lists;
import com.google.gson.*;
import moe.sylvi.bitexchange.BitExchange;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.BitHelper;
import moe.sylvi.bitexchange.bit.BitResource;
import moe.sylvi.bitexchange.bit.Recursable;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tag.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class AbstractDataRegistryBuilder<R, I extends BitInfo<R>> implements BitRegistryBuilder<R, I> {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final HashMap<BitRegistry<?,?>, List<JsonObject>> JSON_OBJECTS = new HashMap<>();
    private static final HashMap<JsonObject, Identifier> JSON_IDS = new HashMap<>();
    protected final HashMap<R, JsonObject> processing = new HashMap<>();
    protected final HashMap<R, List<JsonObject>> modifying = new HashMap<>();
    protected final HashMap<JsonObject, Double> calculated = new HashMap<>();

    protected final int priority;
    protected final BitRegistry<R, I> registry;

    public AbstractDataRegistryBuilder(BitRegistry<R, I> registry, int priority) {
        this.registry = registry;
        this.priority = priority;
    }
    public AbstractDataRegistryBuilder(BitRegistry<R, I> registry) {
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
                boolean modify = JsonHelper.getBoolean(json, "modify", false);
                if (json.has("id")) {
                    String id = JsonHelper.getString(json, "id");
                    registerID(id, json, override, modify);
                } else if (json.has("ids")) {
                    for (JsonElement elem : JsonHelper.getArray(json, "ids")) {
                        registerID(elem.getAsString(), json, override, modify);
                    }
                }
            } catch (Exception e) {
                BitExchange.warn("Error occured while preparing resource " + JSON_IDS.get(json).toString(), e);
            }
        }
    }

    @Override
    public I process(R resource) {
        I result = null;
        if (processing.containsKey(resource)) {
            JsonObject json = processing.get(resource);
            try {
                if (json.has("copy")) {
                    result = parseCopyResource(resource, json);
                } else {
                    result = parseJson(resource, json);
                }
            } catch (Throwable throwable) {
                BitExchange.warn("Error occured while processing resource " + JSON_IDS.get(json).toString(), throwable);
                return null;
            }
        }
        if (modifying.containsKey(resource)) {
            for (JsonObject json : modifying.get(resource)) {
                try {
                    result = parseModifyResource(resource, json, result);
                } catch (Throwable throwable) {
                    BitExchange.warn("Error occured while modifying resource " + JSON_IDS.get(json).toString(), throwable);
                }
            }
        }
        return result;
    }

    @Override
    public void postProcess() {
        processing.clear();
        modifying.clear();
        calculated.clear();
    }

    protected void registerID(String id, JsonObject json, boolean override, boolean modify) {
        Registry<R> resourceRegistry = registry.getResourceRegistry();
        if (id.startsWith("#")) {
            id = id.substring(1);
            TagKey<R> tag = TagKey.of(resourceRegistry.getKey(), new Identifier(id));
            for (var entry : resourceRegistry.iterateEntries(tag)) {
                if (modify) {
                    modifying.computeIfAbsent(entry.value(), k -> new ArrayList<>()).add(json);
                    registry.prepareResource(entry.value(), this);
                } else if (override || !processing.containsKey(entry.value())) {
                    processing.put(entry.value(), json);
                    registry.prepareResource(entry.value(), this);
                }
            }
        } else {
            R resource = resourceRegistry.get(new Identifier(id));
            if (resource != null && (override || !processing.containsKey(resource))) {
                processing.put(resource, json);
                registry.prepareResource(resource, this);
            }
        }
    }

    abstract I parseJson(R resource, JsonObject json) throws Throwable;

    abstract I modifyResource(R resource, I info, JsonObject json) throws Throwable;

    protected double parseBitValue(R resource, JsonObject json) throws Throwable {
        if (calculated.containsKey(json)) {
            return calculated.get(json);
        }
        double value = JsonHelper.getDouble(json, "value", 0);
        if (json.has("value_ref")) {
            String addStr = JsonHelper.getString(json, "value_ref");
            String[] ids = addStr.split(",");
            for (String id : ids) {
                BitResource parsed = BitHelper.parseResourceId(id, registry);

                Recursable<BitInfo> result = parsed.getRegistry().getOrProcess(parsed.getResource());
                if (result.isRecursive()) {
                    throw new Exception("Found circular reference for " + id + "");
                }

                if (result.get() != null) {
                    value += result.get().getValue() * parsed.getAmount();
                }
            }
        }
        calculated.put(json, value);
        return value;
    }

    protected double modifyBitValue(I info, JsonObject json) throws Throwable {
        boolean hasValue = json.has("value");
        double value = JsonHelper.getDouble(json, "value", info.getValue());
        if (json.has("value_ref")) {
            if (!hasValue) {
                value = 0;
            }
            String addStr = JsonHelper.getString(json, "value_ref");
            String[] ids = addStr.split(",");
            for (String id : ids) {
                BitResource parsed = BitHelper.parseResourceId(id, registry);

                Recursable<BitInfo> result = parsed.getRegistry().getOrProcess(parsed.getResource());
                if (result.isRecursive()) {
                    throw new Exception("Found circular reference for " + id + "");
                }

                if (result.get() != null) {
                    value += result.get().getValue(parsed.getAmount());
                }
            }
        }
        return value;
    }

    protected I parseCopyResource(R resource, JsonObject json) throws Throwable {
        String id = JsonHelper.getString(json, "copy");
        BitResource<R, I> parsed = parseTypedResourceId(id);

        Recursable<I> result = parsed.getRegistry().getOrProcess(parsed.getResource());

        if (result.isRecursive()) {
            throw new Exception("Found circular reference for " + id + "");
        }
        if (result.get() == null) {
            throw new Exception("Copy failed, could not find " + id + "");
        }

        return modifyResource(resource, result.get().withResource(resource), json);
    }

    protected I parseModifyResource(R resource, JsonObject json, @Nullable I overrideSource) throws Throwable {
        if (overrideSource == null) {
            Recursable<I> source = registry.getOrProcess(resource, true);

            if (source.isRecursive()) {
                throw new Exception("Circular 'modify' call");
            }

            return modifyResource(resource, source.get(), json);
        } else {
            return modifyResource(resource, overrideSource, json);
        }
    }

    protected BitResource<R, I> parseTypedResourceId(String id) throws Throwable {
        long count = 1;

        int multIndex = id.indexOf("*");
        if (multIndex >= 0) {
            count = Integer.parseInt(id.substring(multIndex + 1));
            id = id.substring(0, multIndex);
        }

        String finalId = id;
        R resourceRef = registry.getResourceRegistry().getOrEmpty(new Identifier(id)).orElseThrow(() -> new JsonSyntaxException("Invalid or unsupported id '" + finalId + "'"));

        return BitResource.of(registry, resourceRef, count);
    }

    public static void loadResources(ResourceManager manager) {
        JSON_OBJECTS.clear();
        JSON_IDS.clear();
        manager.findResources("bit_registry", identifier -> identifier.getPath().endsWith(".json")).forEach((id, resource) -> {
            try {
                InputStream stream = resource.getInputStream();

                Reader reader = new BufferedReader(new InputStreamReader(stream));
                JsonObject root = JsonHelper.deserialize(GSON, reader, JsonObject.class);

                String registryType = JsonHelper.getString(root, "type");
                BitRegistry<?,?> registry = BitRegistries.REGISTRY.get(new Identifier(registryType));
                List<JsonObject> objectList = JSON_OBJECTS.computeIfAbsent(registry, (j) -> Lists.newArrayList());

                boolean defaultOverride = JsonHelper.getBoolean(root, "override", true);
                boolean defaultModify = JsonHelper.getBoolean(root, "modify", false);

                JsonArray array = root.getAsJsonArray("values");
                for (JsonElement elem : array) {
                    JsonObject object = elem.getAsJsonObject();
                    if (!object.has("id") && !object.has("ids")) {
                        throw new JsonSyntaxException("Json must have an 'id' or 'ids' field");
                    }
                    if (!object.has("override")) {
                        object.addProperty("override", defaultOverride);
                    }
                    if (!object.has("modify")) {
                        object.addProperty("modify", defaultModify);
                    }
                    objectList.add(object);
                    JSON_IDS.put(object, id);
                }
            } catch (Exception e) {
                BitExchange.warn("Error occured while loading resource " + id.toString(), e);
            }
        });
    }
}
