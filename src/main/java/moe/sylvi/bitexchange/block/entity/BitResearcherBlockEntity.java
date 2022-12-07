package moe.sylvi.bitexchange.block.entity;

import moe.sylvi.bitexchange.BitComponents;
import moe.sylvi.bitexchange.BitExchange;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.research.ResearchableItem;
import moe.sylvi.bitexchange.inventory.ImplementedInventory;
import moe.sylvi.bitexchange.screen.BitResearcherScreenHandler;
import moe.sylvi.bitexchange.transfer.FullInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public class BitResearcherBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, SidedInventory, ImplementedInventory {
    public UUID owner;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

    public BitResearcherBlockEntity(BlockPos pos, BlockState state) {
        super(BitExchange.BIT_RESEARCHER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new BitResearcherScreenHandler(syncId, inv, this);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        Inventories.readNbt(tag, this.inventory);
        if (tag.contains("Owner")) {
            owner = tag.getUuid("Owner");
        }
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        Inventories.writeNbt(tag, this.inventory);
        if (owner != null) {
            tag.putUuid("Owner", this.owner);
        }
        super.writeNbt(tag);
    }

    public static void tick(World world, BlockPos pos, BlockState state, BitResearcherBlockEntity entity) {
        if (!world.isClient) {
            PlayerEntity player = entity.getOwner();
            if (player != null) {
                var storage = FullInventoryStorage.of(entity);
                var context = ContainerItemContext.ofSingleSlot(storage.getSlot(0));
                var fluidStorage = context.find(FluidStorage.ITEM);
                if (fluidStorage != null && fluidStorage.supportsExtraction()) {
                    var fluidComponent = BitComponents.FLUID_KNOWLEDGE.get(player);
                    try (Transaction transaction = Transaction.openOuter()) {
                        var success = false;
                        for (var view : fluidStorage) {
                            if (view.isResourceBlank()) {
                                continue;
                            }
                            var fluid = view.getResource().getFluid();
                            if (fluidComponent.canLearn(fluid)) {
                                var knowledge = fluidComponent.getKnowledge(fluid);
                                var maxKnowledge = BitRegistries.FLUID.getResearch(fluid);
                                if (knowledge < maxKnowledge) {
                                    long extracted;
                                    try (Transaction nested = transaction.openNested()) {
                                        var toExtract = Math.min(view.getAmount(), maxKnowledge - knowledge);
                                        extracted = view.extract(view.getResource(), toExtract, nested);
                                    }
                                    BitExchange.log(Level.INFO, "Extracted: " + extracted);
                                    var added = fluidComponent.addKnowledge(fluid, extracted);
                                    if (added > 0) {
                                        view.extract(view.getResource(), added, transaction);
                                        if (fluidComponent.hasLearned(fluid)) {
                                            var hover = fluid.getDefaultState().getBlockState().getBlock().getName().formatted(Formatting.WHITE);
                                            player.sendMessage(Text.literal("Researched fluid: ").formatted(Formatting.LIGHT_PURPLE).append(hover), false);
                                        }
                                        success = true;
                                    }
                                }
                            }
                        }
                        if (success) {
                            transaction.commit();
                        }
                    }
                }
                ItemStack input = entity.getStack(0);
                if (!input.isEmpty()) {
                    Item item = input.getItem();
                    if (item instanceof ResearchableItem researchableItem) {
                        var owner = entity.getOwner();
                        if (researchableItem.canResearch(input, owner) && !researchableItem.hasResearched(input, owner)) {
                            entity.setStack(0, researchableItem.research(input, owner));
                            entity.markDirty();
                        }
                    }
                    var component = BitComponents.ITEM_KNOWLEDGE.get(player);
                    if (component.canLearn(item)) {
                        long knowledge = component.getKnowledge(item);
                        if (knowledge < BitRegistries.ITEM.getResearch(item)) {
                            int count = (int) component.addKnowledge(item, input.getCount());
                            input.decrement(count);
                            if (component.hasLearned(item)) {
                                player.sendMessage(Text.literal("Researched item: ").formatted(Formatting.LIGHT_PURPLE).append(item.getDefaultStack().toHoverableText()), false);
                            }
                            entity.setStack(0, input);
                            entity.markDirty();
                        }
                    }
                }
            }
        }
    }

    private PlayerEntity getOwner() {
        return this.owner != null ? world.getPlayerByUuid(this.owner) : null;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[] { 0 };
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        var item = stack.getItem();
        if (item instanceof ResearchableItem researchableItem) {
            return researchableItem.canResearch(stack, getOwner());
        } else {
            var info = BitRegistries.ITEM.get(item);
            return info != null && info.isResearchable() && info.getResearch() > 0;
        }
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        var item = stack.getItem();
        if (item instanceof ResearchableItem researchableItem) {
            return researchableItem.hasResearched(stack, getOwner());
        } else {
            PlayerEntity player = getOwner();
            return player != null && BitComponents.ITEM_KNOWLEDGE.get(player).hasLearned(stack.getItem());
        }
    }
}
