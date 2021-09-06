package moe.sylvi.bitexchange.bit.research;

import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.info.FluidBitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.text.MutableText;

public class FluidBitResearchRequirement extends BitResearchRequirement<Fluid> {
    public FluidBitResearchRequirement(Fluid resource, BitRegistry<Fluid, FluidBitInfo> registry) {
        super(resource, (BitRegistry)registry);
    }

    @Override
    public MutableText getName(PlayerEntity player) {
        return resource.getDefaultState().getBlockState().getBlock().getName().shallowCopy();
    }
}
