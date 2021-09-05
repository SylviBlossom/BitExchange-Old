package moe.sylvi.bitexchange.screen.slot;

import moe.sylvi.bitexchange.BitRegistries;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SlotResearch extends Slot {
    public SlotResearch(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return BitRegistries.ITEM.getResearch(stack.getItem()) > 0;
    }
}
