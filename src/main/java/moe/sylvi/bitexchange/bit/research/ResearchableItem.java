package moe.sylvi.bitexchange.bit.research;

import moe.sylvi.bitexchange.block.entity.BitResearcherBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface ResearchableItem {
    boolean canResearch(ItemStack stack, PlayerEntity player);
    boolean hasResearched(ItemStack stack, PlayerEntity player);
    ItemStack research(ItemStack stack, PlayerEntity player);
}
