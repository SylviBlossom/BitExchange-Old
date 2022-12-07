package moe.sylvi.bitexchange.bit.registry.builder.recipe;

import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.BitResource;
import net.minecraft.item.Item;
import net.minecraft.recipe.Recipe;

import java.util.List;

public interface RecipeHandler<T extends Recipe<?>> {
    boolean isAutomatable(T recipe);

    List<ResourceIngredient<?,?>> getIngredients(T recipe);

    List<RecipeHandlerOutput<?,?>> getOutputs(T recipe);

    default boolean hasOutput(T recipe, Object resource) {
        for (var output : getOutputs(recipe)) {
            if (output.resource.getResource() == resource) {
                return true;
            }
        }
        return false;
    }

    default BitResource<?,?> getRemainder(T recipe, ResourceIngredient<?,?> ingredient, BitResource<?,?> resource) {
        if (resource.getResource() instanceof Item item) {
            return item.hasRecipeRemainder() ? BitResource.of(BitRegistries.ITEM, item.getRecipeRemainder(), resource.getAmount()) : null;
        }
        return null;
    }
}
