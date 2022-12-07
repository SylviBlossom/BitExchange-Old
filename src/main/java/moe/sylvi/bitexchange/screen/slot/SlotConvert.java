package moe.sylvi.bitexchange.screen.slot;

import moe.sylvi.bitexchange.inventory.IBitConsumerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SlotConvert extends Slot {
    private IBitConsumerInventory bcInventory;

    public SlotConvert(Inventory inventory, int index, int x, int y, IBitConsumerInventory bcInventory) {
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
