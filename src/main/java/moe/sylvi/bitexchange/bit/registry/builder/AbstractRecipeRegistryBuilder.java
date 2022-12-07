package moe.sylvi.bitexchange.bit.registry.builder;

import com.google.common.collect.Lists;
import me.shedaniel.autoconfig.AutoConfig;
import moe.sylvi.bitexchange.BitConfig;
import moe.sylvi.bitexchange.BitExchange;
import moe.sylvi.bitexchange.bit.BitHelper;
import moe.sylvi.bitexchange.bit.BitResource;
import moe.sylvi.bitexchange.bit.Recursable;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.info.BitInfoResearchable;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import moe.sylvi.bitexchange.bit.registry.SimpleBitRegistry;
import moe.sylvi.bitexchange.bit.registry.builder.recipe.*;
import moe.sylvi.bitexchange.bit.research.CombinedResearchRequirement;
import moe.sylvi.bitexchange.bit.research.RecipeResearchRequirement;
import net.minecraft.recipe.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Level;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public abstract class AbstractRecipeRegistryBuilder<R, I extends BitInfo<R>> implements BitRegistryBuilder<R, I> {
    public static final RecipeHandler<?> DEFAULT_HANDLER = new SimpleRecipeHandler();
    private static final HashMap<RecipeType<?>, RecipeHandler<?>> recipeHandlers = new HashMap<>();
    private static final List<Function<Recipe<?>, RecipeHandler<?>>> recipeHandlerGetters = Lists.newArrayList();

    static {
        registerHandler(RecipeType.SMITHING, new SmithingRecipeHandler());
    }

    public static void registerHandler(RecipeType<?> recipeType, RecipeHandler<?> handler) {
        recipeHandlers.put(recipeType, handler);
    }
    public static void registerHandlerGetter(Function<Recipe<?>, RecipeHandler<?>> getter) {
        recipeHandlerGetters.add(getter);
    }

    public static RecipeHandler getRecipeHandler(Recipe<?> recipe) {
        for (var getter : recipeHandlerGetters) {
            var handler = getter.apply(recipe);
            if (handler != null) {
                return handler;
            }
        }
        return recipeHandlers.getOrDefault(recipe.getType(), DEFAULT_HANDLER);
    }

    private final HashMap<RecipeOutput, Double> processedRecipes = new HashMap<>();
    private final HashMap<R, Double> processedItems = new HashMap<>();
    private final HashMap<R, List<RecipeOutput>> recipeMap = new HashMap<>();

    private final BitRegistry<R, I> registry;

    public AbstractRecipeRegistryBuilder(BitRegistry<R, I> registry) {
        this.registry = registry;
    }

    abstract Class<R> getResourceClass();

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

    abstract boolean shouldProcess(R resource, Recipe<?> recipe);

    @Override
    public I process(R resource) {
        if (!recipeMap.containsKey(resource)) {
            return null;
        }
        Recipe<?> smallestRecipe = null;
        double smallestBits = -1;
        List<Recipe<?>> recipes = Lists.newArrayList();
        for (RecipeOutput recipeOutput : recipeMap.get(resource)) {
            var recipe = recipeOutput.recipe;
            double value;
            if (processedRecipes.containsKey(recipeOutput)) {
                value = processedRecipes.get(recipeOutput);
            } else {
                //IRecipeHandler recipeHandler = getRecipeHandler(recipe);
                Recursable<Double> processed = processItemRecipe(recipe, recipeOutput.resource);
                if (processed.isRecursive()) {
                    continue;
                }
                processedRecipes.put(recipeOutput, processed.get());
                value = processed.get();
            }
            if (smallestBits < 0 || (value > 0 && value < smallestBits)) {
                smallestBits = value;
                smallestRecipe = recipe;
            }
            recipes.add(recipe);
        }
        if (smallestBits <= 0) {
            if (SimpleBitRegistry.DEBUGGING) {
                BitExchange.log(Level.INFO, "[DEBUG] No valid recipe found");
            }
            return null;
        }
        if (SimpleBitRegistry.DEBUGGING) {
            BitExchange.log(Level.INFO, "[DEBUG] Final recipe: " + smallestRecipe.getId().toString());
        }
        processedItems.put(resource, smallestBits);
        I infoResult = createInfo(resource, smallestBits, smallestRecipe);
        if (infoResult instanceof BitInfoResearchable researchInfo) {
            List<RecipeResearchRequirement> requirements = Lists.newArrayList();
            for (Recipe<?> recipe : recipes) {
                RecipeResearchRequirement requirement = new RecipeResearchRequirement(recipe, getRecipeHandler(recipe));
                if (!requirements.contains(requirement)) {
                    requirements.add(requirement);
                }
            }
            if (!requirements.isEmpty()) {
                researchInfo.addRequiredResearch(new CombinedResearchRequirement<>(requirements));
            }
        }
        return infoResult;
    }

    public abstract I createInfo(R resource, double bits, Recipe<?> smallestRecipe);

    @Override
    public void postProcess() {
        //processingRecipes.clear();
        processedRecipes.clear();
        processedItems.clear();
        recipeMap.clear();
    }

    private void mapRecipes(MinecraftServer server, RecipeType recipeType) {
        var config = AutoConfig.getConfigHolder(BitConfig.class).getConfig();

        // Check if the recipe type is disabled in the config
        var recipeTypeId = Registry.RECIPE_TYPE.getId(recipeType);
        if (recipeTypeId != null && config.blacklistedRecipeTypes.contains(recipeTypeId.toString())) {
            return;
        }

        List<Recipe> list = server.getRecipeManager().listAllOfType(recipeType);
        for (Recipe recipe : list) {

            // Ignore this recipe if it's disabled in the config
            if (config.blacklistedRecipes.contains(recipe.getId().toString())) {
                continue;
            }

            // Only recipes with valid recipe handlers are supported
            var handler = getRecipeHandler(recipe);
            if (handler == null) {
                continue;
            }

            // Process values for each recipe output (modded recipes can have multiple)
            for (var output : handler.getOutputs(recipe)) {

                // Only process output if it's the correct class for the builder (e.g. Item for ItemRecipeRegistryBuilder)
                var resourceClass = getResourceClass();
                if (!resourceClass.isInstance(((RecipeHandlerOutput)output).resource.getResource())) {
                    continue;
                }
                var resource = resourceClass.cast(((RecipeHandlerOutput)output).resource.getResource());

                // Check if the item is disabled in the config
                if (config.blacklistedItems.contains(BitHelper.getItemId(resource, registry))) {
                    continue;
                }

                // Overridable check for processing
                if (!shouldProcess(resource, recipe)) {
                    continue;
                }

                // Mark the recipe and the current output for processing
                var recipeOutput = new RecipeOutput(((RecipeHandlerOutput)output).resource, (Recipe<?>) recipe);
                if (!recipeMap.containsKey(resource)) {
                    recipeMap.put(resource, Lists.newArrayList(recipeOutput));
                    registry.prepareResource(resource, this);
                } else {
                    recipeMap.get(resource).add(recipeOutput);
                }
            }
        }
    }

    private Recursable<Double> processItemRecipe(Recipe<?> recipe, BitResource<?,?> output) {
        double finalBits = 0;
        boolean failed = false;
        RecipeHandler handler = getRecipeHandler(recipe);
        List<ResourceIngredient<?,?>> ingredients = handler.getIngredients(recipe);
        AtomicBoolean recursed = new AtomicBoolean(false);
        for (var ingredient : ingredients) {
            double smallestBits = 0;
            boolean foundFirst = false;
            boolean noValue = true;
            for (var resource : ingredient.getResources()) {
                double newBits = getExactValue(resource).consumeRecursive(() -> recursed.set(true));
                if (recursed.get()) {
                    break;
                }
                if (newBits == 0) {
                    continue;
                }
                var remainder = handler.getRemainder(recipe, ingredient, resource);
                if (remainder != null) {
                    var remainderBits = getExactValue(remainder).consumeRecursive(() -> recursed.set(true));
                    if (recursed.get()) {
                        break;
                    }
                    if (remainderBits == 0) {
                        continue;
                    }
                    newBits -= remainderBits;
                }
                noValue = false;
                if (!foundFirst) {
                    smallestBits = newBits;
                    foundFirst = true;
                } else {
                    smallestBits = Math.min(smallestBits, newBits);
                }
            }
            if (recursed.get() || noValue) {
                failed = true;
                break;
            }
            finalBits += smallestBits;
        }
        if (!failed && finalBits > 0) {
            var myCount = 0.0;
            var totalCount = 0.0;
            var remainingBits = finalBits;
            for(var otherOutput : handler.getOutputs(recipe)) {
                var recipeOutput = ((RecipeHandlerOutput)otherOutput);
                Recursable<BitInfo> targetInfo = Recursable.of(null, false);
                if (recipeOutput.resource.getResource() == output.getResource()) {
                    myCount += recipeOutput.resource.getAmount() / ((Long)recipeOutput.ratio.orElse(1L));
                } else {
                    targetInfo = recipeOutput.resource.getOrProcessInfo();
                }
                if (targetInfo.notNullOrRecursive()) {
                    remainingBits -= targetInfo.get().getValue(recipeOutput.resource.getAmount());
                } else {
                    totalCount += recipeOutput.resource.getAmount() / ((Long)recipeOutput.ratio.orElse(1L));
                }
            }
            return Recursable.of((remainingBits * (myCount / totalCount)) / myCount, recursed.get());
        }
        return Recursable.of(-1.0, recursed.get());
    }

    private Recursable<Double> getExactValue(BitResource resource) {
        if (processedItems.containsKey(resource)) {
            return Recursable.of(processedItems.get(resource), false);
        } else {
            Recursable result = resource.getOrProcessInfo();
            if (result.get() != null && result.get() instanceof BitInfo info) {
                return result.into(info.getValue(resource.getAmount()));
            } else {
                return result.into(0.0);
            }
        }
    }

    record RecipeOutput(BitResource<?,?> resource, Recipe<?> recipe) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RecipeOutput that = (RecipeOutput) o;
            return Objects.equals(resource, that.resource) && Objects.equals(recipe, that.recipe);
        }

        @Override
        public int hashCode() {
            return Objects.hash(resource, recipe);
        }
    }
}
