package moe.sylvi.bitexchange.bit.registry.builder;

import moe.sylvi.bitexchange.bit.info.FluidBitInfo;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import moe.sylvi.bitexchange.bit.research.ResearchTier;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.fluid.Fluid;
import net.minecraft.recipe.Recipe;

public class FluidRecipeRegistryBuilder extends AbstractRecipeRegistryBuilder<Fluid, FluidBitInfo> {

    public FluidRecipeRegistryBuilder(BitRegistry<Fluid, FluidBitInfo> registry) {
        super(registry);
    }

    @Override
    Class<Fluid> getResourceClass() {
        return Fluid.class;
    }

    @Override
    boolean shouldProcess(Fluid resource, Recipe<?> recipe) {
        return true;
    }

    @Override
    public FluidBitInfo createInfo(Fluid resource, double bits, Recipe<?> smallestRecipe) {
        return BitInfo.ofFluid(resource, bits, ResearchTier.CRAFTED.getResearch() * FluidConstants.BUCKET, FluidConstants.BUCKET, true);
    }
}
