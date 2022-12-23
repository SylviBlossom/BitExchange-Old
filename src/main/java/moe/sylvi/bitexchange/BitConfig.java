package moe.sylvi.bitexchange;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import moe.sylvi.bitexchange.bit.research.ResearchTier;

import java.util.List;

@Config(name = "bitexchange")
public class BitConfig implements ConfigData {
    // General

    @ConfigEntry.Category("general") @ConfigEntry.Gui.Tooltip
    public boolean showUnlearnedValues = false;

    @ConfigEntry.Category("general") @ConfigEntry.Gui.Tooltip
    public List<String> blacklistedItems = List.of(
            "minecraft:coal_ore",
            "minecraft:copper_ore",
            "minecraft:iron_ore",
            "minecraft:redstone_ore",
            "minecraft:gold_ore",
            "minecraft:lapis_ore",
            "minecraft:emerald_ore",
            "minecraft:diamond_ore"
    );

    @ConfigEntry.Category("general") @ConfigEntry.Gui.Tooltip
    public List<String> blacklistedRecipeTypes = List.of(
            "modern_industrialization:quarry"
    );

    @ConfigEntry.Category("general") @ConfigEntry.Gui.Tooltip
    public List<String> blacklistedRecipes = List.of(
            "modern_industrialization:materials/electrolyzer/bauxite",
            "modern_industrialization:vanilla_recipes/macerator/gilded_blackstone"
    );

    // Balance

    @ConfigEntry.Category("balance") @ConfigEntry.Gui.Tooltip
    public boolean allowBuyCraftables = true;
    @ConfigEntry.Category("balance") @ConfigEntry.Gui.Tooltip
    public boolean allowSellCraftables = true;

    @ConfigEntry.Category("balance") @ConfigEntry.Gui.Tooltip
    public double buyPriceMultiplier = 1;
    @ConfigEntry.Category("balance") @ConfigEntry.Gui.Tooltip
    public double sellPriceMultiplier = 1;

    @ConfigEntry.Category("balance") @ConfigEntry.Gui.Tooltip
    public double craftableBuyPriceMultiplier = 1;
    @ConfigEntry.Category("balance") @ConfigEntry.Gui.Tooltip
    public double craftableSellPriceMultiplier = 1;

    @ConfigEntry.Category("balance") @ConfigEntry.Gui.Tooltip @ConfigEntry.Gui.CollapsibleObject
    public ResearchTiers researchTiers = new ResearchTiers();
    public static class ResearchTiers {
        @ConfigEntry.Gui.Tooltip public long abundant = ResearchTier.ABUNDANT.getDefaultResearch();
        @ConfigEntry.Gui.Tooltip public long common = ResearchTier.COMMON.getDefaultResearch();
        @ConfigEntry.Gui.Tooltip public long uncommon = ResearchTier.UNCOMMON.getDefaultResearch();
        @ConfigEntry.Gui.Tooltip public long rare = ResearchTier.RARE.getDefaultResearch();
        @ConfigEntry.Gui.Tooltip public long epic = ResearchTier.EPIC.getDefaultResearch();
        @ConfigEntry.Gui.Tooltip public long unique = ResearchTier.UNIQUE.getDefaultResearch();
        @ConfigEntry.Gui.Tooltip public long crafted = ResearchTier.CRAFTED.getDefaultResearch();
    }


    public double getBuyPriceMultiplier(boolean craftable) {
        return craftable ? buyPriceMultiplier * craftableBuyPriceMultiplier : buyPriceMultiplier;
    }
    public double getSellPriceMultiplier(boolean craftable) {
        return craftable ? sellPriceMultiplier * craftableSellPriceMultiplier : sellPriceMultiplier;
    }
    public boolean shouldSupportCraftables() {
        return allowBuyCraftables || allowSellCraftables;
    }
}
