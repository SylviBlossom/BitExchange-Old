package moe.sylvi.bitexchange.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface BitRegistryEvents {
    EventGroup GROUP = EventGroup.of("BitRegistryEvents");

    EventHandler ITEMS = GROUP.server("items", () -> BitRegistryItemEventJS.class);
    EventHandler FLUIDS = GROUP.server("fluids", () -> BitRegistryFluidEventJS.class);
}
