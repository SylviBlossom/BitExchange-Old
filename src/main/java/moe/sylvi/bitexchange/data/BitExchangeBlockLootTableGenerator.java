package moe.sylvi.bitexchange.data;

import moe.sylvi.bitexchange.BitExchange;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.condition.SurvivesExplosionLootCondition;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.CopyNbtLootFunction;
import net.minecraft.loot.provider.nbt.ContextLootNbtProvider;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.util.Identifier;

import java.util.function.BiConsumer;

public class BitExchangeBlockLootTableGenerator extends FabricBlockLootTableProvider {
    public BitExchangeBlockLootTableGenerator(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected void generateBlockLootTables() {
        addDrop(BitExchange.BIT_CONVERTER_BLOCK, block -> LootTable.builder()
                .pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1.0F))
                        .with(ItemEntry.builder(BitExchange.BIT_CONVERTER_BLOCK_ITEM)
                                .apply(CopyNbtLootFunction.builder(ContextLootNbtProvider.BLOCK_ENTITY)
                                        .withOperation("Items", "BlockEntityTag.Items")
                                )
                        )
                        .conditionally(SurvivesExplosionLootCondition.builder())
                )
        );
        addDrop(BitExchange.BIT_RESEARCHER_BLOCK);
        addDrop(BitExchange.BIT_FACTORY_BLOCK);
        addDrop(BitExchange.BIT_LIQUEFIER_BLOCK);
        addDrop(BitExchange.BIT_MINER_BLOCK);
        addDrop(BitExchange.BYTE_MINER_BLOCK);
        addDrop(BitExchange.KILOBIT_MINER_BLOCK);
        addDrop(BitExchange.MEGABIT_MINER_BLOCK);
        addDrop(BitExchange.GIGABIT_MINER_BLOCK);
        addDrop(BitExchange.TERABIT_MINER_BLOCK);
        addDrop(BitExchange.PETABIT_MINER_BLOCK);
        addDrop(BitExchange.EXABIT_MINER_BLOCK);
        addDrop(BitExchange.ITTY_BIT_MINER_BLOCK);
    }
}
