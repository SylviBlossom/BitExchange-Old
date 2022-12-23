package moe.sylvi.bitexchange.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BitExchangeItemTagGenerator extends FabricTagProvider<Item> {
    private static final TagKey<Item> C_COPPER_INGOTS   = TagKey.of(Registry.ITEM_KEY, new Identifier("c", "copper_ingots"));
    private static final TagKey<Item> C_IRON_INGOTS     = TagKey.of(Registry.ITEM_KEY, new Identifier("c", "iron_ingots"));
    private static final TagKey<Item> C_GOLD_INGOTS     = TagKey.of(Registry.ITEM_KEY, new Identifier("c", "gold_ingots"));
    private static final TagKey<Item> C_SHULKER_BOXES   = TagKey.of(Registry.ITEM_KEY, new Identifier("c", "shulker_boxes"));

    public BitExchangeItemTagGenerator(FabricDataGenerator dataGenerator) {
        super(dataGenerator, Registry.ITEM);
    }

    @Override
    protected void generateTags() {
        getOrCreateTagBuilder(C_COPPER_INGOTS).add(Items.COPPER_INGOT);
        getOrCreateTagBuilder(C_IRON_INGOTS).add(Items.IRON_INGOT);
        getOrCreateTagBuilder(C_GOLD_INGOTS).add(Items.GOLD_INGOT);

        getOrCreateTagBuilder(C_SHULKER_BOXES)
                .add(Items.SHULKER_BOX)
                .add(Items.WHITE_SHULKER_BOX)
                .add(Items.ORANGE_SHULKER_BOX)
                .add(Items.MAGENTA_SHULKER_BOX)
                .add(Items.LIGHT_BLUE_SHULKER_BOX)
                .add(Items.YELLOW_SHULKER_BOX)
                .add(Items.LIME_SHULKER_BOX)
                .add(Items.PINK_SHULKER_BOX)
                .add(Items.GRAY_SHULKER_BOX)
                .add(Items.LIGHT_GRAY_SHULKER_BOX)
                .add(Items.CYAN_SHULKER_BOX)
                .add(Items.PURPLE_SHULKER_BOX)
                .add(Items.BLUE_SHULKER_BOX)
                .add(Items.BROWN_SHULKER_BOX)
                .add(Items.GREEN_SHULKER_BOX)
                .add(Items.RED_SHULKER_BOX)
                .add(Items.BLACK_SHULKER_BOX);
    }
}
