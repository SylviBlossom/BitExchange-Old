package moe.sylvi.bitexchange.bit.registry.builder;

import com.google.common.collect.Lists;
import moe.sylvi.bitexchange.bit.Recursable;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import moe.sylvi.bitexchange.bit.registry.builder.recipe.RecipeInfo;
import moe.sylvi.bitexchange.bit.registry.builder.recipe.SmithingRecipeInfo;
import moe.sylvi.bitexchange.bit.research.CombinedResearchRequirement;
import moe.sylvi.bitexchange.bit.research.RecipeResearchRequirement;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.Registry;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class RecipeRegistryBuilder implements BitRegistryBuilder<Item, ItemBitInfo> {
    public static final RecipeInfo DEFAULT_INFO = new RecipeInfo();
    private static final HashSet<Recipe<Inventory>> processedRecipes = new HashSet<>();
    private static final HashMap<Item, Double> processedItems = new HashMap<>();
    private static final HashMap<Item, List<Recipe<Inventory>>> recipeMap = new HashMap<>();
    private static final HashMap<RecipeType<?>, RecipeInfo> recipeInfo = new HashMap<>();

    static {
        setRecipeInfo(RecipeType.SMITHING, new SmithingRecipeInfo());
    }

    public static void setRecipeInfo(RecipeType<?> recipeType, RecipeInfo info) {
        recipeInfo.put(recipeType, info);
    }

    public static RecipeInfo getRecipeInfo(Recipe<Inventory> recipe) {
        return recipeInfo.getOrDefault(recipe.getType(), DEFAULT_INFO);
    }

    private final BitRegistry<Item, ItemBitInfo> registry;

    public RecipeRegistryBuilder(BitRegistry<Item, ItemBitInfo> registry) {
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
            List<Recipe<Inventory>> recipes = Lists.newArrayList();
            for (Recipe<Inventory> recipe : recipeMap.get(item)) {
                if (processedRecipes.contains(recipe)) {
                    continue;
                }
                processedRecipes.add(recipe);
                RecipeInfo recipeInfo = getRecipeInfo(recipe);
                Recursable<Double> processed = processItemRecipe(recipe);
                if (processed.isRecursive()) {
                    processedRecipes.remove(recipe);
                    continue;
                }
                if (!recipeInfo.isAutomatable(recipe)) {
                    isResource = false;
                }
                if (smallestBits < 0 || processed.get() < smallestBits) {
                    smallestBits = processed.get();
                }
                recipes.add(recipe);
            }
            if (smallestBits > 0) {
                processedItems.put(item, smallestBits);
                ItemBitInfo infoResult = BitInfo.ofItem(item, smallestBits, 1, isResource);
                List<RecipeResearchRequirement> requirements = Lists.newArrayList();
                for (Recipe<Inventory> recipe : recipes) {
                    RecipeResearchRequirement requirement = new RecipeResearchRequirement(recipe, getRecipeInfo(recipe));
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
        processedRecipes.clear();
        processedItems.clear();
        recipeMap.clear();
    }

    private void mapRecipes(MinecraftServer server, RecipeType recipeType) {
        List<Recipe> list = server.getRecipeManager().listAllOfType(recipeType);
        for (Recipe recipe : list) {
            Item item = recipe.getOutput().getItem();
            if (!recipeMap.containsKey(item)) {
                recipeMap.put(item, Lists.newArrayList((Recipe<Inventory>)recipe));
                registry.prepareResource(item, this);
            } else {
                recipeMap.get(item).add((Recipe<Inventory>)recipe);
            }
        }
    }

    private Recursable<Double> processItemRecipe(Recipe<Inventory> recipe) {
        double finalBits = 0;
        boolean failed = false;
        RecipeInfo info = getRecipeInfo(recipe);
        List<Ingredient> ingredients = info.getIngredients(recipe);
        AtomicBoolean recursed = new AtomicBoolean(false);
        for (Ingredient ingredient : ingredients) {
            ItemStack[] stacks = ingredient.getMatchingStacksClient();
            if (!ingredient.isEmpty() && stacks.length > 0) {
                double smallestBits = 0;
                for (ItemStack itemStack : stacks) {
                    Item item = itemStack.getItem();
                    double newBits = getExactValue(item).consumeRecursive(() -> recursed.set(true));
                    if (item.hasRecipeRemainder()) {
                        newBits = Math.max(0, newBits - Math.max(0, getExactValue(item.getRecipeRemainder())
                                .consumeRecursive(() -> recursed.set(true))));
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
