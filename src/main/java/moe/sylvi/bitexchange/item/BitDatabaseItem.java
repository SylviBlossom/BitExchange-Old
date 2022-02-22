package moe.sylvi.bitexchange.item;

import moe.sylvi.bitexchange.BitComponents;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.info.BitInfoResearchable;
import moe.sylvi.bitexchange.bit.registry.ResearchableBitRegistry;
import moe.sylvi.bitexchange.bit.research.BitKnowledge;
import moe.sylvi.bitexchange.bit.research.ResearchableItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BitDatabaseItem extends Item implements ResearchableItem {
    public BitDatabaseItem() {
        super(new FabricItemSettings().group(ItemGroup.MISC).rarity(Rarity.EPIC));
    }

    @Override
    public boolean canResearch(ItemStack stack, PlayerEntity player) {
        return true;
    }

    @Override
    public boolean hasResearched(ItemStack stack, PlayerEntity player) {
        return false;
    }

    @Override
    public ItemStack research(ItemStack stack, PlayerEntity player) {
        var learnedAny = false;
        for (var registry : BitRegistries.REGISTRY) {
            if (registry instanceof ResearchableBitRegistry researchRegistry) {
                var knowledge = researchRegistry.getKnowledge(player);
                var knowledgeMap = knowledge.getKnowledgeMap();
                for (var info : researchRegistry) {
                    var bitInfo = (BitInfoResearchable)info;
                    if (!knowledgeMap.containsKey(bitInfo.getResource()) || (long)knowledgeMap.get(bitInfo.getResource()) < bitInfo.getResearch()) {
                        learnedAny = true;
                        knowledgeMap.put(bitInfo.getResource(), bitInfo.getResearch());
                    }
                }
                knowledge.setKnowledgeMap(knowledgeMap);
            }
        }
        if (learnedAny) {
            player.sendMessage(new LiteralText("Research completed!").formatted(Formatting.LIGHT_PURPLE), false);
            stack.decrement(1);
        }
        return stack;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(new LiteralText("Research Item").formatted(Formatting.DARK_PURPLE));
        tooltip.add(new LiteralText("Contains knowledge of all").formatted(Formatting.GRAY));
        tooltip.add(new LiteralText("possible bit sequences.").formatted(Formatting.GRAY));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
