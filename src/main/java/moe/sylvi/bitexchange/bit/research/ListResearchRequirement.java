package moe.sylvi.bitexchange.bit.research;

import java.util.List;

public class ListResearchRequirement<T extends ResearchRequirement> extends AbstractListResearchRequirement<T> {
    private final List<T> requirements;

    public ListResearchRequirement(List<T> requirements) {
        this.requirements = requirements;
    }

    @Override
    public List<T> getRequirements() {
        return requirements;
    }
}
