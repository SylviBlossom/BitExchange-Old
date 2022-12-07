package moe.sylvi.bitexchange.bit.info;

import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public interface BitInfo<T> {
    static ItemBitInfo ofItem(Item item, double value, long research, boolean researchable, boolean automatable, List<ResearchRequirement> requirements) {
        return new ItemBitInfo(item, value, research, researchable, automatable, requirements);
    }
    static ItemBitInfo ofItem(Item item, double value, long research, boolean researchable, boolean automatable) {
        return ofItem(item, value, research, researchable, automatable, new ArrayList<>());
    }

    static FluidBitInfo ofFluid(Fluid fluid, double value, long research, long ratio, boolean researchable, List<ResearchRequirement> requirements) {
        return new FluidBitInfo(fluid, value, research, ratio, researchable, requirements);
    }
    static FluidBitInfo ofFluid(Fluid fluid, double value, long research, long ratio, boolean researchable) {
        return ofFluid(fluid, value, research, ratio, researchable, new ArrayList<>());
    }

    T getResource();

    double getValue();

    default double getValue(double amount) {
        return getValue() * (amount / getRatio());
    }

    default long getRatio() {
        return 1;
    }

    Text getDisplayName();

    <I extends BitInfo<T>> I withResource(T resource);

    default <I extends BitInfo<T>> I copy() {
        return withResource(getResource());
    }
}
