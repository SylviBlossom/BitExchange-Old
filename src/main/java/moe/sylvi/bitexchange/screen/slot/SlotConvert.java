package moe.sylvi.bitexchange.screen.slot;

import moe.sylvi.bitexchange.inventory.BitConsumerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SlotConvert extends Slot {
    private BitConsumerInventory bcInventory;

    public SlotConvert(Inventory inventory, int index, int x, int y, BitConsumerInventory bcInventory) {
        super(inventory, index, x, y);
        this.bcInventory = bcInventory;
    }

    @Override
    public ItemStack takeStack(int amount) {
        return this.bcInventory.createStack(this.getStack(), amount);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }
}
