package moe.sylvi.bitexchange.transfer;

import com.google.common.collect.MapMaker;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.minecraft.inventory.Inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FullInventoryStorage extends CombinedStorage<ItemVariant, SingleSlotStorage<ItemVariant>> implements InventoryStorage {
    private static final Map<Inventory, FullInventoryStorage> WRAPPERS = new MapMaker().weakValues().makeMap();

    public static FullInventoryStorage of(Inventory inventory) {
        FullInventoryStorage storage = WRAPPERS.computeIfAbsent(inventory, FullInventoryStorage::new);
        storage.resizeSlotList();
        return storage;
    }

    public static SlicedInventoryStorageWrapper ofSlice(Inventory inventory, int startIndex, int size) {
        FullInventoryStorage storage = of(inventory);
        return new SlicedInventoryStorageWrapper(storage, startIndex, size);
    }

    public final Inventory inventory;
    private final List<FullInventoryStorageSlot> slots;

    public FullInventoryStorage(Inventory inventory) {
        super(Collections.emptyList());
        this.inventory = inventory;
        this.slots = new ArrayList<>();

        resizeSlotList();
    }

    private void resizeSlotList() {
        int inventorySize = inventory.size();

        if (inventorySize != parts.size()) {
            while (slots.size() < inventorySize) {
                slots.add(new FullInventoryStorageSlot(this, slots.size()));
            }

            parts = Collections.unmodifiableList(slots.subList(0, inventorySize));
        }
    }

    @Override
    public List<SingleSlotStorage<ItemVariant>> getSlots() {
        return parts;
    }
}
