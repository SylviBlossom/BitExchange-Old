package moe.sylvi.bitexchange.compat.indrev;

import me.steven.indrev.FabricRecipeRemainder;
import moe.sylvi.bitexchange.BitExchange;
import moe.sylvi.bitexchange.bit.BitResource;
import moe.sylvi.bitexchange.bit.registry.builder.recipe.ResourceIngredient;
import moe.sylvi.bitexchange.bit.registry.builder.recipe.SimpleRecipeHandler;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import org.apache.logging.log4j.Level;

public class IRSelfRemainderRecipeHandler extends SimpleRecipeHandler {
    @Override
    public BitResource<?, ?> getRemainder(Recipe<Inventory> recipe, ResourceIngredient<?, ?> ingredient, BitResource<?, ?> resource) {
        if (resource.getResource() instanceof FabricRecipeRemainder remainder) {
            try {
                var stack = remainder.getRemainder(new ItemStack((Item) resource.getResource(), 1), null, null);
                return BitResource.fromStack(stack);
            } catch (Exception ignored) { }
        }
        return super.getRemainder(recipe, ingredient, resource);
    }
}
