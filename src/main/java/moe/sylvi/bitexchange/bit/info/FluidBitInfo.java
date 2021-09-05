package moe.sylvi.bitexchange.bit.info;

import net.minecraft.fluid.Fluid;

public class FluidBitInfo implements BitInfo<Fluid> {
    private Fluid fluid;
    private double value;

    public FluidBitInfo(Fluid fluid, double value) {
        this.fluid = fluid;
        this.value = value;
    }

    @Override
    public Fluid getResource() {
        return fluid;
    }

    @Override
    public void setResource(Fluid resource) {
        fluid = resource;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public FluidBitInfo copy() {
        return new FluidBitInfo(fluid, value);
    }
}
