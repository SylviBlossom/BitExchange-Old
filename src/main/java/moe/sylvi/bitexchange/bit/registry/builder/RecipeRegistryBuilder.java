package moe.sylvi.bitexchange.bit.registry.builder;

import com.google.common.collect.Lists;
import moe.sylvi.bitexchange.bit.BitResource;
import moe.sylvi.bitexchange.bit.Recursable;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import moe.sylvi.bitexchange.bit.registry.builder.recipe.IRecipeHandler;
import moe.sylvi.bitexchange.bit.registry.builder.recipe.SimpleRecipeHandler;
import moe.sylvi.bitexchange.bit.registry.builder.recipe.SmithingRecipeHandler;
import moe.sylvi.bitexchange.bit.research.CombinedResearchRequirement;
import moe.sylvi.bitexchange.bit.research.RecipeResearchRequirement;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.recipe.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class RecipeRegistryBuilder<R, I extends BitInfo<R>> implements BitRegistryBuilder<R, I> {
    public static final IRecipeHandler<?,?> DEFAULT_HANDLER = new SimpleRecipeHandler();
    private static final HashMap<RecipeType<?>, IRecipeHandler<?,?>> recipeHandlers = new HashMap<>();

    static {
        registerHandler(RecipeType.SMITHING, new SmithingRecipeHandler());
    }

    public static void registerHandler(RecipeType<?> recipeType, IRecipeHandler<?,?> handler) {
        recipeHandlers.put(recipeType, handler);
    }

    public static IRecipeHandler<?,?> getRecipeHandler(Recipe<?> recipe) {
        return recipeHandlers.getOrDefault(recipe.getType(), DEFAULT_HANDLER);
    }

    private final HashMap<Recipe<?>, Double> processedRecipes = new HashMap<>();
    private final HashMap<R, Double> processedItems = new HashMap<>();
    private final HashMap<R, List<Recipe<?>>> recipeMap = new HashMap<>();

    private final BitRegistry<R, I> registry;

    public RecipeRegistryBuilder(BitRegistry<R, I> registry) {
        this.registry = registry;
    }

    @Override
    public int getPriority() {
        return ItemPriorities.RECIPES;
    }

    @Override
    public void prepare(MinecraftServer server) {
        for (RecipeType<?> recipeType : Registry.RECIPE_TYPE) {
            mapRecipes(server, recipeType);
        }
    }

    @Override
    public ItemBitInfo process(Item item) {
        if (recipeMap.containsKey(item)) {
            double smallestBits = -1;
            boolean isResource = true;
            List<Recipe<?>> recipes = Lists.newArrayList();
            for (Recipe<?> recipe : recipeMap.get(item)) {
                IRecipeHandler recipeHandler = getRecipeHandler(recipe);
                Recursable<Double> processed = processItemRecipe(recipe);
                if (processed.isRecursive()) {
                    continue;
                }
                //processedRecipes.put(recipe, processed.get());
                double value = processed.get();
                if (smallestBits < 0 || (value > 0 && value < smallestBits)) {
                    smallestBits = value;
                    isResource = recipeHandler.isAutomatable(recipe);
                }
                recipes.add(recipe);
            }
            if (smallestBits > 0) {
                processedItems.put(item, smallestBits);
                ItemBitInfo infoResult = BitInfo.ofItem(item, smallestBits, 1, true, isResource);
                List<RecipeResearchRequirement> requirements = Lists.newArrayList();
                for (Recipe<Inventory> recipe : recipes) {
                    RecipeResearchRequirement requirement = new RecipeResearchRequirement(recipe, getRecipeHandler(recipe));
                    if (!requirements.contains(requirement)) {
                        requirements.add(requirement);
                    }
                }
                if (!requirements.isEmpty()) {
                    infoResult.addRequiredResearch(CombinedResearchRequirement.of(requirements));
                }
                return infoResult;
            }
        }
        return null;
    }

    @Override
    public void postProcess() {
        //processingRecipes.clear();
        processedRecipes.clear();
        processedItems.clear();
        recipeMap.clear();
    }

    private void mapRecipes(MinecraftServer server, RecipeType recipeType) {
        List<Recipe> list = server.getRecipeManager().listAllOfType(recipeType);
        for (Recipe recipe : list) {
            Item item = recipe.getOutput().getItem();
            if (!recipeMap.containsKey(item)) {
                recipeMap.put(item, Lists.newArrayList((Recipe<?>)recipe));
                registry.prepareResource(item, this);
            } else {
                recipeMap.get(item).add((Recipe<?>)recipe);
            }
        }
    }

    private Recursable<Double> processItemRecipe(Recipe<?> recipe) {
        double finalBits = 0;
        boolean failed = false;
        IRecipeHandler handler = getRecipeHandler(recipe);
        List<List<BitResource>> ingredients = handler.getIngredients(recipe);
        AtomicBoolean recursed = new AtomicBoolean(false);
        for (var options : ingredients) {
            double smallestBits = 0;
            for (var resource : options) {
                Item item = itemStack.getItem();
                double newBits = getExactValue(item).consumeRecursive(() -> recursed.set(true)) * itemStack.getCount();
                if (item.hasRecipeRemainder()) {
                    newBits = Math.max(0, newBits - Math.max(0, getExactValue(item.getRecipeRemainder())
                            .consumeRecursive(() -> recursed.set(true)) * itemStack.getCount()));
                }
                if (recursed.get()) {
                    break;
                }
                if (smallestBits == 0) {
                    smallestBits = newBits;
                } else if (newBits > 0) {
                    smallestBits = Math.min(smallestBits, newBits);
                }
            }
            if (recursed.get()) {
                failed = true;
                break;
            }
            if (smallestBits == 0) {
                failed = true;
                break;
            }
            finalBits += smallestBits;
        }
        if (!failed && finalBits > 0) {
            int count = recipe.getOutput().getCount();
            return Recursable.of(finalBits / count, recursed.get());
        }
        return Recursable.of(-1.0, recursed.get());
    }

    private Recursable<Double> getExactValue(Item item) {
        if (processedItems.containsKey(item)) {
            return Recursable.of(processedItems.get(item), false);
        } else {
            Recursable<ItemBitInfo> result = registry.getOrProcess(item);
            if (result.get() != null) {
                return result.into(result.get().getValue());
            } else {
                return result.into(0.0);
            }
        }
    }
}
