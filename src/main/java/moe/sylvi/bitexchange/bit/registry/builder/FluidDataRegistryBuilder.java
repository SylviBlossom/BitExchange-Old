package moe.sylvi.bitexchange.bit.registry.builder;

import com.google.gson.JsonObject;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import moe.sylvi.bitexchange.bit.info.FluidBitInfo;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.JsonHelper;

import java.util.List;

public class FluidDataRegistryBuilder extends DataRegistryBuilder<Fluid, FluidBitInfo> {
    public FluidDataRegistryBuilder(BitRegistry<Fluid, FluidBitInfo> registry) {
        super(registry);
    }

    @Override
    FluidBitInfo parseJson(Fluid resource, JsonObject json) throws Throwable {
        double value = parseJsonBits(resource, json);
        long research = JsonHelper.getLong(json, "research", 1);
        List<ResearchRequirement> researchRequirements = parseResearchRequirements(json);

        return BitInfo.ofFluid(resource, value, research, researchRequirements);
    }
}
