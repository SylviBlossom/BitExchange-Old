package moe.sylvi.bitexchange.bit.research;

import com.google.common.collect.Lists;
import moe.sylvi.bitexchange.bit.registry.builder.RecipeRegistryBuilder;
import moe.sylvi.bitexchange.bit.registry.builder.recipe.IRecipeHandler;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;

import java.util.List;

public class RecipeResearchRequirement extends ListResearchRequirement<IngredientResearchRequirement> {
    private final List<IngredientResearchRequirement> requirements;

    public RecipeResearchRequirement(Recipe<Inventory> recipe) {
        this(recipe, RecipeRegistryBuilder.getRecipeHandler(recipe));
    }

    public RecipeResearchRequirement(Recipe<Inventory> recipe, IRecipeHandler handler) {
        this.requirements = Lists.newArrayList();
        for (var options : handler.getIngredients(recipe)) {
            if (!ingredient.isEmpty() && ingredient.getMatchingStacks() != null) {
                IngredientResearchRequirement requirement = new IngredientResearchRequirement(ingredient);
                if (!requirements.contains(requirement)) {
                    requirements.add(requirement);
                }
            }
        }
    }

    @Override
    public List<IngredientResearchRequirement> getRequirements() {
        return requirements;
    }
}
