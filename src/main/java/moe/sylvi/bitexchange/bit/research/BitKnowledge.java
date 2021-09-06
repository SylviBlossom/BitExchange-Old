package moe.sylvi.bitexchange.bit.research;

import java.util.List;
import java.util.Map;

public interface BitKnowledge<T> {
    Map<T, Long> getKnowledgeMap();
    void setKnowledgeMap(Map<T, Long> knowledge);

    long getKnowledge(T resource);
    long addKnowledge(T resource, long count);

    boolean hasLearned(T resource);
    boolean canLearn(T resource);
    List<T> getAllLearned();
}
