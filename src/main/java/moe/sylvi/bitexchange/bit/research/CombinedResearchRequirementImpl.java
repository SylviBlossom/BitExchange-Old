package moe.sylvi.bitexchange.bit.research;

import java.util.List;

public class CombinedResearchRequirementImpl<T extends ResearchRequirement> extends CombinedResearchRequirement<T> {
    private final List<T> requirements;

    public CombinedResearchRequirementImpl(List<T> requirements) {
        this.requirements = requirements;
    }

    @Override
    public List<T> getRequirements() {
        return requirements;
    }
}
