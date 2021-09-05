package moe.sylvi.bitexchange.bit.research;

import java.util.List;

public class ListResearchRequirementImpl<T extends ResearchRequirement> extends ListResearchRequirement<T> {
    private final List<T> requirements;

    public ListResearchRequirementImpl(List<T> requirements) {
        this.requirements = requirements;
    }

    @Override
    public List<T> getRequirements() {
        return requirements;
    }
}
