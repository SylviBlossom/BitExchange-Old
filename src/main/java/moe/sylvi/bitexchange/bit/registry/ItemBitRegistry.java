package moe.sylvi.bitexchange.bit.registry;

import com.google.common.collect.Lists;
import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class ItemBitRegistry extends BitRegistryImpl<Item, ItemBitInfo> {
    public ItemBitRegistry(Registry<Item> resourceRegistry) {
        super(resourceRegistry);
    }

    public long getResearch(Item item) {
        ItemBitInfo info = get(item);
        return info != null ? info.getResearch() : 0;
    }

    public List<ResearchRequirement> getResearchRequirements(Item item) {
        ItemBitInfo info = get(item);
        return info != null ? info.getResearchRequirements() : Lists.newArrayList();
    }

    public boolean isAutomatable(Item item) {
        ItemBitInfo info = get(item);
        return info != null && info.isAutomatable();
    }
}
