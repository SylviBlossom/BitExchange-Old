package moe.sylvi.bitexchange.mixin;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DrawableHelper.class)
public interface DrawableHelperMixin {
    @Invoker("drawTexturedQuad")
    static void bitexchange_drawTexturedQuad(Matrix4f matrices, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1) {
        throw new AssertionError();
    }
}
