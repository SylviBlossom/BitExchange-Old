package moe.sylvi.bitexchange.compat.modern_industrialization;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import moe.sylvi.bitexchange.bit.registry.builder.AbstractRecipeRegistryBuilder;

public class MICompat {
    public static void load() {
        for (var recipeType : MIMachineRecipeTypes.getRecipeTypes()) {
            AbstractRecipeRegistryBuilder.registerHandler(recipeType, new MIRecipeHandler());
        }
    }
}
