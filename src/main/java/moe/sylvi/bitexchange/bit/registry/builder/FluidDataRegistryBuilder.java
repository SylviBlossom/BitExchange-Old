package moe.sylvi.bitexchange.bit.registry.builder;

import com.google.gson.JsonObject;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import moe.sylvi.bitexchange.bit.info.FluidBitInfo;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.JsonHelper;

import java.util.List;

public class FluidDataRegistryBuilder extends AbstractResearchableDataRegistryBuilder<Fluid, FluidBitInfo> {
    public FluidDataRegistryBuilder(BitRegistry<Fluid, FluidBitInfo> registry) {
        super(registry);
    }

    @Override
    FluidBitInfo parseJson(Fluid resource, JsonObject json) throws Throwable {
        double value = parseBitValue(resource, json);
        long ratio = JsonHelper.getLong(json, "ratio", FluidConstants.BUCKET);
        double research = JsonHelper.getDouble(json, "research", 1);
        boolean researchable = JsonHelper.getBoolean(json, "researchable", true);
        List<ResearchRequirement> researchRequirements = parseResearchRequirements(json);

        return BitInfo.ofFluid(resource, value, (long)Math.floor(research * ratio), ratio, researchable, researchRequirements);
    }

    @Override
    FluidBitInfo modifyResource(Fluid resource, FluidBitInfo info, JsonObject json) throws Throwable {
        double value = modifyBitValue(info, json);
        long ratio = JsonHelper.getLong(json, "ratio", info.getRatio());
        long research = parseResearch(json, info.getResearch());
        boolean researchable = parseResearchable(json, info.isResearchable());
        List<ResearchRequirement> researchRequirements = modifyResearchRequirements(info, json);

        return BitInfo.ofFluid(resource, value, research, ratio, researchable, researchRequirements);
    }
}
