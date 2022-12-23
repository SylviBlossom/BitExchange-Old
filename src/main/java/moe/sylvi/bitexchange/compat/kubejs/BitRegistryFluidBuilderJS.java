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
import moe.sylvi.bitexchange.bit.info.FluidBitInfo;
import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import moe.sylvi.bitexchange.bit.registry.builder.recipe.ItemResourceIngredient;
import moe.sylvi.bitexchange.bit.research.IngredientResearchRequirement;
import moe.sylvi.bitexchange.bit.research.ListResearchRequirement;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
import moe.sylvi.bitexchange.bit.research.ResearchTier;
import moe.sylvi.bitexchange.mixin.FluidBlockMixin;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
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

public class BitRegistryFluidBuilderJS {
    private final List<BuilderAction> actions = new ArrayList<>();
    private final Resource resource;

    private FluidBitInfo cached;

    private double value;
    private long research;
    private long ratio;
    private boolean researchable;
    private List<ResearchRequirement> researchRequirements;

    private boolean cancelled = false;

    public BitRegistryFluidBuilderJS(Resource resource) {
        this.resource = resource;

        this.value = 0;
        this.research = 1;
        this.ratio = FluidConstants.BUCKET;
        this.researchable = true;
        this.researchRequirements = new ArrayList<>();
    }

    public Resource getResource() {
        return resource;
    }


    public BitRegistryFluidBuilderJS value(double value) {
        actions.add(() -> this.value = value);
        return this;
    }
    public BitRegistryFluidBuilderJS research(long research) {
        actions.add(() -> this.research = research);
        return this;
    }
    public BitRegistryFluidBuilderJS ratio(long ratio) {
        actions.add(() -> this.ratio = ratio);
        return this;
    }
    public BitRegistryFluidBuilderJS researchTier(String researchTier) {
        var tier = ResearchTier.byName(researchTier);
        if (tier == null) {
            throw new RecipeExceptionJS("Invalid research tier: " + researchTier);
        }
        actions.add(() -> this.research = tier.getResearch());
        return this;
    }
    public BitRegistryFluidBuilderJS researchable(boolean researchable) {
        actions.add(() -> this.researchable = researchable);
        return this;
    }
    public BitRegistryFluidBuilderJS researchable() {
        return researchable(true);
    }
    public BitRegistryFluidBuilderJS nonResearchable() {
        return researchable(false);
    }

    public BitRegistryFluidBuilderJS clearRequirements() {
        actions.add(() -> this.researchRequirements = new ArrayList<>());
        return this;
    }
    /*public BitRegistryFluidBuilderJS require(Resource resource) {
        actions.add(() -> {
            var requirements = resource.fluids().stream()
                    .map(fluid -> ResearchRequirement.of(fluid, BitRegistries.FLUID))
                    .toList();
            this.researchRequirements.add(new ListResearchRequirement<>(requirements));
        });
        return this;
    }*/

    public BitRegistryFluidBuilderJS copy(Fluid fluid, boolean allowFallback) {
        actions.add(() -> {
            var result = BitRegistries.FLUID.getOrProcess(fluid, allowFallback);

            if (!result.notNullOrRecursive()) {
                cancelled = true;
                return;
            }

            var info = result.get();

            value = info.getValue();
            research = info.getResearch();
            ratio = info.getRatio();
            researchable = info.isResearchable();
            researchRequirements = new ArrayList<>(info.getResearchRequirements());
        });
        return this;
    }
    public BitRegistryFluidBuilderJS copy(Fluid fluid) {
        return copy(fluid, false);
    }

    public BitRegistryFluidBuilderJS modify() {
        if (resource.fluids.isEmpty()) {
            return this;
        }
        return copy(resource.fluids.get(0), true);
    }

    public BitRegistryFluidBuilderJS copyValue(Fluid fluid, double multiplier) {
        actions.add(() -> {
            var result = BitRegistries.FLUID.getOrProcess(fluid);

            if (!result.notNullOrRecursive()) {
                cancelled = true;
                return;
            }

            value = result.get().getValue() * multiplier;
        });
        return this;
    }
    public BitRegistryFluidBuilderJS copyValue(Fluid fluid) {
        return copyValue(fluid, 1);
    }


    public FluidBitInfo build(Fluid resource) {
        if (cached != null) {
            return cached.withResource(resource);
        }

        for (var action : actions) {
            action.run();

            if (cancelled) {
                return null;
            }
        }

        cached = new FluidBitInfo(resource, value, research, ratio, researchable, researchRequirements);
        return cached;
    }


    public record Resource(List<Fluid> fluids) {

        public static final Resource EMPTY = new Resource(List.of());

        public static Resource of(@Nullable Object o) {
            if (o instanceof Wrapper w) {
                o = w.unwrap();
            }

            if (o == null || o == Fluids.EMPTY) {
                return EMPTY;
            } else if (o instanceof TagKey fluidTag) {
                var entries = Registry.FLUID.getEntryList(fluidTag);
                if (entries.isEmpty()) {
                    return EMPTY;
                }
                return new Resource(((RegistryEntryList.Named<Fluid>)entries.get()).stream().map(RegistryEntry::value).toList());
            } else if (o instanceof Identifier id) {
                var fluid = KubeJSRegistries.fluids().get(id);

                if (fluid == null || fluid == Fluids.EMPTY) {
                    if (RecipeJS.itemErrors) {
                        throw new RecipeExceptionJS("Item '" + id + "' not found!").error();
                    }

                    return EMPTY;
                }

                return new Resource(List.of(fluid));
            } else if (o instanceof FluidBlock block) {
                var fluid = ((FluidBlockMixin) block).bitexchange_getFluid();
                return new Resource(List.of(fluid));
            } else if (o instanceof BlockItem item && item.getBlock() instanceof FluidBlock block) {
                var fluid = ((FluidBlockMixin) block).bitexchange_getFluid();
                return new Resource(List.of(fluid));
            } else if (o instanceof NbtString tag) {
                return of(tag.asString());
            } else if (o instanceof Pattern || o instanceof NativeRegExp) {
                var reg = UtilsJS.parseRegex(o);

                if (reg != null) {
                    return new Resource(matchFluids(reg));
                }

                return EMPTY;
            } else if (o instanceof CharSequence) {
                var s = o.toString().trim();

                return new Resource(parse(s));
            }

            var list = ListJS.of(o);

            if (list != null) {
                var fluids = new ArrayList<Fluid>();

                for (var obj : list) {
                    fluids.addAll(of(obj).fluids);
                }

                return new Resource(fluids);
            }

            return EMPTY;
        }

        public static List<Fluid> parse(String s) {
            if (s.isEmpty() || s.equals("-") || s.equals("empty") || s.equals("minecraft:empty")) {
                return List.of();
            }

            if (s.startsWith("#")) {
                var tagId = new Identifier(s.substring(1));

                var tag = TagKey.of(Registry.FLUID_KEY, tagId);

                var entryList = Registry.FLUID.getEntryList(tag);
                if (entryList.isPresent()) {
                    return entryList.get().stream()
                            .map(RegistryEntry::value)
                            .toList();
                }

            } else if (s.startsWith("@")) {
                var modId = s.substring(1);

                return Registry.FLUID.stream()
                        .filter(fluid -> Registry.FLUID.getId(fluid).getNamespace().equalsIgnoreCase(modId))
                        .toList();

            } else if (s.startsWith("%")) {
                throw new RecipeExceptionJS("'%' operator not supported on fluids").error();
            }

            var reg = UtilsJS.parseRegex(s);
            if (reg != null) {
                return matchFluids(reg);
            }

            var fluid = KubeJSRegistries.fluids().get(new Identifier(s));

            if (fluid == null || fluid == Fluids.EMPTY) {
                if (RecipeJS.itemErrors) {
                    throw new RecipeExceptionJS("Item '" + s + "' not found!").error();
                }

                return List.of();
            }

            return List.of(fluid);
        }

        private static List<Fluid> matchFluids(Pattern pattern) {
            return Registry.FLUID.stream()
                    .filter(fluid -> {
                        var id = Registry.FLUID.getId(fluid).toString();
                        return pattern.matcher(id).find();
                    })
                    .toList();
        }
    }

    @FunctionalInterface
    interface BuilderAction {
        void run();
    }
}
