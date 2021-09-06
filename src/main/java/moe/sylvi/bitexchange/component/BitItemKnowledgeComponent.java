package moe.sylvi.bitexchange.component;

import com.google.common.collect.Lists;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import moe.sylvi.bitexchange.BitComponents;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.info.BitInfoResearchable;
import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
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

public class BitItemKnowledgeComponent implements BitKnowledgeComponent<Item, ItemBitInfo>, AutoSyncedComponent {
    private Map<Item, Long> knowledge = new HashMap<>();
    private final Object provider;

    public BitItemKnowledgeComponent(Object provider) {
        this.provider = provider;
    }

    @Override
    public PlayerEntity getPlayer() {
        return (PlayerEntity) provider;
    }

    @Override
    public BitRegistry<Item, ItemBitInfo> getBitRegistry() {
        return BitRegistries.ITEM;
    }

    @Override
    public Map<Item, Long> getKnowledgeMap() {
        return knowledge;
    }

    @Override
    public void setKnowledgeMap(Map<Item, Long> knowledge) {
        this.knowledge = knowledge;
        BitComponents.ITEM_KNOWLEDGE.sync(provider);
    }
}
