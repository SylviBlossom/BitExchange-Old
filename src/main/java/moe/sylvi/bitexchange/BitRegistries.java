package moe.sylvi.bitexchange;

import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import moe.sylvi.bitexchange.bit.registry.FluidBitRegistry;
import moe.sylvi.bitexchange.bit.registry.ItemBitRegistry;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BitRegistries {
    public static final Registry<BitRegistry> REGISTRY =
            FabricRegistryBuilder.createSimple(BitRegistry.class, new Identifier(BitExchange.MOD_ID, "bit_registry")).buildAndRegister();

    public static final ItemBitRegistry ITEM;
    public static final FluidBitRegistry FLUID;

    static {
        ITEM = Registry.register(REGISTRY, new Identifier(BitExchange.MOD_ID, "item"), new ItemBitRegistry(Registry.ITEM));
        FLUID = Registry.register(REGISTRY, new Identifier(BitExchange.MOD_ID, "fluid"), new FluidBitRegistry(Registry.FLUID));
    }

    public static void build(MinecraftServer server) {
        for (BitRegistry<?,?> registry : REGISTRY) {
            registry.preBuild(server);
        }
        for (BitRegistry<?,?> registry : REGISTRY) {
            registry.build();
        }
        for (BitRegistry<?,?> registry : REGISTRY) {
            registry.postBuild();
        }
    }
}
