package moe.sylvi.bitexchange.transfer;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;

import java.util.List;

public class SlicedInventoryStorageWrapper extends CombinedStorage<ItemVariant, SingleSlotStorage<ItemVariant>> implements InventoryStorage {
    private final InventoryStorage storage;
    private final int startIndex;
    private final int size;

    public SlicedInventoryStorageWrapper(InventoryStorage storage, int startIndex, int size) {
        super(storage.getSlots().subList(startIndex, startIndex + size));
        this.storage = storage;
        this.startIndex = startIndex;
        this.size = size;
    }

    @Override
    public List<SingleSlotStorage<ItemVariant>> getSlots() {
        return parts;
    }
}
