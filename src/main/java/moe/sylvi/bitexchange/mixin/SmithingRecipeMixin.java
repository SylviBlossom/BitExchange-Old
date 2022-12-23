package moe.sylvi.bitexchange.mixin;

import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.SmithingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SmithingRecipe.class)
public interface SmithingRecipeMixin {
    @Accessor("base")
    Ingredient bitexchange_getBase();

    @Accessor("addition")
    Ingredient bitexchange_getAddition();
}
