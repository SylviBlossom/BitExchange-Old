package moe.sylvi.bitexchange.bit.info;

import moe.sylvi.bitexchange.bit.research.BitKnowledge;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
import moe.sylvi.bitexchange.component.BitKnowledgeComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
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
        player.sendMessage(new LiteralText("Researched: ").formatted(Formatting.LIGHT_PURPLE).append(getDisplayName()), false);
    }
}
