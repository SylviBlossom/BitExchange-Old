package moe.sylvi.bitexchange.bit.info;

import moe.sylvi.bitexchange.BitComponents;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.research.BitKnowledge;
import moe.sylvi.bitexchange.bit.research.FluidBitResearchRequirement;
import moe.sylvi.bitexchange.bit.research.ItemBitResearchRequirement;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;

import java.util.List;

public class FluidBitInfo implements BitInfoResearchable<Fluid> {
    protected final Fluid fluid;
    protected final double value;
    protected final long research;
    protected final List<ResearchRequirement> researchRequirements;

    public FluidBitInfo(Fluid fluid, double value, long research, List<ResearchRequirement> researchRequirements) {
        this.fluid = fluid;
        this.value = value;
        this.research = research;
        this.researchRequirements = researchRequirements;
    }

    @Override
    public Fluid getResource() {
        return fluid;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public long getResearch() {
        return research;
    }

    @Override
    public FluidBitInfo copy() {
        return new FluidBitInfo(fluid, value, research, researchRequirements);
    }

    @Override
    public ResearchRequirement createResearchRequirement() {
        return new FluidBitResearchRequirement(fluid, BitRegistries.FLUID);
    }

    @Override
    public List<ResearchRequirement> getResearchRequirements() {
        return researchRequirements;
    }

    @Override
    public <V> BitKnowledge<Fluid> getKnowledge(V provider) {
        return BitComponents.FLUID_KNOWLEDGE.get(provider);
    }
}
