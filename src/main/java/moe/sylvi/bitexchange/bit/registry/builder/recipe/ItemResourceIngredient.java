package moe.sylvi.bitexchange.bit.registry.builder.recipe;

import com.google.common.collect.Lists;
import moe.sylvi.bitexchange.bit.BitResource;
import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;

import java.util.ArrayList;
import java.util.List;

public class ItemResourceIngredient implements ResourceIngredient<Item, ItemBitInfo> {
    protected final List<BitResource<Item, ItemBitInfo>> resources;
    protected final int index;

    public ItemResourceIngredient(Ingredient ingredient) {
        this(ingredient, -1);
    }
    public ItemResourceIngredient(Ingredient ingredient, int index) {
        this.index = index;
        this.resources = new ArrayList<>();
        for (var stack : ingredient.getMatchingStacks()) {
            resources.add(BitResource.fromStack(stack));
        }
    }

    public ItemResourceIngredient(List<ItemStack> stacks) {
        this(stacks, -1);
    }
    public ItemResourceIngredient(List<ItemStack> stacks, int index) {
        this.index = index;
        this.resources = new ArrayList<>();
        for (var stack : stacks) {
            resources.add(BitResource.fromStack(stack));
        }
    }

    @Override
    public List<BitResource<Item, ItemBitInfo>> getResources() {
        return resources;
    }

    @Override
    public int getIndex() {
        return index;
    }
}
