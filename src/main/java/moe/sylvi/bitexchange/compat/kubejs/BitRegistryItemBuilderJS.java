package moe.sylvi.bitexchange.compat.kubejs;

import dev.latvian.mods.kubejs.KubeJSRegistries;
import dev.latvian.mods.kubejs.platform.IngredientPlatformHelper;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.ListJS;
import dev.latvian.mods.kubejs.util.UtilsJS;
import dev.latvian.mods.rhino.Wrapper;
import dev.latvian.mods.rhino.regexp.NativeRegExp;
import moe.sylvi.bitexchange.BitExchange;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.Recursable;
import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import moe.sylvi.bitexchange.bit.registry.builder.BitRegistryBuilder;
import moe.sylvi.bitexchange.bit.registry.builder.recipe.ItemResourceIngredient;
import moe.sylvi.bitexchange.bit.research.IngredientResearchRequirement;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
import moe.sylvi.bitexchange.bit.research.ResearchTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtString;
import net.minecraft.recipe.Ingredient;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryEntryList;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BitRegistryItemBuilderJS {
    private final List<BuilderAction> actions = new ArrayList<>();
    private final Resource resource;

    private ItemBitInfo cached;

    private double value;
    private long research;
    private boolean researchable;
    private boolean automatable;
    private List<ResearchRequirement> researchRequirements;

    private boolean cancelled = false;

    public BitRegistryItemBuilderJS(Resource resource) {
        this.resource = resource;

        this.value = 0;
        this.research = 1;
        this.researchable = true;
        this.automatable = true;
        this.researchRequirements = new ArrayList<>();
    }

    public Resource getResource() {
        return resource;
    }


    public BitRegistryItemBuilderJS value(double value) {
        actions.add(() -> this.value = value);
        return this;
    }
    public BitRegistryItemBuilderJS research(long research) {
        actions.add(() -> this.research = research);
        return this;
    }
    public BitRegistryItemBuilderJS researchTier(String researchTier) {
        var tier = ResearchTier.byName(researchTier);
        if (tier == null) {
            throw new RecipeExceptionJS("Invalid research tier: " + researchTier);
        }
        actions.add(() -> this.research = tier.getResearch());
        return this;
    }

    public BitRegistryItemBuilderJS researchable(boolean researchable) {
        actions.add(() -> this.researchable = researchable);
        return this;
    }
    public BitRegistryItemBuilderJS researchable() {
        return researchable(true);
    }
    public BitRegistryItemBuilderJS nonResearchable() {
        return researchable(false);
    }

    public BitRegistryItemBuilderJS automatable(boolean automatable) {
        actions.add(() -> this.automatable = automatable);
        return this;
    }
    public BitRegistryItemBuilderJS automatable() {
        return automatable(true);
    }
    public BitRegistryItemBuilderJS nonAutomatable() {
        return automatable(false);
    }

    public BitRegistryItemBuilderJS clearRequirements() {
        actions.add(() -> this.researchRequirements = new ArrayList<>());
        return this;
    }
    public BitRegistryItemBuilderJS require(Ingredient ingredient) {
        actions.add(() -> {
            var requirement = new IngredientResearchRequirement(new ItemResourceIngredient(ingredient));
            this.researchRequirements.add(requirement);
        });
        return this;
    }

    public BitRegistryItemBuilderJS copy(Item item, boolean allowFallback) {
        actions.add(() -> {
            var result = BitRegistries.ITEM.getOrProcess(item, allowFallback);

            if (!result.notNullOrRecursive()) {
                BitExchange.log(Level.INFO, "cancelled!!!");
                BitExchange.log(Level.INFO, "null: " + (result.get() != null));
                BitExchange.log(Level.INFO, "recursive: " + (result.isRecursive()));
                cancelled = true;
                return;
            }

            var info = result.get();

            value = info.getValue();
            research = info.getResearch();
            researchable = info.isResearchable();
            automatable = info.isAutomatable();
            researchRequirements = new ArrayList<>(info.getResearchRequirements());
        });
        return this;
    }
    public BitRegistryItemBuilderJS copy(Item item) {
        return copy(item, false);
    }

    public BitRegistryItemBuilderJS modify() {
        if (resource.items.isEmpty()) {
            return this;
        }
        return copy(resource.items.get(0), true);
    }

    public BitRegistryItemBuilderJS copyValue(Item item, double multiplier) {
        actions.add(() -> {
            var result = BitRegistries.ITEM.getOrProcess(item);

            if (!result.notNullOrRecursive()) {
                cancelled = true;
                return;
            }

            value = result.get().getValue() * multiplier;
        });
        return this;
    }
    public BitRegistryItemBuilderJS copyValue(Item item) {
        return copyValue(item, 1);
    }


    public ItemBitInfo build(Item resource) {
        if (cached != null) {
            return cached.withResource(resource);
        }

        for (var action : actions) {
            action.run();

            if (cancelled) {
                return null;
            }
        }

        cached = new ItemBitInfo(resource, value, research, researchable, automatable, researchRequirements);
        return cached;
    }


    public record Resource(List<Item> items) {

        public static final Resource EMPTY = new Resource(List.of());

        public static Resource of(@Nullable Object o) {
            if (o instanceof Wrapper w) {
                o = w.unwrap();
            }

            if (o == null || o == ItemStack.EMPTY || o == Items.AIR) {
                return EMPTY;
            } else if (o instanceof ItemStack stack) {
                return stack.isEmpty() ? EMPTY : new Resource(List.of(stack.getItem()));
            } else if (o instanceof Ingredient ingr) {
                var items = new ArrayList<Item>();
                for (var stack : ingr.getMatchingStacks()) {
                    items.add(stack.getItem());
                }
                return new Resource(items);
            } else if (o instanceof TagKey itemTag) {
                var entries = Registry.ITEM.getEntryList(itemTag);
                if (entries.isEmpty()) {
                    return EMPTY;
                }
                return new Resource(((RegistryEntryList.Named<Item>)entries.get()).stream().map(RegistryEntry::value).toList());
            } else if (o instanceof Identifier id) {
                var item = KubeJSRegistries.items().get(id);

                if (item == null || item == Items.AIR) {
                    if (RecipeJS.itemErrors) {
                        throw new RecipeExceptionJS("Item '" + id + "' not found!").error();
                    }

                    return EMPTY;
                }

                return new Resource(List.of(item));
            } else if (o instanceof ItemConvertible item) {
                return new Resource(List.of(item.asItem()));
            } else if (o instanceof NbtString tag) {
                return of(tag.asString());
            } else if (o instanceof Pattern || o instanceof NativeRegExp) {
                var reg = UtilsJS.parseRegex(o);

                if (reg != null) {
                    var ingredient = IngredientPlatformHelper.get().regex(reg);
                    return of(ingredient);
                }

                return EMPTY;
            } else if (o instanceof CharSequence) {
                var s = o.toString().trim();

                return new Resource(parse(s));
            }

            var list = ListJS.of(o);

            if (list != null) {
                var items = new ArrayList<Item>();

                for (var obj : list) {
                    items.addAll(of(obj).items);
                }

                return new Resource(items);
            }

            return EMPTY;
        }

        public static List<Item> parse(String s) {
            if (s.isEmpty() || s.equals("-") || s.equals("air") || s.equals("minecraft:air")) {
                return List.of();
            }

            Ingredient ingredient = null;

            if (s.startsWith("#")) {
                ingredient = IngredientPlatformHelper.get().tag(s.substring(1));
            } else if (s.startsWith("@")) {
                ingredient = IngredientPlatformHelper.get().mod(s.substring(1));
            } else if (s.startsWith("%")) {
                var group = UtilsJS.findCreativeTab(s.substring(1));

                if (group == null) {
                    if (RecipeJS.itemErrors) {
                        throw new RecipeExceptionJS("Item group '" + s.substring(1) + "' not found!").error();
                    }

                    return List.of();
                }

                ingredient = IngredientPlatformHelper.get().creativeTab(group);
            }

            if (ingredient == null) {
                var reg = UtilsJS.parseRegex(s);

                if (reg != null) {
                    ingredient = IngredientPlatformHelper.get().regex(reg);
                }
            }

            if (ingredient == null) {
                var item = KubeJSRegistries.items().get(new Identifier(s));

                if (item == null || item == Items.AIR) {
                    if (RecipeJS.itemErrors) {
                        throw new RecipeExceptionJS("Item '" + s + "' not found!").error();
                    }

                    return List.of();
                }

                return List.of(item);
            }

            var items = new ArrayList<Item>();
            for (var stack : ingredient.getMatchingStacks()) {
                items.add(stack.getItem());
            }
            return items;
        }
    }

    @FunctionalInterface
    interface BuilderAction {
        void run();
    }
}
