package moe.sylvi.bitexchange.bit.registry.builder;

import com.google.gson.JsonObject;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
import net.minecraft.item.Item;
import net.minecraft.util.JsonHelper;

import java.util.List;

public class ItemDataRegistryBuilder extends AbstractResearchableDataRegistryBuilder<Item, ItemBitInfo> {
    public ItemDataRegistryBuilder(BitRegistry<Item, ItemBitInfo> registry) {
        super(registry);
    }

    @Override
    ItemBitInfo parseJson(Item resource, JsonObject json) throws Throwable {
        double value = parseBitValue(resource, json);
        long research = parseResearch(json, 1);
        boolean researchable = parseResearchable(json, true);
        boolean automatable = JsonHelper.getBoolean(json, "automatable", true);
        List<ResearchRequirement> researchRequirements = parseResearchRequirements(json);

        return BitInfo.ofItem(resource, value, research, researchable, automatable, researchRequirements);
    }

    @Override
    ItemBitInfo modifyResource(Item resource, ItemBitInfo info, JsonObject json) throws Throwable {
        double value = modifyBitValue(info, json);
        long research = parseResearch(json, info.getResearch());
        boolean researchable = parseResearchable(json, info.isResearchable());
        boolean automatable = JsonHelper.getBoolean(json, "automatable", info.isAutomatable());
        List<ResearchRequirement> researchRequirements = modifyResearchRequirements(info, json);

        return BitInfo.ofItem(resource, value, research, researchable, automatable, researchRequirements);
    }
}
