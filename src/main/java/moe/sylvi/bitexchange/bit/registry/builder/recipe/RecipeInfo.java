package moe.sylvi.bitexchange.bit.registry.builder.recipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;

import java.util.List;

public class RecipeInfo {
    public boolean isAutomatable(Recipe<Inventory> recipe) {
        return false;
    }

    public List<Ingredient> getIngredients(Recipe<Inventory> recipe) {
        return recipe.getIngredients();
    }
}
