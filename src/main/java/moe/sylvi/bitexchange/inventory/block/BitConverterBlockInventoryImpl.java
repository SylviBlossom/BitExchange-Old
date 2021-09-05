package moe.sylvi.bitexchange.inventory.block;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class BitConverterBlockInventoryImpl implements BitConverterBlockInventory {
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(2, ItemStack.EMPTY);

    @Override
    public World getWorld() {
        return null;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }
}
