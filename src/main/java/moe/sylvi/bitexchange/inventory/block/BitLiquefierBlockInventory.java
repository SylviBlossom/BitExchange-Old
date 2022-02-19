package moe.sylvi.bitexchange.inventory.block;

import moe.sylvi.bitexchange.inventory.BitConsumerInventory;

public interface BitLiquefierBlockInventory extends BitConsumerInventory {
    static BitLiquefierBlockInventory blank() {
        return new BitLiquefierBlockInventoryImpl();
    }

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
