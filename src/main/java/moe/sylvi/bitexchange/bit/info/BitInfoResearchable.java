package moe.sylvi.bitexchange.bit.info;

import moe.sylvi.bitexchange.bit.research.BitKnowledge;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public interface BitInfoResearchable<T> extends BitInfo<T> {
    long getResearch();

    boolean isResearchable();

    ResearchRequirement createResearchRequirement();

    List<ResearchRequirement> getResearchRequirements();

    default void addRequiredResearch(ResearchRequirement requirement) {
        List<ResearchRequirement> requirements = getResearchRequirements();
        if (!requirements.contains(requirement)) {
            requirements.add(requirement);
        }
    }

    <V> BitKnowledge<T> getKnowledge(V provider);

    default void showResearchMessage(PlayerEntity player) {
        player.sendMessage(Text.literal("Researched: ").formatted(Formatting.LIGHT_PURPLE).append(getDisplayName()), false);
    }
}
