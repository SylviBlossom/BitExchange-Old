package moe.sylvi.bitexchange.bit.research;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.List;

public abstract class CombinedResearchRequirement<T extends ResearchRequirement> implements ResearchRequirement {
    public static <O extends ResearchRequirement> CombinedResearchRequirement<O> of(List<O> requirements) {
        return new CombinedResearchRequirementImpl<>(requirements);
    }

    public abstract List<T> getRequirements();

    public int getDisplaySwitchSpeed() {
        return 20;
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
    public void createTooltip(PlayerEntity player, List<Text> lines, Formatting failColor, Formatting metColor, boolean met) {
        List<T> requirements = getRequirements();
        if (!requirements.isEmpty()) {
            T requirement = requirements.get((int) Math.floorDiv(player.world.getTime(), getDisplaySwitchSpeed()) % requirements.size());
            boolean newMet = met || requirement.isMet(player);
            List<Text> newLines = Lists.newArrayList();
            requirement.createTooltip(player, newLines, failColor, metColor, newMet);
            for (Text line : newLines) {
                if (requirements.size() == 1 || requirement instanceof ListResearchRequirement listRequirement) {
                    lines.add(line);
                } else if (!line.getString().endsWith(" ...")) {
                    lines.add(line.shallowCopy().append(new LiteralText(" ...").formatted(newMet ? metColor : failColor)));
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CombinedResearchRequirement<?> that = (CombinedResearchRequirement<?>) o;
        return getRequirements().equals(that.getRequirements());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getRequirements().toArray());
    }
}
