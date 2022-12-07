package moe.sylvi.bitexchange.bit.research;

import com.google.common.collect.Lists;
import moe.sylvi.bitexchange.bit.Recursable;
import moe.sylvi.bitexchange.bit.registry.builder.recipe.ResourceIngredient;
import moe.sylvi.bitexchange.bit.info.BitInfoResearchable;

import java.util.*;

public class IngredientResearchRequirement<R, I extends BitInfoResearchable<R>> extends AbstractCombinedResearchRequirement<ResearchRequirement> {
    private final ResourceIngredient<R, I> ingredient;
    private final Hashtable<R, ResearchRequirement> cached = new Hashtable<>();

    public IngredientResearchRequirement(ResourceIngredient<R, I> ingredient) {
        this.ingredient = ingredient;
    }

    @Override
    public List<ResearchRequirement> getRequirements() {
        List<ResearchRequirement> requirements = Lists.newArrayList();
        for (var resource : ingredient.getResources()) {
            ResearchRequirement requirement = cached.computeIfAbsent(resource.getResource(), item -> {
                Recursable<I> info = resource.getOrProcessInfo();
                return info.notNullOrRecursive() ? info.get().createResearchRequirement() : null;
            });
            if (requirement != null) {
                requirements.add(requirement);
            }
        }
        return requirements;
    }
}
