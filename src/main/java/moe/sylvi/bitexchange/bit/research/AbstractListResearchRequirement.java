package moe.sylvi.bitexchange.bit.research;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractListResearchRequirement<T extends ResearchRequirement> implements ResearchRequirement {
    public abstract List<T> getRequirements();

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
    public void createTooltip(PlayerEntity player, List<Text> lines, Formatting failColor, Formatting metColor, boolean met) {
        for (T requirement : getRequirements()) {
            requirement.createTooltip(player, lines, failColor, metColor, requirement.isMet(player));
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
}
