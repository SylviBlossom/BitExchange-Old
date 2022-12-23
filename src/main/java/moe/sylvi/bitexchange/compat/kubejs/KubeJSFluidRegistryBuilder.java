package moe.sylvi.bitexchange.compat.kubejs;

import moe.sylvi.bitexchange.bit.info.FluidBitInfo;
import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import moe.sylvi.bitexchange.bit.registry.builder.BitRegistryBuilder;
import moe.sylvi.bitexchange.bit.registry.builder.ItemPriorities;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;

public class KubeJSFluidRegistryBuilder implements BitRegistryBuilder<Fluid, FluidBitInfo> {
    protected final BitRegistry<Fluid, FluidBitInfo> registry;

    public Map<Fluid, BitRegistryFluidBuilderJS> builders = new HashMap<>();

    public KubeJSFluidRegistryBuilder(BitRegistry<Fluid, FluidBitInfo> registry) {
        this.registry = registry;
    }

    @Override
    public int getPriority() {
        return ItemPriorities.ABOVE_DATA;
    }

    @Override
    public void prepare(MinecraftServer server) {
        BitRegistryEvents.FLUIDS.post(new BitRegistryFluidEventJS(this));

        for (var fluid : builders.keySet()) {
            registry.prepareResource(fluid, this);
        }
    }

    @Override
    public FluidBitInfo process(Fluid resource) {
        return builders.get(resource).build(resource);
    }

    @Override
    public void postProcess() {
        builders.clear();
    }
}
