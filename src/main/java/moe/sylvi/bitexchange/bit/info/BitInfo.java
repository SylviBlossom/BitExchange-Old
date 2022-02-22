package moe.sylvi.bitexchange.bit.info;

import com.google.common.collect.Lists;
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

    static FluidBitInfo ofFluid(Fluid fluid, double value, long research, boolean researchable, List<ResearchRequirement> requirements) {
        return new FluidBitInfo(fluid, value, research, researchable, requirements);
    }
    static FluidBitInfo ofFluid(Fluid fluid, double value, long research, boolean researchable) {
        return ofFluid(fluid, value, research, researchable, new ArrayList<>());
    }

    T getResource();

    double getValue();

    Text getDisplayName();

    <I extends BitInfo<T>> I copy();
}
