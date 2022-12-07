package moe.sylvi.bitexchange.screen;

import moe.sylvi.bitexchange.BitExchange;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.storage.IBitStorage;
import moe.sylvi.bitexchange.inventory.block.IBitLiquefierBlockInventory;
import moe.sylvi.bitexchange.screen.slot.*;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

public class BitLiquefierScreenHandler extends ScreenHandler {
    public static final int PLAYER_SLOT = 3;
    private final PlayerInventory playerInventory;
    private final IBitLiquefierBlockInventory inventory;
    private BlockPos pos;

    //This constructor gets called on the client when the server wants it to open the screenHandler,
    //The client will call the other constructor with an empty Inventory and the screenHandler will automatically
    //sync this empty inventory with the inventory on the server.
    public BitLiquefierScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, IBitLiquefierBlockInventory.blank());
        pos = buf.readBlockPos();
    }

    //This constructor gets called from the BlockEntity on the server without calling the other constructor first, the server knows the inventory of the container
    //and can therefore directly provide it as an argument. This inventory will then be synced to the client.
    public BitLiquefierScreenHandler(int syncId, PlayerInventory playerInventory, IBitLiquefierBlockInventory inventory) {
        super(BitExchange.BIT_LIQUEFIER_SCREEN_HANDLER, syncId);
        checkSize(inventory, 3);
        this.playerInventory = playerInventory;
        this.inventory = inventory;
        this.pos = BlockPos.ORIGIN;
        //some inventories do custom logic when a player opens it.
        inventory.onOpen(playerInventory.player);

        //This will place the slot in the correct locations for a 3x3 Grid. The slots exist on both server and client!
        //This will not render the background of the slots however, this is the Screens job
        int m;
        int l;
        //Our inventory
        this.addSlot(new SlotStorage(inventory, 0, 26, 17));
        this.addSlot(new SlotFluidResource(inventory, 1, 80, 17, playerInventory));
        this.addSlot(new SlotInput(inventory, 2, 26, 52, playerInventory));
        //The player inventory
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
            }
        }
        //The player Hotbar
        for (m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
        }

    }

    public PlayerInventory getPlayerInventory() {
        return this.playerInventory;
    }

    public double getBits() {
        IBitStorage storage = inventory.getStorage();
        return storage != null ? storage.getBits() : -1.0;
    }

    public double getResourceBits() {
        ItemStack resource = this.slots.get(1).getStack();
        if (!resource.isEmpty()) {
            return BitRegistries.ITEM.getValue(resource.getItem());
        }
        return 0;
    }

    public BlockPos getPos() {
        return pos;
    }

    public FluidVariant getFluidVariant() {
        return inventory.getOuputFluid().getResource();
    }

    public long getFluidAmount() {
        return inventory.getOuputFluid().getAmount();
    }

    public float getFluidPercentage() {
        return (float)inventory.getOuputFluid().getAmount() / inventory.getOuputFluid().getCapacity();
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    // Shift + Player Inv Slot
    @Override
    public ItemStack transferSlot(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.slots.get(1).getStack().isEmpty() && this.slots.get(1).canInsert(originalStack)) {
                ItemStack insertStack = originalStack.copy();
                insertStack.setCount(1);
                this.slots.get(1).setStack(insertStack);
                this.slots.get(1).markDirty();
                originalStack.decrement(1);
                return ItemStack.EMPTY;
            } else if (!(this.insertItem(originalStack, 0, 1, false) || this.insertItem(originalStack, 2, 3, false))) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }
}
