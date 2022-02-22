package moe.sylvi.bitexchange;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "bitexchange")
public class BitConfig implements ConfigData {
    public boolean showUnlearnedValues = false;
}
