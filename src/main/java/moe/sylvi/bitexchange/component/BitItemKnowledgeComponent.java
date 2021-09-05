package moe.sylvi.bitexchange.component;

import com.google.common.collect.Lists;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import moe.sylvi.bitexchange.BitComponents;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BitItemKnowledgeComponent implements BitKnowledgeComponent<Item>, AutoSyncedComponent {
    private Map<Item, Long> knowledge = new HashMap<>();
    private final Object provider;

    public BitItemKnowledgeComponent(Object provider) {
        this.provider = provider;
    }

    @Override
    public long getKnowledge(Item item) {
        return knowledge.getOrDefault(item, (long)0);
    }

    @Override
    public long addKnowledge(Item item, long count) {
        long current = knowledge.getOrDefault(item, (long)0);
        long added = Math.min(count, BitRegistries.ITEM.getResearch(item) - current);
        knowledge.put(item, current + added);
        BitComponents.ITEM_KNOWLEDGE.sync(provider);
        return added;
    }

    @Override
    public boolean getLearned(Item item) {
        ItemBitInfo info = BitRegistries.ITEM.get(item);
        return info != null && knowledge.getOrDefault(item, (long)0) >= info.getResearch();
    }

    @Override
    public boolean canLearn(Item item) {
        ItemBitInfo info = BitRegistries.ITEM.get(item);
        if (info != null) {
            boolean failedAny = false;
            for (ResearchRequirement requirement : info.getResearchRequirements()) {
                if (!requirement.isMet((PlayerEntity) provider)) {
                    failedAny = true;
                    break;
                }
            }
            return !failedAny;
        }
        return false;
    }

    @Override
    public Map<Item, Long> getAllKnowledge() {
        return new HashMap<>(knowledge);
    }

    @Override
    public void setAllKnowledge(Map<Item, Long> knowledge) {
        this.knowledge = knowledge;
        BitComponents.ITEM_KNOWLEDGE.sync(provider);
    }

    @Override
    public List<Item> getAllLearned() {
        List<Item> result = Lists.newArrayList();
        for (Map.Entry<Item, Long> entry : knowledge.entrySet()) {
            ItemBitInfo info = BitRegistries.ITEM.get(entry.getKey());
            if (info != null && entry.getValue() >= info.getResearch()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        NbtList list = (NbtList)tag.get("Knowledge");

        knowledge.clear();
        for (int i = 0; i < list.size(); i++) {
            NbtCompound subtag = list.getCompound(i);

            Item item = Registry.ITEM.get(new Identifier(subtag.getString("id")));
            long value = subtag.getLong("value");

            knowledge.put(item, value);
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        NbtList list = new NbtList();
        tag.put("Knowledge", list);

        int i = 0;
        for (Map.Entry<Item, Long> entry : knowledge.entrySet()) {
            NbtCompound subtag = new NbtCompound();
            list.add(i++, subtag);

            subtag.putString("id", Registry.ITEM.getId(entry.getKey()).toString());
            subtag.putLong("value", entry.getValue());
        }
    }
}
