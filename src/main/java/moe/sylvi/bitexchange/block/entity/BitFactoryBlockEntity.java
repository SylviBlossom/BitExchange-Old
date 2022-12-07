package moe.sylvi.bitexchange.block.entity;

import moe.sylvi.bitexchange.BitExchange;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.inventory.block.IBitFactoryBlockInventory;
import moe.sylvi.bitexchange.screen.BitFactoryScreenHandler;
import moe.sylvi.bitexchange.transfer.BitFluidStorage;
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
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BitFactoryBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, SidedInventory, IBitFactoryBlockInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(getDefaultInventorySize(), ItemStack.EMPTY);
    private final BitFluidStorage inputFluid = new BitFluidStorage();

    public BitFactoryBlockEntity(BlockPos pos, BlockState state) {
        super(BitExchange.BIT_FACTORY_BLOCK_ENTITY, pos, state);
    }

    @Override
    public World getConsumerWorld() {
        return this.world;
    }

    @Override
    public BitFluidStorage getInputFluid() {
        return inputFluid;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new BitFactoryScreenHandler(syncId, inv, this);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
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

    public static void tick(World world, BlockPos pos, BlockState state, BitFactoryBlockEntity entity) {
        if (!world.isClient) {
            ItemStack storage = entity.getStack(0);
            ItemStack resource = entity.getStack(1);

            entity.consumeInputs();

            if (!storage.isEmpty() && !resource.isEmpty()) {
                entity.createStacks(resource, 1, 3, 9);
            }
        }
    }

    public boolean createOutput(Item item) {
        ItemStack stack = item.getDefaultStack();
        for (int i = 3; i < 12; i++) {
            ItemStack slot = getStack(i);
            if (slot.isEmpty()) {
                setStack(i, stack);
                return true;
            } else if (slot.getMaxCount() > slot.getCount() && canMergeItems(slot, stack)) {
                slot.increment(1);
                return true;
            }
        }
        return false;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[] { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return slot == 2 && BitRegistries.ITEM.getValue(stack.getItem()) > 0;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot > 2;
    }

    private static boolean canMergeItems(ItemStack first, ItemStack second) {
        if (first.getItem() != second.getItem()) {
            return false;
        } else if (first.getDamage() != second.getDamage()) {
            return false;
        } else if (first.getCount() > first.getMaxCount()) {
            return false;
        } else {
            return ItemStack.areNbtEqual(first, second);
        }
    }
}
