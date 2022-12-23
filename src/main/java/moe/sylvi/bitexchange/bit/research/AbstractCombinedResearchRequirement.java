package moe.sylvi.bitexchange.bit.research;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractCombinedResearchRequirement<T extends ResearchRequirement> implements ResearchRequirement {
    public static <O extends ResearchRequirement> AbstractCombinedResearchRequirement<O> of(List<O> requirements) {
        return new CombinedResearchRequirement<>(requirements);
    }

    public abstract List<T> getRequirements();

    public int getDisplaySwitchSpeed() {
        return 20;
    }

    @Override
    public Identifier getId() {
        return ResearchRequirements.COMBINED_REQUIREMENT_ID;
    }

    @Override
    public boolean isMet(PlayerEntity player) {
        List<T> requirements = getRequirements();
        if (requirements.isEmpty()) {
            return true;
        }
        for (T requirement : requirements) {
            if (requirement.isMet(player)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void createTooltip(PlayerEntity player, List<Text> lines, Style failStyle, Style metStyle, boolean met) {
        List<T> requirements = getRequirements();
        if (!requirements.isEmpty()) {
            T requirement = requirements.get((int) Math.floorDiv(player.world.getTime(), getDisplaySwitchSpeed()) % requirements.size());
            boolean newMet = met || requirement.isMet(player);
            List<Text> newLines = new ArrayList<>();
            requirement.createTooltip(player, newLines, failStyle, metStyle, newMet);
            for (Text line : newLines) {
                if (requirements.size() == 1 || requirement instanceof AbstractListResearchRequirement listRequirement) {
                    lines.add(line);
                } else if (!line.getString().endsWith(" ...")) {
                    lines.add(line.copy().append(Text.literal(" ...").setStyle(newMet ? metStyle : failStyle)));
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (AbstractCombinedResearchRequirement<?>) o;
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

    public static AbstractCombinedResearchRequirement<?> readFromPacket(PacketByteBuf buf) {
        int size = buf.readVarInt();
        ResearchRequirement[] requirements = new ResearchRequirement[size];
        for (int i = 0; i < size; i++) {
            requirements[i] = ResearchRequirement.readFromPacket(buf);
        }
        return of(Arrays.asList(requirements));
    }
}
