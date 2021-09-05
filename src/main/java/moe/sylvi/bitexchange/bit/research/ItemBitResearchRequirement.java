package moe.sylvi.bitexchange.bit.research;

import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.text.MutableText;

public class ItemBitResearchRequirement extends BitResearchRequirement<Item> {
    public ItemBitResearchRequirement(Item resource, BitRegistry<Item, ItemBitInfo> registry) {
        super(resource, (BitRegistry)registry);
    }

    @Override
    public MutableText getName(PlayerEntity player) {
        return resource.getName().shallowCopy();
    }
}
