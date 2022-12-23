package moe.sylvi.bitexchange.bit.research;

import com.google.common.collect.Lists;
import moe.sylvi.bitexchange.bit.registry.builder.AbstractRecipeRegistryBuilder;
import moe.sylvi.bitexchange.bit.registry.builder.recipe.RecipeHandler;
import moe.sylvi.bitexchange.bit.registry.builder.recipe.ResourceIngredient;
import net.minecraft.recipe.Recipe;

import java.util.ArrayList;
import java.util.List;

public class RecipeResearchRequirement extends AbstractListResearchRequirement<IngredientResearchRequirement<?,?>> {
    private final List<IngredientResearchRequirement<?,?>> requirements;

    public RecipeResearchRequirement(Recipe<?> recipe) {
        this(recipe, AbstractRecipeRegistryBuilder.getRecipeHandler(recipe));
    }
    public RecipeResearchRequirement(Recipe<?> recipe, RecipeHandler handler) {
        this(handler.getIngredients(recipe));
    }

    public RecipeResearchRequirement(List<ResourceIngredient<?,?>> ingredients) {
        this.requirements = new ArrayList<>();
        for (var ingredient : ingredients) {
            if (!ingredient.isEmpty()) {
                IngredientResearchRequirement<?,?> requirement = new IngredientResearchRequirement(ingredient);
                if (!requirements.contains(requirement)) {
                    requirements.add(requirement);
                }
            }
        }
    }

    @Override
    public List<IngredientResearchRequirement<?,?>> getRequirements() {
        return requirements;
    }
}
