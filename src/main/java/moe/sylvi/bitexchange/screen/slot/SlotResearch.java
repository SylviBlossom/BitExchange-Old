package moe.sylvi.bitexchange.screen.slot;

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

        var isResearchable = false;
        if (item instanceof ResearchableItem researchableItem) {
            isResearchable = researchableItem.canResearch(stack, playerInventory.player);
        } else {
            var info = BitRegistries.ITEM.get(item);
            isResearchable = info != null && info.isResearchable() && info.getResearch() > 0;
        }

        if (!isResearchable) {
            SimpleItemContext context = new SimpleItemContext(stack);
            var fluidStorage = context.find(FluidStorage.ITEM);

            if (fluidStorage != null) {
                var fluidVariant = StorageUtil.findStoredResource(fluidStorage);

                if (fluidVariant != null && !fluidVariant.isBlank()) {
                    var info = BitRegistries.FLUID.get(fluidVariant.getFluid());

                    isResearchable = info != null && info.isResearchable() && info.getResearch() > 0;
                }
            }
        }

        return isResearchable;
    }
}
