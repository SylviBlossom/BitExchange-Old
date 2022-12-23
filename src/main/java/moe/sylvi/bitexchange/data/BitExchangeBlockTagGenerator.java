package moe.sylvi.bitexchange.data;

import moe.sylvi.bitexchange.BitExchange;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.registry.Registry;

public class BitExchangeBlockTagGenerator extends FabricTagProvider<Block> {
    public BitExchangeBlockTagGenerator(FabricDataGenerator dataGenerator) {
        super(dataGenerator, Registry.BLOCK);
    }

    @Override
    protected void generateTags() {
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(BitExchange.BIT_CONVERTER_BLOCK)
                .add(BitExchange.BIT_RESEARCHER_BLOCK)
                .add(BitExchange.BIT_FACTORY_BLOCK)
                .add(BitExchange.BIT_LIQUEFIER_BLOCK)
                .add(BitExchange.BIT_MINER_BLOCK)
                .add(BitExchange.BYTE_MINER_BLOCK)
                .add(BitExchange.KILOBIT_MINER_BLOCK)
                .add(BitExchange.MEGABIT_MINER_BLOCK)
                .add(BitExchange.GIGABIT_MINER_BLOCK)
                .add(BitExchange.TERABIT_MINER_BLOCK)
                .add(BitExchange.PETABIT_MINER_BLOCK)
                .add(BitExchange.EXABIT_MINER_BLOCK)
                .add(BitExchange.ITTY_BIT_MINER_BLOCK);
    }
}
