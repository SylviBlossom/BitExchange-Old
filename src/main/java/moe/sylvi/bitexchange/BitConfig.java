package moe.sylvi.bitexchange;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "bitexchange")
public class BitConfig implements ConfigData {
    boolean showUnlearnedValues = false;
}
