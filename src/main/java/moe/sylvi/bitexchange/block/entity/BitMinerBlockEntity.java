package moe.sylvi.bitexchange.block.entity;

import moe.sylvi.bitexchange.BitExchange;
import moe.sylvi.bitexchange.block.BitMinerBlock;
import moe.sylvi.bitexchange.inventory.ImplementedInventory;
import moe.sylvi.bitexchange.screen.BitMinerScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BitMinerBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, SidedInventory, ImplementedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private int miningProgress;

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return miningProgress;
        }

        @Override
        public void set(int index, int value) {
            miningProgress = value;
        }

        @Override
        public int size() {
            return 1;
        }
    };

    public BitMinerBlockEntity(BlockPos pos, BlockState state) {
        super(BitExchange.BIT_MINER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new BitMinerScreenHandler(syncId, inv, this, propertyDelegate);
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        Inventories.readNbt(tag, this.inventory);
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        Inventories.writeNbt(tag, this.inventory);
        super.writeNbt(tag);
    }

    public static void tick(World world, BlockPos pos, BlockState state, BitMinerBlockEntity entity) {
        if (!world.isClient) {
            ItemStack stack = entity.getStack(0);
            if (stack.isEmpty() || stack.getCount() < stack.getMaxCount()) {
                entity.miningProgress++;
                if (entity.miningProgress >= entity.getMiningSpeed()) {
                    if (stack.isEmpty()) {
                        entity.setStack(0, entity.getMiningOutput().getDefaultStack());
                    } else {
                        stack.increment(1);
                        entity.setStack(0, stack);
                    }
                    entity.markDirty();
                    entity.miningProgress = 0;
                }
            } else {
                entity.miningProgress = 0;
            }
        }
    }

    public int getMiningSpeed() {
        return ((BitMinerBlock)getCachedState().getBlock()).speed;
    }

    public Item getMiningOutput() {
        return ((BitMinerBlock)getCachedState().getBlock()).output;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[] { 0 };
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeInt(getMiningSpeed());
    }
}
