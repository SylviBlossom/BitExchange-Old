package moe.sylvi.bitexchange.bit.registry.builder.recipe;

import moe.sylvi.bitexchange.bit.BitResource;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSimpleRecipeHandler<T extends Recipe<?>> implements RecipeHandler<T> {
    @Override
    public List<ResourceIngredient<?,?>> getIngredients(T recipe) {
        List<ResourceIngredient<?,?>> result = new ArrayList<>();

        var ingredients = getItemIngredients(recipe);
        for (var i = 0; i < ingredients.size(); i++) {
            var ingredient = ingredients.get(i);

            if (ingredient.isEmpty()) {
                continue;
            }

            var resourceIngredient = new ItemResourceIngredient(ingredient, i);

            if (!resourceIngredient.isEmpty()) {
                result.add(resourceIngredient);
            }
        }

        return result;
    }

    @Override
    public List<RecipeHandlerOutput<?,?>> getOutputs(T recipe) {
        var stack = getOutputStack(recipe);

        return List.of(new RecipeHandlerOutput<>(BitResource.fromStack(stack)));
    }

    public abstract List<Ingredient> getItemIngredients(T recipe);

    public abstract ItemStack getOutputStack(T recipe);
}
