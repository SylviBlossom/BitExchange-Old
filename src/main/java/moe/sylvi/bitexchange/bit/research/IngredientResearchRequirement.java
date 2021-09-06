package moe.sylvi.bitexchange.bit.research;

import com.google.common.collect.Lists;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.Recursable;
import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;

import java.util.*;

public class IngredientResearchRequirement extends CombinedResearchRequirement<ResearchRequirement> {
    private final Ingredient ingredient;
    private final Hashtable<Item, ResearchRequirement> cached = new Hashtable<>();

    public IngredientResearchRequirement(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    @Override
    public List<ResearchRequirement> getRequirements() {
        List<ResearchRequirement> requirements = Lists.newArrayList();
        for (ItemStack stack : ingredient.getMatchingStacks()) {
            ResearchRequirement requirement = cached.computeIfAbsent(stack.getItem(), item -> {
                Recursable<ItemBitInfo> info = BitRegistries.ITEM.getOrProcess(item);
                return info.notNullOrRecursive() ? info.get().createResearchRequirement() : null;
            });
            if (requirement != null) {
                requirements.add(requirement);
            }
        }
        return requirements;
    }
}
