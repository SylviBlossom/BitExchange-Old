package moe.sylvi.bitexchange.compat.modern_industrialization;

import moe.sylvi.bitexchange.bit.BitResource;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;

public record MIBitResource<R, I extends BitInfo<R>>(BitRegistry<R, I> registry, R resource, double amount, double probability) implements BitResource<R, I> {
    @Override
    public BitRegistry<R, I> getRegistry() {
        return registry;
    }

    @Override
    public R getResource() {
        return resource;
    }

    @Override
    public double getAmount() {
        return amount;
    }
}
