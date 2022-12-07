package moe.sylvi.bitexchange.inventory.block;

import moe.sylvi.bitexchange.transfer.BitFluidStorage;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class BitConverterBlockInventoryImpl implements IBitConverterBlockInventory {
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(getDefaultInventorySize(), ItemStack.EMPTY);
    private final BitFluidStorage inputFluid = new BitFluidStorage();

    @Override
    public World getConsumerWorld() {
        return null;
    }

    @Override
    public BitFluidStorage getInputFluid() {
        return inputFluid;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }
}
