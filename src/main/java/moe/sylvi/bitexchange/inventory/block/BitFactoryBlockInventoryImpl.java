package moe.sylvi.bitexchange.inventory.block;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class BitFactoryBlockInventoryImpl implements BitFactoryBlockInventory {
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(12, ItemStack.EMPTY);

    @Override
    public World getWorld() {
        return null;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }
}
