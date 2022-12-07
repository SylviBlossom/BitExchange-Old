package moe.sylvi.bitexchange.inventory.block;

import moe.sylvi.bitexchange.inventory.IBitConsumerInventory;

public interface IBitFactoryBlockInventory extends IBitConsumerInventory {
    static IBitFactoryBlockInventory blank() {
        return new BitFactoryBlockInventoryImpl();
    }

    default int getDefaultInventorySize() {
        return 12;
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
