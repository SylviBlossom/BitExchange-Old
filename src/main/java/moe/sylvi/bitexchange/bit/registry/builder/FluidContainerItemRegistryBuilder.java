package moe.sylvi.bitexchange.bit.registry.builder;

import com.google.common.collect.Lists;
import me.shedaniel.autoconfig.AutoConfig;
import moe.sylvi.bitexchange.BitConfig;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.Recursable;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import moe.sylvi.bitexchange.bit.info.FluidBitInfo;
import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
import moe.sylvi.bitexchange.bit.research.ResearchTier;
import moe.sylvi.bitexchange.transfer.PreviewContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FluidContainerItemRegistryBuilder implements BitRegistryBuilder<Item, ItemBitInfo> {
    private static final HashMap<Item, Pair<ContainerItemContext, Storage<FluidVariant>>> fluidStorages = new HashMap<>();
    private final BitRegistry<Item, ItemBitInfo> registry;

    public FluidContainerItemRegistryBuilder(BitRegistry<Item, ItemBitInfo> registry) {
        this.registry = registry;
    }

    @Override
    public int getPriority() {
        return ItemPriorities.BELOW_DATA;
    }

    @Override
    public void prepare(MinecraftServer server) {
        var config = AutoConfig.getConfigHolder(BitConfig.class).getConfig();

        for (Item item : Registry.ITEM) {
            ItemStack stack = item.getDefaultStack();

            if (stack.isEmpty()) {
                continue;
            }

            // Dont process fluid containers that are blacklisted in the config
            if (config.blacklistedItems.contains(Registry.ITEM.getId(item).toString())) {
                continue;
            }

            // Attempt to get the fluid storage for the item
            ContainerItemContext context = new PreviewContainerItemContext(stack, server.getWorld(World.OVERWORLD));
            Storage<FluidVariant> storage = FluidStorage.ITEM.find(item.getDefaultStack(), context);

            if (storage == null) {
                continue;
            }

            FluidVariant resource = StorageUtil.findStoredResource(storage);

            // If fluid exists, mark it for processing
            if (resource != null && !resource.isBlank()) {
                fluidStorages.put(item, new Pair<>(context, storage));
                registry.prepareResource(item, this);
            }
        }
    }

    @Override
    public ItemBitInfo process(Item item) {
        Pair<ContainerItemContext, Storage<FluidVariant>> pair = fluidStorages.get(item);

        ContainerItemContext context = pair.getLeft();
        Storage<FluidVariant> storage = pair.getRight();

        boolean success = true;
        double bits = 0.0;
        List<ResearchRequirement> requirements = new ArrayList<>();
        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<FluidVariant> view : storage) {
                FluidVariant resource = view.getResource();
                if (resource == null || resource.isBlank()) {
                    continue;
                }
                long amount = view.getAmount();
                if (storage.supportsExtraction()) {
                    view.extract(resource, view.getAmount(), transaction);
                }
                Recursable<FluidBitInfo> bitResult = BitRegistries.FLUID.getOrProcess(resource.getFluid());
                if (!bitResult.notNullOrRecursive()) {
                    success = false;
                    break;
                }
                bits += (amount * bitResult.get().getValue()) / FluidConstants.BUCKET;
                var requirement = bitResult.get().createResearchRequirement();
                if (!requirements.contains(requirement)) {
                    requirements.add(requirement);
                }
            }
            if (success) {
                if (storage.supportsExtraction()) {
                    for (SingleSlotStorage<ItemVariant> slot : context.getAdditionalSlots()) {
                        if (slot.getResource() == null || slot.getResource().isBlank()) {
                            continue;
                        }
                        Recursable<ItemBitInfo> result = registry.getOrProcess(slot.getResource().getItem());
                        if (!result.notNullOrRecursive()) {
                            success = false;
                            break;
                        }
                        bits += result.get().getValue();
                        ResearchRequirement requirement = result.get().createResearchRequirement();
                        if (!requirements.contains(requirement)) {
                            requirements.add(requirement);
                        }
                    }
                } else if (item.hasRecipeRemainder()) {
                    Recursable<ItemBitInfo> bitResult = registry.getOrProcess(item.getRecipeRemainder());
                    if (bitResult.notNullOrRecursive()) {
                        bits += bitResult.get().getValue();
                        ResearchRequirement requirement = bitResult.get().createResearchRequirement();
                        if (!requirements.contains(requirement)) {
                            requirements.add(requirement);
                        }
                    } else {
                        success = false;
                    }
                }
            }
        }

        if (success) {
            return BitInfo.ofItem(item, bits, ResearchTier.CRAFTED.getResearch(), true, false, requirements);
        } else {
            return null;
        }
    }

    @Override
    public void postProcess() {
        fluidStorages.clear();
    }
}
