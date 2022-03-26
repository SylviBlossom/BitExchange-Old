package moe.sylvi.bitexchange.bit.registry;

import com.google.common.collect.Lists;
import moe.sylvi.bitexchange.BitExchange;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.registry.builder.BitRegistryBuilder;
import moe.sylvi.bitexchange.bit.Recursable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SimpleBitRegistry<R,I extends BitInfo<R>> implements BitRegistry<R,I> {
    private final Logger logger;
    private final Registry<R> resourceRegistry;
    private final I emptyInfo;

    private final HashMap<R, I> infoMap = new HashMap<>();
    private final List<I> infoList = Lists.newArrayList();

    private final List<BitRegistryBuilder<R,I>> builders = Lists.newArrayList();
    private final HashMap<R, List<BitRegistryBuilder<R,I>>> resourceProcessors = new HashMap<>();
    private final HashSet<RecursivePair<R,I>> recursiveCheck = new HashSet<>();

    private BitRegistryBuilder<R,I> currentProcessor;
    private boolean processing;

    public SimpleBitRegistry(Registry<R> resourceRegistry, I emptyInfo) {
        this.resourceRegistry = resourceRegistry;
        this.emptyInfo = emptyInfo;
        this.logger = LogManager.getLogger();
    }

    @Override
    public void registerBuilder(BitRegistryBuilder<R,I> builder) {
        builders.add(builder);
    }

    @Override
    public void prepareResource(R resource, BitRegistryBuilder<R,I> builder) {
        var list = resourceProcessors.computeIfAbsent(resource, i -> Lists.newArrayList());
        if (!list.contains(builder)) {
            list.add(builder);
        }
        list.sort(Comparator.comparing(BitRegistryBuilder::getPriority));
    }

    @Override
    public Registry<R> getResourceRegistry() {
        return resourceRegistry;
    }

    @Override
    public I getEmpty() {
        return emptyInfo;
    }

    @Override
    public void preBuild(MinecraftServer server) {
        logger.log(Level.INFO, "Preparing bit registry builders");
        processing = true;
        infoMap.clear();
        infoList.clear();
        for (BitRegistryBuilder<R,I> builder : builders) {
            try {
                builder.prepare(server);
            } catch (Exception e) {
                logger.error("Error in preparing " + builder.getClass().getSimpleName(), e);
            }
        }
    }

    @Override
    public void build() {
        logger.log(Level.INFO, "Building bit values");
        for (R resource : resourceProcessors.keySet()) {
            try {
                if (!infoMap.containsKey(resource)) {
                    process(resource, false);
                }
            } catch (Exception e) {
                String errorMessage = "Error in processing resource " + resource.toString();
                if (currentProcessor != null) {
                    errorMessage += " [" + currentProcessor.getClass().getSimpleName() + "]";
                    currentProcessor = null;
                }
                logger.error(errorMessage, e);
            }
            recursiveCheck.clear();
            recursiveCheck.clear();
        }
    }

    @Override
    public void postBuild() {
        for (BitRegistryBuilder<R,I> builder : builders) {
            try {
                builder.postProcess();
            } catch (Exception e) {
                logger.error("Error in post-processing " + builder.getClass().getSimpleName(), e);
            }
        }
        for (I info : infoMap.values()) {
            if (info.getValue() > 0) {
                infoList.add(info);
            }
        }
        processing = false;
        resourceProcessors.clear();
        logger.log(Level.INFO, "Built " + infoList.size() + " bit values");
    }

    private Recursable<I> process(R resource, boolean allowFallback) {
        if (resourceProcessors.containsKey(resource) && !resourceProcessors.get(resource).isEmpty()) {
            boolean recursedAll = true;
            for (BitRegistryBuilder<R,I> builder : resourceProcessors.get(resource)) {
                RecursivePair<R,I> recursivePair = new RecursivePair<>(resource, builder);
                if (recursiveCheck.contains(recursivePair)) {
                    if (!allowFallback) {
                        return Recursable.of(null, true);
                    } else {
                        continue;
                    }
                } else {
                    recursedAll = false;
                    recursiveCheck.add(recursivePair);
                }
                boolean topProcessor = currentProcessor == null;
                currentProcessor = topProcessor ? builder : currentProcessor;
                I result = builder.process(resource);
                currentProcessor = topProcessor ? null : currentProcessor;
                if (result != null) {
                    add(result);
                    recursiveCheck.remove(recursivePair);
                    return Recursable.of(result, false);
                }
                recursiveCheck.remove(recursivePair);
            }
            if (recursedAll) {
                return Recursable.of(null, true);
            }
        }
        return Recursable.of(null, false);
    }

    @Override
    public void add(I info) {
        if (!infoMap.containsKey(info.getResource())) {
            infoMap.put(info.getResource(), info);
        }
    }

    @Override
    public I get(R resource) {
        return infoMap.get(resource);
    }

    @Override
    public Recursable<I> getOrProcess(R resource, boolean allowFallback) {
        if (processing && !infoMap.containsKey(resource)) {
            return process(resource, allowFallback);
        } else {
            return Recursable.of(get(resource), false);
        }
    }

    @NotNull
    @Override
    public Iterator<I> iterator() {
        return infoList.iterator();
    }

    @Override
    public List<I> getList() { return infoList; }

    protected record RecursivePair<R,I extends BitInfo<R>>(R resource, BitRegistryBuilder<R, I> builder) {}
}
