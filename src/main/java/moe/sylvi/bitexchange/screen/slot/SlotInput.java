package moe.sylvi.bitexchange.screen.slot;

import moe.sylvi.bitexchange.BitComponents;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.storage.BitStorages;
import moe.sylvi.bitexchange.transfer.SimpleItemContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SlotInput extends Slot {
    private PlayerInventory playerInventory;

    public SlotInput(Inventory inventory, int index, int x, int y, PlayerInventory playerInventory) {
        super(inventory, index, x, y);
        this.playerInventory = playerInventory;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        Item item = stack.getItem();
        SimpleItemContext context = new SimpleItemContext(stack);
        return context.find(BitStorages.ITEM) != null || (BitRegistries.ITEM.getValue(item) > 0 && BitComponents.ITEM_KNOWLEDGE.get(playerInventory.player).hasLearned(item));
    }
}
