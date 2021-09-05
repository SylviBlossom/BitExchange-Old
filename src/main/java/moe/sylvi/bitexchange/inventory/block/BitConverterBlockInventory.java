package moe.sylvi.bitexchange.inventory.block;

import moe.sylvi.bitexchange.inventory.BitConsumerInventory;

public interface BitConverterBlockInventory extends BitConsumerInventory {
    static BitConverterBlockInventory blank() {
        return new BitConverterBlockInventoryImpl();
    }

    default int getDefaultInventorySize() {
        return 2;
    }

    @Override
    default int getStorageSlot() {
        return 0;
    }

    @Override
    default int getInputSlot() {
        return 1;
    }
}
