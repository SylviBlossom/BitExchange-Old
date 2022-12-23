package moe.sylvi.bitexchange.compat.kubejs;

import moe.sylvi.bitexchange.BitExchange;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.registry.builder.ItemDataRegistryBuilder;
import org.apache.logging.log4j.Level;

public class BitExchangeKubeJSCompat {
    public static void load() {
        BitRegistries.ITEM.registerBuilder(new KubeJSItemRegistryBuilder(BitRegistries.ITEM));
        BitRegistries.FLUID.registerBuilder(new KubeJSFluidRegistryBuilder(BitRegistries.FLUID));
    }
}
