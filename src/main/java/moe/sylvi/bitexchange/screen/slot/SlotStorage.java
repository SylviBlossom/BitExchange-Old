package moe.sylvi.bitexchange.screen.slot;

import moe.sylvi.bitexchange.bit.storage.BitStorages;
import moe.sylvi.bitexchange.transfer.SimpleItemContext;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SlotStorage extends Slot {
    public SlotStorage(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        SimpleItemContext context = new SimpleItemContext(stack);
        return context.find(BitStorages.ITEM) != null;
    }
}
