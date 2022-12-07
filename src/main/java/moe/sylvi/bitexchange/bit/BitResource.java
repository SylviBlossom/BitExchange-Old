package moe.sylvi.bitexchange.bit;

import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface BitResource<R, I extends BitInfo<R>> {
    static <R, I extends BitInfo<R>> BitResource<R, I> of(BitRegistry<R, I> registry, R resource, double amount) {
        return new BitResourceImpl<>(registry, resource, amount);
    }
    static BitResource<Item, ItemBitInfo> fromStack(ItemStack stack) {
        return new BitResourceImpl<>(BitRegistries.ITEM, stack.getItem(), stack.getCount());
    }

    BitRegistry<R, I> getRegistry();

    R getResource();

    double getAmount();

    default long getDefaultRatio() {
        var resource = getResource();
        if (resource instanceof Fluid fluid) {
            return FluidConstants.BUCKET;
        } else {
            return 1;
        }
    }

    default long getRatio() { return getRatio(false); }
    default long getRatio(boolean process) {
        if (process) {
            var info = getOrProcessInfo();
            if (info.notNullOrRecursive()) {
                return info.get().getRatio();
            }
        } else {
            var info = getInfo();
            if (info != null) {
                return info.getRatio();
            }
        }
        return getDefaultRatio();
    }

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