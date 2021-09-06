package moe.sylvi.bitexchange.bit.info;

import moe.sylvi.bitexchange.bit.research.BitKnowledge;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
import moe.sylvi.bitexchange.component.BitKnowledgeComponent;

import java.util.List;

public interface BitInfoResearchable<T> extends BitInfo<T> {
    long getResearch();

    ResearchRequirement createResearchRequirement();

    List<ResearchRequirement> getResearchRequirements();
    default void addRequiredResearch(ResearchRequirement requirement) {
        List<ResearchRequirement> requirements = getResearchRequirements();
        if (!requirements.contains(requirement)) {
            requirements.add(requirement);
        }
    }

    <V> BitKnowledge<T> getKnowledge(V provider);
}
