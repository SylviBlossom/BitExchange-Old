package moe.sylvi.bitexchange.bit.registry.builder;

import com.google.gson.JsonObject;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
import net.minecraft.item.Item;
import net.minecraft.util.JsonHelper;

import java.util.List;

public class ItemDataRegistryBuilder extends DataRegistryBuilder<Item, ItemBitInfo> {
    public ItemDataRegistryBuilder(BitRegistry<Item, ItemBitInfo> registry) {
        super(registry);
    }

    @Override
    ItemBitInfo parseJson(Item resource, JsonObject json) throws Throwable {
        double value = parseJsonBits(resource, json);
        long research = JsonHelper.getLong(json, "research", 1);
        boolean automatable = JsonHelper.getBoolean(json, "automatable", true);
        List<ResearchRequirement> researchRequirements = parseResearchRequirements(json);

        return BitInfo.ofItem(resource, value, research, automatable, researchRequirements);
    }
}
