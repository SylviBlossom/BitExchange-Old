package moe.sylvi.bitexchange.bit.registry.builder.recipe;

import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.BitResource;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public abstract class AbstractSimpleRecipeHandler<T extends Recipe<?>> implements IRecipeHandler<T, BitResource> {
    @Override
    public List<List<BitResource>> getIngredients(T recipe) {
        List<List<BitResource>> result = Lists.newArrayList();

        for (var ingredient : getItemIngredients(recipe)) {
            if (ingredient.isEmpty()) {
                continue;
            }

            List<BitResource> options = Lists.newArrayList();

            for (var stack : ingredient.getMatchingStacks()) {
                options.add(BitResource.of(BitRegistries.ITEM, stack.getItem(), stack.getCount()));
            }

            if (options.size() > 0) {
                result.add(options);
            }
        }

        return result;
    }

    @Override
    public List<BitResource> getOutputs(T recipe) {
        var stack = getOutputStack(recipe);

        return List.of(BitResource.of(BitRegistries.ITEM, stack.getItem(), stack.getCount()));
    }

    public abstract List<Ingredient> getItemIngredients(T recipe);

    public abstract ItemStack getOutputStack(T recipe);
}
