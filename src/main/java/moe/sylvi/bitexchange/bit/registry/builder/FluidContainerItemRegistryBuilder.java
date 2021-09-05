package moe.sylvi.bitexchange.bit.registry.builder;

import com.google.common.collect.Lists;
import moe.sylvi.bitexchange.BitExchange;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.Recursable;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import moe.sylvi.bitexchange.bit.info.FluidBitInfo;
import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
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
import org.apache.logging.log4j.Level;

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
        for (Item item : Registry.ITEM) {
            ItemStack stack = item.getDefaultStack();

            if (stack.isEmpty()) {
                continue;
            }

            ContainerItemContext context = new PreviewContainerItemContext(stack, server.getWorld(World.OVERWORLD));
            Storage<FluidVariant> storage = FluidStorage.ITEM.find(item.getDefaultStack(), context);

            if (storage == null) {
                continue;
            }

            FluidVariant resource = StorageUtil.findStoredResource(storage, null);

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

        BitExchange.log(Level.INFO, "Processing fluid container: " + item.getName().getString());

        boolean success = true;
        double bits = 0.0;
        boolean automatable = true;
        List<ResearchRequirement> requirements = Lists.newArrayList();
        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<FluidVariant> view : storage.iterable(transaction)) {
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
            }
            BitExchange.log(Level.INFO, "Stage 1: " + (success ? "Passed" : "Failed"));
            if (success) {
                if (storage.supportsExtraction()) {
                    for (SingleSlotStorage<ItemVariant> slot : context.getAdditionalSlots()) {
                        if (slot.getResource() == null || slot.getResource().isBlank()) {
                            continue;
                        }
                        Recursable<ItemBitInfo> result = registry.getOrProcess(slot.getResource().getItem());
                        if (!result.notNullOrRecursive()) {
                            BitExchange.log(Level.INFO, "Stage 2 [Extraction]: Null or recursive result for " + slot.getResource().getItem().getName().getString());
                            success = false;
                            break;
                        }
                        bits += result.get().getValue();
                        automatable = automatable && result.get().isAutomatable();
                        ResearchRequirement requirement = result.get().createResearchRequirement();
                        if (!requirements.contains(requirement)) {
                            requirements.add(requirement);
                        }
                    }
                } else if (item.hasRecipeRemainder()) {
                    Recursable<ItemBitInfo> bitResult = registry.getOrProcess(item.getRecipeRemainder());
                    if (bitResult.notNullOrRecursive()) {
                        bits += bitResult.get().getValue();
                        automatable = bitResult.get().isAutomatable();
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
            return BitInfo.ofItem(item, bits, 1, automatable, requirements);
        } else {
            return null;
        }
    }

    @Override
    public void postProcess() {
        fluidStorages.clear();
    }
}
