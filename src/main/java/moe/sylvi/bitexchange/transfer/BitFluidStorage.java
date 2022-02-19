package moe.sylvi.bitexchange.transfer;

import moe.sylvi.bitexchange.BitRegistries;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;

public class BitFluidStorage extends SingleVariantStorage<FluidVariant> {
    private final long capacity;
    private final boolean canInsert;
    private final boolean canExtract;

    public BitFluidStorage() {
        this(Long.MAX_VALUE, true, false);
    }

    public BitFluidStorage(long capacity, boolean canInsert, boolean canExtract) {
        this.capacity = capacity;
        this.canInsert = canInsert;
        this.canExtract = canExtract;
    }

    @Override
    protected FluidVariant getBlankVariant() {
        return FluidVariant.blank();
    }

    @Override
    protected long getCapacity(FluidVariant variant) {
        return this.capacity;
    }

    @Override
    protected boolean canInsert(FluidVariant variant) {
        if (canInsert && !variant.isBlank()) {
            return BitRegistries.FLUID.get(variant.getFluid()) != null;
        }
        return false;
    }

    @Override
    protected boolean canExtract(FluidVariant variant) {
        return canExtract;
    }
}
