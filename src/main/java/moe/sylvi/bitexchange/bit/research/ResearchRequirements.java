package moe.sylvi.bitexchange.bit.research;

import moe.sylvi.bitexchange.BitExchange;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ResearchRequirements {
    public static final Registry<ResearchRequirement.ResearchRequirementReader> REGISTRY =
            FabricRegistryBuilder.createSimple(ResearchRequirement.ResearchRequirementReader.class, new Identifier(BitExchange.MOD_ID, "research_requirement")).buildAndRegister();

    public static final Identifier BIT_REQUIREMENT_ID = new Identifier(BitExchange.MOD_ID, "bit");
    public static final Identifier LIST_REQUIREMENT_ID = new Identifier(BitExchange.MOD_ID, "list");
    public static final Identifier COMBINED_REQUIREMENT_ID = new Identifier(BitExchange.MOD_ID, "combined");

    static {
        register(BIT_REQUIREMENT_ID, BitResearchRequirement::readFromPacket);
        register(LIST_REQUIREMENT_ID, ListResearchRequirement::readFromPacket);
        register(COMBINED_REQUIREMENT_ID, CombinedResearchRequirement::readFromPacket);
    }

    public static void register(Identifier id, ResearchRequirement.ResearchRequirementReader deserializer) {
        Registry.register(REGISTRY, id, deserializer);
    }
}
