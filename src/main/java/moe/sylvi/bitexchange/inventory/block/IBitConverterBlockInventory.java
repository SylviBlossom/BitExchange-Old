package moe.sylvi.bitexchange.inventory.block;

import moe.sylvi.bitexchange.inventory.IBitConsumerInventory;

public interface IBitConverterBlockInventory extends IBitConsumerInventory {
    static IBitConverterBlockInventory blank() {
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
