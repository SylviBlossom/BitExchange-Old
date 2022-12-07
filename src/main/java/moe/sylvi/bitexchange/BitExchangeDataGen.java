package moe.sylvi.bitexchange;

import moe.sylvi.bitexchange.data.BitExchangeFluidBitProvider;
import moe.sylvi.bitexchange.data.BitExchangeItemBitProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class BitExchangeDataGen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        fabricDataGenerator.addProvider(BitExchangeItemBitProvider::new);
        fabricDataGenerator.addProvider(BitExchangeFluidBitProvider::new);
    }
}
