package moe.sylvi.bitexchange.transfer;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.item.ItemStack;

public class FullInventoryStorageSlot extends SingleStackStorage {
    private final FullInventoryStorage storage;
    private final int slot;

    public FullInventoryStorageSlot(FullInventoryStorage storage, int slot) {
        this.storage = storage;
        this.slot = slot;
    }

    @Override
    protected ItemStack getStack() {
        return storage.inventory.getStack(slot);
    }

    @Override
    protected void setStack(ItemStack stack) {
        storage.inventory.setStack(slot, stack);
    }

    @Override
    protected boolean canInsert(ItemVariant itemVariant) {
        return storage.inventory.isValid(slot, itemVariant.toStack());
    }

    @Override
    protected boolean canExtract(ItemVariant itemVariant) {
        return super.canExtract(itemVariant);
    }

    @Override
    protected int getCapacity(ItemVariant itemVariant) {
        return Math.min(storage.inventory.getMaxCountPerStack(), storage.inventory.getStack(slot).getMaxCount());
    }
}
