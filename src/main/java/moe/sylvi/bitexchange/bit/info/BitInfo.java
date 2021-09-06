package moe.sylvi.bitexchange.bit.info;

import com.google.common.collect.Lists;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;

public interface BitInfo<T> {
    static ItemBitInfo ofItem(Item item, double value, long research, boolean automatable, List<ResearchRequirement> requirements) {
        return new ItemBitInfo(item, value, research, automatable, requirements);
    }
    static ItemBitInfo ofItem(Item item, double value, long research, boolean automatable) {
        return ofItem(item, value, research, automatable, new ArrayList<>());
    }

    static FluidBitInfo ofFluid(Fluid fluid, double value, long research, List<ResearchRequirement> requirements) {
        return new FluidBitInfo(fluid, value, research, requirements);
    }
    static FluidBitInfo ofFluid(Fluid fluid, double value, long research) {
        return ofFluid(fluid, value, research, new ArrayList<>());
    }

    T getResource();

    double getValue();

    <I extends BitInfo<T>> I copy();
}
