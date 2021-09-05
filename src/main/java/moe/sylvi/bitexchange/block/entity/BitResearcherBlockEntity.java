package moe.sylvi.bitexchange.block.entity;

import moe.sylvi.bitexchange.BitComponents;
import moe.sylvi.bitexchange.BitExchange;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.component.BitKnowledgeComponent;
import moe.sylvi.bitexchange.inventory.ImplementedInventory;
import moe.sylvi.bitexchange.screen.BitResearcherScreenHandler;
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
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
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
        return new TranslatableText(getCachedState().getBlock().getTranslationKey());
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
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        Inventories.writeNbt(tag, this.inventory);
        if (owner != null) {
            tag.putUuid("Owner", this.owner);
        }
        return tag;
    }

    public static void tick(World world, BlockPos pos, BlockState state, BitResearcherBlockEntity entity) {
        if (!world.isClient) {
            PlayerEntity player = entity.getOwner();
            if (player != null) {
                ItemStack input = entity.getStack(0);
                if (!input.isEmpty()) {
                    Item item = input.getItem();
                    BitKnowledgeComponent<Item> component = BitComponents.ITEM_KNOWLEDGE.get(player);
                    if (component.canLearn(item)) {
                        long knowledge = component.getKnowledge(item);
                        if (knowledge < BitRegistries.ITEM.getResearch(item)) {
                            int count = (int) component.addKnowledge(item, input.getCount());
                            input.decrement(count);
                            if (component.getLearned(item)) {
                                player.sendMessage(new LiteralText("Researched item: ").formatted(Formatting.LIGHT_PURPLE).append(item.getDefaultStack().toHoverableText()), false);
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
        return BitRegistries.ITEM.getResearch(stack.getItem()) > 0;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        PlayerEntity player = getOwner();
        return player != null && BitComponents.ITEM_KNOWLEDGE.get(player).getLearned(stack.getItem());
    }
}
