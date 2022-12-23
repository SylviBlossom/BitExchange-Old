package moe.sylvi.bitexchange.bit.research;

import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public interface ResearchRequirement {


    Identifier getId();

    static <R, I extends BitInfo<R>> BitResearchRequirement<R, I> of(R resource, BitRegistry<R, I> registry) {
        return new BitResearchRequirement<>(resource, registry);
    }

    boolean isMet(PlayerEntity player);

    void createTooltip(PlayerEntity player, List<Text> lines, Style failStyle, Style metStyle, boolean met);
    default void createTooltip(PlayerEntity player, List<Text> lines, Style failStyle, Style metStyle) {
        createTooltip(player, lines, failStyle, metStyle, isMet(player));
    }

    default void writeToPacket(PacketByteBuf buf) {
        buf.writeIdentifier(getId());
    }


    static ResearchRequirement readFromPacket(PacketByteBuf buf) {
        var requirementId = buf.readIdentifier();

        if (!ResearchRequirements.REGISTRY.containsId(requirementId)) {
            throw new IllegalArgumentException("Unknown research requirement type: " + requirementId);
        }

        return ResearchRequirements.REGISTRY.get(requirementId).readFromPacket(buf);
    }

    @FunctionalInterface
    interface ResearchRequirementReader {
        ResearchRequirement readFromPacket(PacketByteBuf buf);
    }
}
