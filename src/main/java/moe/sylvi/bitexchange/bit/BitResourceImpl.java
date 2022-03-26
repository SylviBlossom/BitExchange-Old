package moe.sylvi.bitexchange.bit;

import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;

import java.util.Objects;

public record BitResourceImpl<R, I extends BitInfo<R>>(BitRegistry<R, I> registry, R resource, double amount) implements BitResource<R, I> {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BitResourceImpl<?, ?> that = (BitResourceImpl<?, ?>) o;
        return amount == that.amount && Objects.equals(registry, that.registry) && Objects.equals(resource, that.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registry, resource, amount);
    }
}
