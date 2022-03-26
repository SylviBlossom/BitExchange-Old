package moe.sylvi.bitexchange.bit.registry;

import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.Recursable;
import moe.sylvi.bitexchange.bit.registry.builder.BitRegistryBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.Registry;

import java.util.List;

public interface BitRegistry<R,I extends BitInfo<R>> extends Iterable<I> {
    static <T,O extends BitInfo<T>> BitRegistry<T,O> of(Registry<T> resourceRegistry, O defaultInfo) {
        return new SimpleBitRegistry<>(resourceRegistry, defaultInfo);
    }

    void registerBuilder(BitRegistryBuilder<R,I> builder);
    void prepareResource(R resource, BitRegistryBuilder<R,I> builder);

    Registry<R> getResourceRegistry();
    I getEmpty();

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
        I info = get(resource);
        return info != null ? info.getValue() : 0;
    }
    default double getValue(R resource, double amount) {
        I info = get(resource);
        return info != null ? info.getValue(amount) : 0;
    }

    List<I> getList();
}
