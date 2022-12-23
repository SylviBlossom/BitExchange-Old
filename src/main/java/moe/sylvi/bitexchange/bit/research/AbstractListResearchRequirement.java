package moe.sylvi.bitexchange.bit.research;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractListResearchRequirement<T extends ResearchRequirement> implements ResearchRequirement {
    public abstract List<T> getRequirements();

    @Override
    public Identifier getId() {
        return ResearchRequirements.LIST_REQUIREMENT_ID;
    }

    @Override
    public boolean isMet(PlayerEntity player) {
        for (T requirement : getRequirements()) {
            if (!requirement.isMet(player)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void createTooltip(PlayerEntity player, List<Text> lines, Style failStyle, Style metStyle, boolean met) {
        for (T requirement : getRequirements()) {
            requirement.createTooltip(player, lines, failStyle, metStyle, requirement.isMet(player));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractListResearchRequirement<?> that = (AbstractListResearchRequirement<?>) o;
        return getRequirements().equals(that.getRequirements());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getRequirements().toArray());
    }

    @Override
    public void writeToPacket(PacketByteBuf buf) {
        ResearchRequirement.super.writeToPacket(buf);

        buf.writeVarInt(getRequirements().size());
        for (T requirement : getRequirements()) {
            requirement.writeToPacket(buf);
        }
    }

    public static AbstractListResearchRequirement<?> readFromPacket(PacketByteBuf buf) {
        int size = buf.readVarInt();
        ResearchRequirement[] requirements = new ResearchRequirement[size];
        for (int i = 0; i < size; i++) {
            requirements[i] = ResearchRequirement.readFromPacket(buf);
        }
        return new ListResearchRequirement(Arrays.asList(requirements));
    }
}
