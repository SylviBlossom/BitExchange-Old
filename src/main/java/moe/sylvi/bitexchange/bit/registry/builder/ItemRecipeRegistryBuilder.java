package moe.sylvi.bitexchange.bit.registry.builder;

import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import net.minecraft.item.Item;
import net.minecraft.recipe.Recipe;

public class ItemRecipeRegistryBuilder extends AbstractRecipeRegistryBuilder<Item, ItemBitInfo> {

    public ItemRecipeRegistryBuilder(BitRegistry<Item, ItemBitInfo> registry) {
        super(registry);
    }

    @Override
    Class<Item> getResourceClass() {
        return Item.class;
    }

    @Override
    boolean shouldProcess(Item resource, Recipe<?> recipe) {
        return true;
    }

    @Override
    public ItemBitInfo createInfo(Item resource, double bits, Recipe<?> smallestRecipe) {
        var isResource = getRecipeHandler(smallestRecipe).isAutomatable(smallestRecipe);
        return BitInfo.ofItem(resource, bits, 1, true, isResource);
    }
}
