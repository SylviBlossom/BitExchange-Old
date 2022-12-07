package moe.sylvi.bitexchange.bit;

import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface BitResourceKey<R, I extends BitInfo<R>> {
    static <R, I extends BitInfo<R>> BitResourceKey<R, I> of(BitRegistry<R, I> registry, R resource) {
        return new BitResourceKeyImpl<>(registry, resource);
    }

    BitRegistry<R, I> getRegistry();

    R getResource();

    default Registry<R> getResourceRegistry() {
        return getRegistry().getResourceRegistry();
    }

    default Identifier getResourceId() {
        return getResourceRegistry().getId(getResource());
    }
}
