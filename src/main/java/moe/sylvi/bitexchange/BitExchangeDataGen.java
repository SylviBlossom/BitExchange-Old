package moe.sylvi.bitexchange;

import moe.sylvi.bitexchange.data.*;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class BitExchangeDataGen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        fabricDataGenerator.addProvider(BitExchangeItemBitGenerator::new);
        fabricDataGenerator.addProvider(BitExchangeItemTagGenerator::new);
        fabricDataGenerator.addProvider(BitExchangeBlockTagGenerator::new);
        fabricDataGenerator.addProvider(BitExchangeBlockLootTableGenerator::new);
        fabricDataGenerator.addProvider(BitExchangeFluidBitGenerator::new);
    }
}
