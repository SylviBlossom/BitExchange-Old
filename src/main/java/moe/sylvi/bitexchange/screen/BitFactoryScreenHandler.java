package moe.sylvi.bitexchange.screen;

import moe.sylvi.bitexchange.BitExchange;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.storage.IBitStorage;
import moe.sylvi.bitexchange.inventory.IBitConsumerInventory;
import moe.sylvi.bitexchange.inventory.block.IBitFactoryBlockInventory;
import moe.sylvi.bitexchange.screen.slot.SlotInput;
import moe.sylvi.bitexchange.screen.slot.SlotOutput;
import moe.sylvi.bitexchange.screen.slot.SlotResource;
import moe.sylvi.bitexchange.screen.slot.SlotStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class BitFactoryScreenHandler extends ScreenHandler {
    public static final int PLAYER_SLOT = 12;
    private final PlayerInventory playerInventory;
    private final IBitConsumerInventory inventory;

    //This constructor gets called on the client when the server wants it to open the screenHandler,
    //The client will call the other constructor with an empty Inventory and the screenHandler will automatically
    //sync this empty inventory with the inventory on the server.
    public BitFactoryScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, IBitFactoryBlockInventory.blank());
    }

    //This constructor gets called from the BlockEntity on the server without calling the other constructor first, the server knows the inventory of the container
    //and can therefore directly provide it as an argument. This inventory will then be synced to the client.
    public BitFactoryScreenHandler(int syncId, PlayerInventory playerInventory, IBitConsumerInventory inventory) {
        super(BitExchange.BIT_FACTORY_SCREEN_HANDLER, syncId);
        checkSize(inventory, 12);
        this.playerInventory = playerInventory;
        this.inventory = inventory;
        //some inventories do custom logic when a player opens it.
        inventory.onOpen(playerInventory.player);

        //This will place the slot in the correct locations for a 3x3 Grid. The slots exist on both server and client!
        //This will not render the background of the slots however, this is the Screens job
        int m;
        int l;
        //Our inventory
        this.addSlot(new SlotStorage(inventory, 0, 8, 17));
        this.addSlot(new SlotResource(inventory, 1, 62, 17, playerInventory));
        this.addSlot(new SlotInput(inventory, 2, 9, 52, playerInventory));
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 3; ++l) {
                this.addSlot(new SlotOutput(inventory, 3 + l + m * 3, 116 + l * 18, 17 + m * 18));
            }
        }
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
