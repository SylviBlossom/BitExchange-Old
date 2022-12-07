package moe.sylvi.bitexchange.bit.registry;

import moe.sylvi.bitexchange.BitComponents;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.info.FluidBitInfo;
import moe.sylvi.bitexchange.bit.research.BitKnowledge;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.registry.Registry;

public class FluidBitRegistry extends SimpleBitRegistry<Fluid, FluidBitInfo> implements ResearchableBitRegistry<Fluid, FluidBitInfo> {
    public FluidBitRegistry(Registry<Fluid> resourceRegistry) {
        super(resourceRegistry, BitInfo.ofFluid(Fluids.EMPTY, 1, 1, FluidConstants.BUCKET, true));
    }

    @Override
    public <V> BitKnowledge<Fluid> getKnowledge(V provider) {
        return BitComponents.FLUID_KNOWLEDGE.get(provider);
    }
}
