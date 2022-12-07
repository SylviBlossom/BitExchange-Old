package moe.sylvi.bitexchange.screen.slot;

import moe.sylvi.bitexchange.BitComponents;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.storage.BitStorages;
import moe.sylvi.bitexchange.transfer.SimpleItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SlotFluidResource extends Slot {
    private final PlayerInventory playerInventory;

    public SlotFluidResource(Inventory inventory, int index, int x, int y, PlayerInventory playerInventory) {
        super(inventory, index, x, y);
        this.playerInventory = playerInventory;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        SimpleItemContext context = new SimpleItemContext(stack);
        var fluidStorage = context.find(FluidStorage.ITEM);

        if (fluidStorage != null) {
            var fluidVariant = StorageUtil.findStoredResource(fluidStorage);
            return fluidVariant != null && !fluidVariant.isBlank() && BitComponents.FLUID_KNOWLEDGE.get(playerInventory.player).hasLearned(fluidVariant.getFluid());
        }

        return false;
    }

    @Override
    public int getMaxItemCount() {
        return 1;
    }
}
