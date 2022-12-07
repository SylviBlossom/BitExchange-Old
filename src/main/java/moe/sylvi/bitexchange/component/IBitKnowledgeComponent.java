package moe.sylvi.bitexchange.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import moe.sylvi.bitexchange.bit.info.BitInfoResearchable;
import moe.sylvi.bitexchange.bit.research.PlayerBitKnowledge;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public interface IBitKnowledgeComponent<T,R extends BitInfoResearchable<T>> extends PlayerBitKnowledge<T,R>, Component {
    @Override
    default void readFromNbt(NbtCompound tag) {
        var list = (NbtList)tag.get("Knowledge");

        var knowledge = new HashMap<T, Long>();
        for (int i = 0; i < list.size(); i++) {
            var subtag = list.getCompound(i);

            var resource = getBitRegistry().getResourceRegistry().get(new Identifier(subtag.getString("id")));
            var value = subtag.getLong("value");

            knowledge.put(resource, value);
        }

        setKnowledgeMap(knowledge);
    }

    @Override
    default void writeToNbt(NbtCompound tag) {
        var list = new NbtList();
        tag.put("Knowledge", list);

        int i = 0;
        for (var entry : getKnowledgeMap().entrySet()) {
            var subtag = new NbtCompound();
            list.add(i++, subtag);

            subtag.putString("id", getBitRegistry().getResourceRegistry().getId(entry.getKey()).toString());
            subtag.putLong("value", entry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    static <T,R extends BitInfoResearchable<T>> Class<IBitKnowledgeComponent<T,R>> asClass() {
        return (Class<IBitKnowledgeComponent<T,R>>) (Object) IBitKnowledgeComponent.class;
    }
}
