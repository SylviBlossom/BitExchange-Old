package moe.sylvi.bitexchange.bit.registry.builder.recipe;

import moe.sylvi.bitexchange.mixin.SmithingRecipeMixin;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;

import java.util.List;

public class SmithingRecipeHandler extends AbstractSimpleRecipeHandler<Recipe<Inventory>> {

    @Override
    public List<Ingredient> getItemIngredients(Recipe<Inventory> recipe) {
        return List.of(((SmithingRecipeMixin)recipe).bitexchange_getBase(), ((SmithingRecipeMixin)recipe).bitexchange_getAddition());
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
