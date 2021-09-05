package moe.sylvi.bitexchange.bit.research;

import com.google.common.collect.Lists;
import moe.sylvi.bitexchange.bit.registry.builder.recipe.RecipeInfo;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;

import java.util.List;

public class RecipeResearchRequirement extends ListResearchRequirement<IngredientResearchRequirement> {
    private final List<IngredientResearchRequirement> requirements;

    public RecipeResearchRequirement(Recipe<Inventory> recipe, RecipeInfo info) {
        this.requirements = Lists.newArrayList();
        for (Ingredient ingredient : (info != null ? info.getIngredients(recipe) : recipe.getIngredients())) {
            IngredientResearchRequirement requirement = new IngredientResearchRequirement(ingredient);
            if (!requirements.contains(requirement)) {
                requirements.add(requirement);
            }
        }
    }

    @Override
    public List<IngredientResearchRequirement> getRequirements() {
        return requirements;
    }
}
