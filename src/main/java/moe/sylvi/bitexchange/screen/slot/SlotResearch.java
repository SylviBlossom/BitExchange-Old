package moe.sylvi.bitexchange.screen.slot;

import me.shedaniel.autoconfig.AutoConfig;
import moe.sylvi.bitexchange.BitConfig;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.research.ResearchableItem;
import moe.sylvi.bitexchange.transfer.SimpleItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SlotResearch extends Slot {
    private final PlayerInventory playerInventory;

    public SlotResearch(Inventory inventory, PlayerInventory playerInventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.playerInventory = playerInventory;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        var item = stack.getItem();

        var config = AutoConfig.getConfigHolder(BitConfig.class).getConfig();

        if (item instanceof ResearchableItem researchableItem) {
            if (researchableItem.canResearch(stack, playerInventory.player)) {
                return true;
            }
        } else {
            var info = BitRegistries.ITEM.get(item);

            if (info != null && info.isResearchable() && info.getResearch() > 0 && (info.isAutomatable() || config.shouldSupportCraftables())) {
                return true;
            }
        }

        SimpleItemContext context = new SimpleItemContext(stack);
        var fluidStorage = context.find(FluidStorage.ITEM);

        if (fluidStorage != null) {
            var fluidVariant = StorageUtil.findStoredResource(fluidStorage);

            if (fluidVariant != null && !fluidVariant.isBlank()) {
                var info = BitRegistries.FLUID.get(fluidVariant.getFluid());

                return info != null && info.isResearchable() && info.getResearch() > 0;
            }
        }

        return false;
    }
}
