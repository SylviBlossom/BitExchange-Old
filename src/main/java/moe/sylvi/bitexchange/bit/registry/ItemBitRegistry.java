package moe.sylvi.bitexchange.bit.registry;

import moe.sylvi.bitexchange.BitComponents;
import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import moe.sylvi.bitexchange.bit.research.BitKnowledge;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public class ItemBitRegistry extends SimpleBitRegistry<Item, ItemBitInfo> implements ResearchableBitRegistry<Item, ItemBitInfo> {
    public ItemBitRegistry(Registry<Item> resourceRegistry) {
        super(resourceRegistry);
    }

    @Override
    public <V> BitKnowledge<Item> getKnowledge(V provider) {
        return BitComponents.ITEM_KNOWLEDGE.get(provider);
    }

    public boolean isAutomatable(Item item) {
        ItemBitInfo info = get(item);
        return info != null && info.isAutomatable();
    }
}
