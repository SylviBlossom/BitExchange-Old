package moe.sylvi.bitexchange.bit.registry;

import moe.sylvi.bitexchange.bit.info.BitInfoResearchable;
import moe.sylvi.bitexchange.bit.research.BitKnowledge;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;

import java.util.ArrayList;
import java.util.List;

public interface ResearchableBitRegistry<R, I extends BitInfoResearchable<R>> extends BitRegistry<R, I> {
    <V> BitKnowledge<R> getKnowledge(V provider);

    default long getResearch(R resource) {
        var info = get(resource);
        return info != null ? info.getResearch() : 0;
    }

    default List<ResearchRequirement> getResearchRequirements(R resource) {
        var info = get(resource);
        return info != null ? info.getResearchRequirements() : new ArrayList<>();
    }
}
