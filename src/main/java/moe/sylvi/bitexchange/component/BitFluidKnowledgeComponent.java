package moe.sylvi.bitexchange.component;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import moe.sylvi.bitexchange.BitComponents;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.info.FluidBitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;

import java.util.HashMap;
import java.util.Map;

public class BitFluidKnowledgeComponent implements IBitKnowledgeComponent<Fluid, FluidBitInfo>, AutoSyncedComponent {
    private Map<Fluid, Long> knowledge = new HashMap<>();
    private final Object provider;

    public BitFluidKnowledgeComponent(Object provider) {
        this.provider = provider;
    }

    @Override
    public PlayerEntity getPlayer() {
        return (PlayerEntity) provider;
    }

    @Override
    public BitRegistry<Fluid, FluidBitInfo> getBitRegistry() {
        return BitRegistries.FLUID;
    }

    @Override
    public Map<Fluid, Long> getKnowledgeMap() {
        return knowledge;
    }

    @Override
    public void setKnowledgeMap(Map<Fluid, Long> knowledge) {
        this.knowledge = knowledge;
        BitComponents.FLUID_KNOWLEDGE.sync(provider);
    }
}
