package moe.sylvi.bitexchange.bit.info;

import com.google.common.collect.Lists;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;

import java.util.List;

public interface BitInfo<T> {
    static ItemBitInfo ofItem(Item item, double value, long research, boolean automatable, List<ResearchRequirement> requirements) {
        return new ItemBitInfo(item, value, research, automatable, requirements);
    }
    static ItemBitInfo ofItem(Item item, double value, long research, boolean automatable) {
        return ofItem(item, value, research, automatable, Lists.newArrayList());
    }

    static FluidBitInfo ofFluid(Fluid fluid, double value) {
        return new FluidBitInfo(fluid, value);
    }

    T getResource();
    void setResource(T resource);

    double getValue();

    <I extends BitInfo<T>> I copy();

    default <I extends BitInfo<T>> I withResource(T resource) {
        I copied = copy();
        copied.setResource(resource);
        return copied;
    }
}
