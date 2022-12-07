package moe.sylvi.bitexchange.inventory.block;

import moe.sylvi.bitexchange.inventory.IBitConsumerInventory;
import moe.sylvi.bitexchange.transfer.BitFluidStorage;

public interface IBitLiquefierBlockInventory extends IBitConsumerInventory {
    static IBitLiquefierBlockInventory blank() {
        return new BitLiquefierBlockInventoryImpl();
    }

    BitFluidStorage getOuputFluid();

    default int getDefaultInventorySize() {
        return 3;
    }

    @Override
    default int getStorageSlot() {
        return 0;
    }

    @Override
    default int getInputSlot() {
        return 2;
    }
}
