package moe.sylvi.bitexchange.transfer;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public class PreviewContainerItemContext implements ContainerItemContext {
    private final Inventory inventory = new SimpleInventory(9);
    private final World world;

    public PreviewContainerItemContext(ItemStack itemStack, World world) {
        inventory.setStack(0, itemStack);

        this.world = world;
    }

    public InventoryStorage getStorage() {
        return InventoryStorage.of(inventory, null);
    }

    @Override
    public SingleSlotStorage<ItemVariant> getMainSlot() {
        return getStorage().getSlots().get(0);
    }

    @Override
    public long extract(ItemVariant itemVariant, long maxAmount, TransactionContext transaction) {
        return ContainerItemContext.super.extract(itemVariant, maxAmount, transaction);
    }

    @Override
    public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
        return getStorage().insert(itemVariant, maxAmount, transactionContext);
    }

    @Override
    public List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
        return getStorage().getSlots();
    }
}
