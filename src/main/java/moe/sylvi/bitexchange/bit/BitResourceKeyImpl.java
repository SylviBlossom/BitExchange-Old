package moe.sylvi.bitexchange.bit;

import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;

public record BitResourceKeyImpl<R, I extends BitInfo<R>>
        (BitRegistry<R, I> registry, R resource) implements BitResourceKey<R, I> {

    @Override
    public BitRegistry<R, I> getRegistry() {
        return registry;
    }

    @Override
    public R getResource() {
        return resource;
    }
}
