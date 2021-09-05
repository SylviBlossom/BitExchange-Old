package moe.sylvi.bitexchange.component;

import dev.onyxstudios.cca.api.v3.component.Component;

import java.util.List;
import java.util.Map;

public interface BitKnowledgeComponent<T> extends Component {
    long getKnowledge(T resource);
    long addKnowledge(T resource, long count);

    Map<T, Long> getAllKnowledge();
    void setAllKnowledge(Map<T, Long> knowledge);

    boolean getLearned(T resource);
    boolean canLearn(T resource);
    List<T> getAllLearned();

    @SuppressWarnings("unchecked")
    static <T> Class<BitKnowledgeComponent<T>> asClass() {
        return (Class<BitKnowledgeComponent<T>>) (Object) BitKnowledgeComponent.class;
    }
}
