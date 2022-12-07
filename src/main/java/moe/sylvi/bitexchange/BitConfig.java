package moe.sylvi.bitexchange;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.List;

@Config(name = "bitexchange")
public class BitConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public boolean showUnlearnedValues = false;

    @ConfigEntry.Gui.Tooltip
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

    @ConfigEntry.Gui.Tooltip
    public List<String> blacklistedRecipeTypes = List.of(
            "modern_industrialization:quarry"
    );

    @ConfigEntry.Gui.Tooltip
    public List<String> blacklistedRecipes = List.of(
            "modern_industrialization:materials/electrolyzer/bauxite",
            "modern_industrialization:vanilla_recipes/macerator/gilded_blackstone"
    );
}
