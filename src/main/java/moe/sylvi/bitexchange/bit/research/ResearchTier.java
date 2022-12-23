package moe.sylvi.bitexchange.bit.research;

import me.shedaniel.autoconfig.AutoConfig;
import moe.sylvi.bitexchange.BitConfig;

public enum ResearchTier {
    ABUNDANT("abundant", 64),
      COMMON("common",   32),
    UNCOMMON("uncommon", 16),
        RARE("rare",     8 ),
        EPIC("epic",     4 ),
      UNIQUE("unique",   2 ),
     CRAFTED("crafted",  1 );

    private final String name;
    private final long defaultResearch;

    ResearchTier(String name, long defaultResearch) {
        this.name = name;
        this.defaultResearch = defaultResearch;
    }

    public String getName() {
        return name;
    }

    public long getDefaultResearch() {
        return defaultResearch;
    }

    public long getResearch() {
        var config = AutoConfig.getConfigHolder(BitConfig.class).getConfig();

        return switch (this) {
            case ABUNDANT -> config.researchTiers.abundant;
            case COMMON   -> config.researchTiers.common;
            case UNCOMMON -> config.researchTiers.uncommon;
            case RARE     -> config.researchTiers.rare;
            case EPIC     -> config.researchTiers.epic;
            case UNIQUE   -> config.researchTiers.unique;
            case CRAFTED  -> config.researchTiers.crafted;
        };
    }

    public static ResearchTier byName(String name) {
        for (ResearchTier tier : values()) {
            if (tier.getName().equalsIgnoreCase(name)) {
                return tier;
            }
        }
        return null;
    }
}
