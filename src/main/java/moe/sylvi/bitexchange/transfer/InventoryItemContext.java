package moe.sylvi.bitexchange.transfer;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.inventory.Inventory;
import net.minecraft.world.World;

import java.util.List;

public class InventoryItemContext implements ContainerItemContext {
    private final InventoryStorage storage;
    private final SingleSlotStorage<ItemVariant> slot;
    private final World world;

    public InventoryItemContext(InventoryStorage storage, SingleSlotStorage<ItemVariant> slot, World world) {
        this.storage = storage;
        this.slot = slot;
        this.world = world;
    }

    public InventoryItemContext(InventoryStorage storage, int slot, World world) {
        this(storage, storage.getSlots().get(slot), world);
    }

    @Override
    public SingleSlotStorage<ItemVariant> getMainSlot() {
        return slot;
    }

    @Override
    public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
        return storage.insert(itemVariant, maxAmount, transactionContext);
    }

    @Override
    public List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
        return storage.getSlots();
    }
}
