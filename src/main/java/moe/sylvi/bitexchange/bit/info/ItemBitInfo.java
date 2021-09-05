package moe.sylvi.bitexchange.bit.info;

import com.google.common.collect.Lists;
import moe.sylvi.bitexchange.BitComponents;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.research.ItemBitResearchRequirement;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
import moe.sylvi.bitexchange.component.BitKnowledgeComponent;
import net.minecraft.item.Item;

import java.util.List;

public class ItemBitInfo implements BitInfoResearchable<Item> {
    protected Item item;
    protected double value;
    protected long research;
    protected boolean automatable;
    protected List<ResearchRequirement> researchRequirements;

    public ItemBitInfo(Item item, double value, long research, boolean automatable) {
        this(item, value, research, automatable, Lists.newArrayList());
    }
    public ItemBitInfo(Item item, double value, long research, boolean automatable, List<ResearchRequirement> researchRequirements) {
        this.item = item;
        this.value = value;
        this.research = research;
        this.automatable = automatable;
        this.researchRequirements = researchRequirements;
    }


    @Override
    public Item getResource() {
        return item;
    }

    @Override
    public void setResource(Item resource) {
        this.item = resource;
    }

    @Override
    public double getValue() {
        return value;
    }

    public long getResearch() {
        return research;
    }

    @Override
    public ResearchRequirement createResearchRequirement() {
        return new ItemBitResearchRequirement(item, BitRegistries.ITEM);
    }

    @Override
    public List<ResearchRequirement> getResearchRequirements() {
        return researchRequirements;
    }

    @Override
    public <V> BitKnowledgeComponent<Item> getKnowledgeComponent(V provider) {
        return BitComponents.ITEM_KNOWLEDGE.get(provider);
    }

    public boolean isAutomatable() {
        return automatable;
    }

    @Override
    public ItemBitInfo copy() {
        return new ItemBitInfo(item, value, research, automatable, researchRequirements);
    }
}
