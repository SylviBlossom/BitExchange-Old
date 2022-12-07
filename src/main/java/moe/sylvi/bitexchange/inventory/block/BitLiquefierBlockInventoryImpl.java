package moe.sylvi.bitexchange.inventory.block;

import moe.sylvi.bitexchange.transfer.BitFluidStorage;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import static moe.sylvi.bitexchange.block.entity.BitLiquefierBlockEntity.FLUID_CAPACITY;

public class BitLiquefierBlockInventoryImpl implements IBitLiquefierBlockInventory {
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(getDefaultInventorySize(), ItemStack.EMPTY);
    private final BitFluidStorage inputFluid = new BitFluidStorage();
    private final BitFluidStorage outputFluid = new BitFluidStorage(FLUID_CAPACITY, false, true);

    @Override
    public World getConsumerWorld() {
        return null;
    }

    @Override
    public BitFluidStorage getInputFluid() {
        return inputFluid;
    }

    @Override
    public BitFluidStorage getOuputFluid() {
        return outputFluid;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }
}
