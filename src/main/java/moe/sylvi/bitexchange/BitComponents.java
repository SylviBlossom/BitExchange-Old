package moe.sylvi.bitexchange;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import moe.sylvi.bitexchange.component.BitItemKnowledgeComponent;
import moe.sylvi.bitexchange.component.BitKnowledgeComponent;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public class BitComponents implements EntityComponentInitializer {
    public static final ComponentKey<BitKnowledgeComponent<Item>> ITEM_KNOWLEDGE =
            ComponentRegistry.getOrCreate(new Identifier(BitExchange.MOD_ID, "item_knowledge"), BitKnowledgeComponent.asClass());

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(ITEM_KNOWLEDGE, BitItemKnowledgeComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}
