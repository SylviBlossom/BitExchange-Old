package moe.sylvi.bitexchange.bit.research;

import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.info.BitInfoResearchable;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;

public class BitResearchRequirement<R, I extends BitInfo<R>> implements ResearchRequirement {
    public final R resource;
    public final BitRegistry<R, I> registry;

    public BitResearchRequirement(R resource, BitRegistry<R, I> registry) {
        this.resource = resource;
        this.registry = registry;
    }

    @Override
    public Identifier getId() {
        return ResearchRequirements.BIT_REQUIREMENT_ID;
    }

    @Override
    public boolean isMet(PlayerEntity player) {
        if (registry.get(resource) instanceof BitInfoResearchable researchable) {
            return researchable.getKnowledge(player).hasLearned(resource);
        }
        return false;
    }

    public Text getName(PlayerEntity player) {
        return registry.get(resource).getDisplayName();
    }

    @Override
    public void createTooltip(PlayerEntity player, List<Text> lines, Style failStyle, Style metStyle, boolean met) {
        var style = met ? metStyle : failStyle;
        lines.add(Text.literal(met ? "✔ " : "✘ ").setStyle(style).append(getName(player).copy().setStyle(style)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BitResearchRequirement<?,?> that = (BitResearchRequirement<?,?>) o;
        return Objects.equals(resource, that.resource) && Objects.equals(registry, that.registry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resource, registry);
    }

    @Override
    public void writeToPacket(PacketByteBuf buf) {
        ResearchRequirement.super.writeToPacket(buf);

        buf.writeIdentifier(BitRegistries.REGISTRY.getId(registry));
        buf.writeIdentifier(registry.getResourceRegistry().getId(resource));
    }

    static BitResearchRequirement readFromPacket(PacketByteBuf buf) {
        var registryId = buf.readIdentifier();
        var resourceId = buf.readIdentifier();

        if (!BitRegistries.REGISTRY.containsId(registryId)) {
            throw new IllegalArgumentException("Unknown registry " + registryId);
        }

        var registry = BitRegistries.REGISTRY.get(registryId);

        if (!registry.getResourceRegistry().containsId(resourceId)) {
            throw new IllegalArgumentException("Unknown resource " + resourceId);
        }

        var resource = registry.getResourceRegistry().get(resourceId);

        return new BitResearchRequirement(resource, registry);
    }
}
