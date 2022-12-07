package moe.sylvi.bitexchange.compat.indrev;

import me.steven.indrev.recipes.SelfRemainderRecipe;
import me.steven.indrev.recipes.machines.IRRecipeType;
import moe.sylvi.bitexchange.bit.registry.builder.AbstractRecipeRegistryBuilder;
import net.minecraft.util.registry.Registry;

public class IRCompat {
    public static void load() {
        AbstractRecipeRegistryBuilder.registerHandlerGetter(recipe -> {
            if (recipe instanceof SelfRemainderRecipe) {
                return new IRSelfRemainderRecipeHandler();
            }
            return null;
        });
        for (var recipeType : Registry.RECIPE_TYPE) {
            if (recipeType instanceof IRRecipeType irRecipeType) {
                AbstractRecipeRegistryBuilder.registerHandler(irRecipeType, new IRRecipeHandler<>());
            }
        }
    }
}
