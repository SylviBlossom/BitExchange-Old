package moe.sylvi.bitexchange.bit.registry;

import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.Recursable;
import moe.sylvi.bitexchange.bit.registry.builder.BitRegistryBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.Registry;

public interface BitRegistry<R,I extends BitInfo<R>> extends Iterable<I> {
    static <T,O extends BitInfo<T>> BitRegistry<T,O> of(Class<O> infoType, Registry<T> resourceRegistry) {
        return new SimpleBitRegistry<>(resourceRegistry);
    }

    void registerBuilder(BitRegistryBuilder<R,I> builder);
    void prepareResource(R resource, BitRegistryBuilder<R,I> builder);

    Registry<R> getResourceRegistry();

    void preBuild(MinecraftServer server);
    void build();
    void postBuild();

    void add(I info);

    I get(R resource);

    Recursable<I> getOrProcess(R resource, boolean allowFallback);
    default Recursable<I> getOrProcess(R resource) {
        return getOrProcess(resource, false);
    }

    default double getValue(R resource) {
        BitInfo<R> info = get(resource);
        return info != null ? info.getValue() : 0;
    }
}
