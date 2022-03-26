package moe.sylvi.bitexchange.bit.registry.builder.recipe;

import moe.sylvi.bitexchange.bit.BitResource;
import moe.sylvi.bitexchange.bit.Recursable;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.recipe.Recipe;

import java.util.List;

public interface IRecipeHandler<T extends Recipe<?>, R extends BitResource> {
    boolean isAutomatable(T recipe);

    List<List<R>> getIngredients(T recipe);

    List<R> getOutputs(T recipe);

    default boolean hasOutput(T recipe, Object resource) {
        for (var output : getOutputs(recipe)) {
            if (output.getResource() == resource) {
                return true;
            }
        }
        return false;
    }
}
