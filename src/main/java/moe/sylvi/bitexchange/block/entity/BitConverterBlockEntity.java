package moe.sylvi.bitexchange.block.entity;

import moe.sylvi.bitexchange.BitExchange;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.storage.BitStorage;
import moe.sylvi.bitexchange.bit.storage.BitStorages;
import moe.sylvi.bitexchange.inventory.block.BitConverterBlockInventory;
import moe.sylvi.bitexchange.screen.BitConverterScreenHandler;
import moe.sylvi.bitexchange.transfer.BitFluidStorage;
import moe.sylvi.bitexchange.transfer.SimpleItemContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class BitConverterBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, BitConverterBlockInventory, SidedInventory, InventoryProvider {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(getDefaultInventorySize(), ItemStack.EMPTY);
    private final BitFluidStorage inputFluid = new BitFluidStorage();

    public BitConverterBlockEntity(BlockPos pos, BlockState state) {
        super(BitExchange.BIT_CONVERTER_BLOCK_ENTITY, pos, state);
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
        return new BitConverterScreenHandler(syncId, inv, this);
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

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        SimpleItemContext context = new SimpleItemContext(stack);
        BitStorage storage = context.find(BitStorages.ITEM);

        if (slot == 0) {
            return storage != null;
        } else {
            return storage != null || BitRegistries.ITEM.get(stack.getItem()) != null;
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, BitConverterBlockEntity entity) {
        if (!world.isClient) {
            entity.consumeInputs();
        }
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[0];
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return false;
    }

    @Override
    public SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos) {
        return this;
    }
}
