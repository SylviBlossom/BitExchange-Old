package moe.sylvi.bitexchange.screen.slot;

import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.research.ResearchableItem;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SlotResearch extends Slot {
    private final PlayerInventory playerInventory;

    public SlotResearch(Inventory inventory, PlayerInventory playerInventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.playerInventory = playerInventory;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        var item = stack.getItem();
        if (item instanceof ResearchableItem researchableItem) {
            return researchableItem.canResearch(stack, playerInventory.player);
        } else {
            var info = BitRegistries.ITEM.get(item);
            return info != null && info.isResearchable() && info.getResearch() > 0;
        }
    }
}
