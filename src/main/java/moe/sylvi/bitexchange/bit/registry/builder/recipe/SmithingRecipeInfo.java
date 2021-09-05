package moe.sylvi.bitexchange.bit.registry.builder.recipe;

import com.google.common.collect.Lists;
import moe.sylvi.bitexchange.mixin.SmithingRecipeMixin;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;

import java.util.List;

public class SmithingRecipeInfo extends RecipeInfo {
    @Override
    public List<Ingredient> getIngredients(Recipe<Inventory> recipe) {
        return Lists.newArrayList(((SmithingRecipeMixin)recipe).getBase(), ((SmithingRecipeMixin)recipe).getAddition());
    }
}
