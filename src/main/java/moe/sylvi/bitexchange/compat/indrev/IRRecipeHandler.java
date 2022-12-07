package moe.sylvi.bitexchange.compat.indrev;

import com.google.common.collect.Lists;
import me.steven.indrev.recipes.machines.IRRecipe;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.BitResource;
import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import moe.sylvi.bitexchange.bit.registry.builder.recipe.ItemResourceIngredient;
import moe.sylvi.bitexchange.bit.registry.builder.recipe.RecipeHandler;
import moe.sylvi.bitexchange.bit.registry.builder.recipe.RecipeHandlerOutput;
import moe.sylvi.bitexchange.bit.registry.builder.recipe.ResourceIngredient;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.item.Item;

import java.util.List;

public class IRRecipeHandler<R extends IRRecipe> implements RecipeHandler<R> {

    @Override
    public boolean isAutomatable(R recipe) {
        return false;
    }

    @Override
    public List<ResourceIngredient<?, ?>> getIngredients(R recipe) {
        List<ResourceIngredient<?, ?>> result = Lists.newArrayList();
        for (var input : recipe.getInput()) {
            List<BitResource<Item, ItemBitInfo>> resources = Lists.newArrayList();
            for (var stack : input.getIngredient().getMatchingStacks()) {
                resources.add(BitResource.of(BitRegistries.ITEM, stack.getItem(), input.getCount()));
            }
            result.add(ResourceIngredient.of(resources));
        }
        return result;
    }

    @Override
    public List<RecipeHandlerOutput<?, ?>> getOutputs(R recipe) {
        List<RecipeHandlerOutput<?, ?>> result = Lists.newArrayList();
        for (var output : recipe.getOutputs()) {
            var stack = output.getStack();
            result.add(new RecipeHandlerOutput<>(BitResource.of(BitRegistries.ITEM, stack.getItem(), stack.getCount() * output.getChance())));
        }
        return result;
    }
}
