package moe.sylvi.bitexchange.screen.slot;

import me.shedaniel.autoconfig.AutoConfig;
import moe.sylvi.bitexchange.BitComponents;
import moe.sylvi.bitexchange.BitConfig;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.BitHelper;
import moe.sylvi.bitexchange.bit.storage.BitStorages;
import moe.sylvi.bitexchange.transfer.SimpleItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SlotInput extends Slot {
    private PlayerInventory playerInventory;

    public SlotInput(Inventory inventory, int index, int x, int y, PlayerInventory playerInventory) {
        super(inventory, index, x, y);
        this.playerInventory = playerInventory;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        var item = stack.getItem();
        var context = new SimpleItemContext(stack);

        if (context.find(BitStorages.ITEM) != null) {
            return true;
        }

        var info = BitRegistries.ITEM.get(item);

        var config = AutoConfig.getConfigHolder(BitConfig.class).getConfig();
        var isDisabled = info != null && !info.isAutomatable() && !config.allowSellCraftables;

        var sellValue = BitHelper.getItemValue(stack, BitHelper.PurchaseMode.SELL);

        if (sellValue > 0 && BitComponents.ITEM_KNOWLEDGE.get(playerInventory.player).hasLearned(item) && !isDisabled) {
            return true;
        }

        var fluidStorage = context.find(FluidStorage.ITEM);
        if (fluidStorage != null) {
            var fluidVariant = StorageUtil.findStoredResource(fluidStorage);

            if (fluidVariant != null && !fluidVariant.isBlank()) {
                var fluid = fluidVariant.getFluid();

                if (BitRegistries.FLUID.getValue(fluid) > 0 && BitComponents.FLUID_KNOWLEDGE.get(playerInventory.player).hasLearned(fluid)) {
                    return true;
                }
            }
        }

        return false;
    }
}
