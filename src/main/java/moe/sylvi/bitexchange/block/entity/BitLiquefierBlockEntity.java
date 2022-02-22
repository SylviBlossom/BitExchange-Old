package moe.sylvi.bitexchange.block.entity;

import moe.sylvi.bitexchange.BitExchange;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.BitHelper;
import moe.sylvi.bitexchange.bit.info.FluidBitInfo;
import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import moe.sylvi.bitexchange.bit.storage.BitStorage;
import moe.sylvi.bitexchange.bit.storage.BitStorages;
import moe.sylvi.bitexchange.block.BitLiquefierBlock;
import moe.sylvi.bitexchange.inventory.block.BitLiquefierBlockInventory;
import moe.sylvi.bitexchange.screen.BitConverterScreenHandler;
import moe.sylvi.bitexchange.screen.BitLiquefierScreenHandler;
import moe.sylvi.bitexchange.transfer.BitFluidStorage;
import moe.sylvi.bitexchange.transfer.FullInventoryStorage;
import moe.sylvi.bitexchange.transfer.InventoryItemContext;
import moe.sylvi.bitexchange.transfer.SimpleItemContext;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class BitLiquefierBlockEntity extends SyncingBlockEntity implements ExtendedScreenHandlerFactory, BitLiquefierBlockInventory, SidedInventory, InventoryProvider {
    public static final long FLUID_CAPACITY = FluidConstants.BUCKET * 8;

    private final DefaultedList<ItemStack> inventory;
    public final BitFluidStorage inputFluid;
    public final BitFluidStorage outputFluid;
    public final CombinedStorage<FluidVariant, BitFluidStorage> combinedStorage;

    public BitLiquefierBlockEntity(BlockPos pos, BlockState state) {
        super(BitExchange.BIT_LIQUEFIER_BLOCK_ENTITY, pos, state);
        this.inventory = DefaultedList.ofSize(getDefaultInventorySize(), ItemStack.EMPTY);
        this.inputFluid = new BitFluidStorage();
        this.outputFluid = new BitFluidStorage(FLUID_CAPACITY, false, true);
        this.combinedStorage = new CombinedStorage<>(Arrays.asList(this.inputFluid, this.outputFluid));
    }

    public FluidVariant getStoredVariant() {
        return this.outputFluid.getResource();
    }

    public float getFillPercent() {
        return (float)this.outputFluid.getAmount() / this.outputFluid.getCapacity();
    }

    public void updateLuminance() {
        FluidVariant variant = getStoredVariant();
        var luminance = variant.isBlank() ? 0 : variant.getFluid().getDefaultState().getBlockState().getLuminance();

        BlockState state = world.getBlockState(pos);
        if (state.get(BitLiquefierBlock.LUMINANCE).intValue() != luminance) {
            world.setBlockState(pos, state.with(BitLiquefierBlock.LUMINANCE, luminance));
        }
    }

    public Storage<FluidVariant> getFluidResource() {
        ItemStack stack = getStack(1);
        if (!stack.isEmpty()) {
            InventoryStorage inventoryStorage = FullInventoryStorage.of(this);
            InventoryItemContext context = new InventoryItemContext(inventoryStorage, 1, getWorld());
            return context.find(FluidStorage.ITEM);
        }
        return null;
    }

    @Override
    public BitFluidStorage getInputFluid() {
        return inputFluid;
    }

    @Override
    public BitFluidStorage getOuputFluid() {
        return outputFluid;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new BitLiquefierScreenHandler(syncId, inv, this);
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        SimpleItemContext context = new SimpleItemContext(stack);
        BitStorage storage = context.find(BitStorages.ITEM);

        if (slot == 0) {
            return storage != null;
        } else if (slot == 1) {
            var fluidStorage = context.find(FluidStorage.ITEM);
            if (fluidStorage != null) {
                var resource = StorageUtil.findStoredResource(fluidStorage, null);
                return resource != null && !resource.isBlank() && BitRegistries.FLUID.get(resource.getFluid()) != null;
            }
            return false;
        } else if (slot == 2) {
            return storage != null || BitRegistries.ITEM.get(stack.getItem()) != null;
        }

        return false;
    }

    public static void tick(World world, BlockPos pos, BlockState state, BitLiquefierBlockEntity entity) {
        if (!world.isClient) {
            entity.consumeInputs();
            entity.createFluid();
        }
    }

    public void createFluid() {
        BitStorage bitStorage = this.getStorage();
        Storage<FluidVariant> fluidStorage = this.getFluidResource();

        if (bitStorage != null) {
            var fluidVariant = StorageUtil.findStoredResource(fluidStorage, null);

            if (fluidVariant == null && !this.outputFluid.isResourceBlank()) {
                consumeFluid(this.outputFluid, true);
            }

            if (fluidVariant != null && !fluidVariant.isBlank()) {
                if (!fluidVariant.equals(this.outputFluid.variant)) {
                    consumeFluid(this.outputFluid, true);
                }

                FluidBitInfo info = BitRegistries.FLUID.get(fluidVariant.getFluid());

                if (info != null && (this.outputFluid.isResourceBlank() || fluidVariant.equals(this.outputFluid.variant))) {
                    var cost = info.getValue() / FluidConstants.BUCKET;
                    long maxFluid = Math.min(FluidConstants.BUCKET, FLUID_CAPACITY - this.outputFluid.amount);

                    double toExtract = cost * maxFluid;
                    long fluidExtracted = maxFluid;
                    try (Transaction transaction = Transaction.openOuter()) {
                        double extracted = bitStorage.extract(toExtract, transaction);
                        if (cost != 0) {
                            fluidExtracted = (long) Math.floor(extracted / cost);
                        }
                    }
                    if (fluidExtracted > 0) {
                        try (Transaction transaction = Transaction.openOuter()) {
                            this.outputFluid.variant = fluidVariant;
                            this.outputFluid.amount += fluidExtracted;
                            bitStorage.extract(fluidExtracted * cost, transaction);
                            transaction.commit();
                        }
                        markDirty();
                    }
                }
            }
        }
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[] { 2 };
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return slot == 2 && BitRegistries.ITEM.getValue(stack.getItem()) > 0;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    @Override
    public SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos) {
        return this;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        updateLuminance();
    }

    private void readFluidNBT(NbtCompound tag) {
        if (tag.contains("Fluid")) {
            var fluidTag = tag.getCompound("Fluid");
            var id = fluidTag.getString("id");
            var amount = fluidTag.getLong("amount");

            var fluid = Registry.FLUID.get(new Identifier(id));
            FluidVariant variant;
            if (fluidTag.contains("tag")) {
                variant = FluidVariant.of(fluid, fluidTag.getCompound("tag"));
            } else {
                variant = FluidVariant.of(fluid);
            }

            this.outputFluid.variant = variant;
            this.outputFluid.amount = amount;
        } else {
            this.outputFluid.variant = FluidVariant.blank();
            this.outputFluid.amount = 0;
        }
    }

    private NbtCompound writeFluidNBT(NbtCompound tag) {
        if (!this.outputFluid.isResourceBlank()) {
            var identifier = Registry.FLUID.getId(this.outputFluid.variant.getFluid());

            var fluidTag = new NbtCompound();
            fluidTag.putString("id", identifier.toString());
            fluidTag.putLong("amount", this.outputFluid.amount);
            if (this.outputFluid.variant.hasNbt()) {
                fluidTag.put("tag", this.outputFluid.variant.copyNbt());
            }

            tag.put("Fluid", fluidTag);
        }

        return tag;
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        Inventories.readNbt(tag, this.inventory);
        readFluidNBT(tag);
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        Inventories.writeNbt(tag, this.inventory);
        writeFluidNBT(tag);
        super.writeNbt(tag);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(getPos());
    }
}
