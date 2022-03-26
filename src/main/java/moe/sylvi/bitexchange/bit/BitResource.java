package moe.sylvi.bitexchange.bit;

import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;

import java.util.Objects;

public interface BitResource<R, I extends BitInfo<R>> {
    static <R, I extends BitInfo<R>> BitResource<R, I> of(BitRegistry<R, I> registry, R resource, double amount) {
        return new BitResourceImpl<>(registry, resource, amount);
    }

    BitRegistry<R, I> getRegistry();

    R getResource();

    double getAmount();

    default I getInfo() {
        return getRegistry().get(getResource());
    }

    default Recursable<I> getOrProcessInfo() {
        return getRegistry().getOrProcess(getResource());
    }

    default double getValue() {
        return getRegistry().getValue(getResource(), getAmount());
    }

    default Recursable<Double> getOrProcessValue() {
        var result = getRegistry().getOrProcess(getResource());
        if (result.notNullOrRecursive()) {
            return Recursable.of(result.get().getValue(getAmount()), false);
        } else {
            return result.into(0.0);
        }
    }
}