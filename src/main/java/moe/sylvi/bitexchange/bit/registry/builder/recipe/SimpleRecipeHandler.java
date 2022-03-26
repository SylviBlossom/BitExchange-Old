package moe.sylvi.bitexchange.bit.registry.builder.recipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;

import java.util.List;

public class SimpleRecipeHandler extends AbstractSimpleRecipeHandler<Recipe<Inventory>> {
    @Override
    public List<Ingredient> getItemIngredients(Recipe<Inventory> recipe) {
        return recipe.getIngredients();
    }

    @Override
    public ItemStack getOutputStack(Recipe<Inventory> recipe) {
        return recipe.getOutput();
    }

    @Override
    public boolean isAutomatable(Recipe<Inventory> recipe) {
        return false;
    }
}
