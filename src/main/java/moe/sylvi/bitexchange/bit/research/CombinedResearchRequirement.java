package moe.sylvi.bitexchange.bit.research;

import java.util.List;

public class CombinedResearchRequirement<T extends ResearchRequirement> extends AbstractCombinedResearchRequirement<T> {
    private final List<T> requirements;

    public CombinedResearchRequirement(List<T> requirements) {
        this.requirements = requirements;
    }

    @Override
    public List<T> getRequirements() {
        return requirements;
    }
}
