package moe.sylvi.bitexchange.bit.info;

import moe.sylvi.bitexchange.BitComponents;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.research.BitKnowledge;
import moe.sylvi.bitexchange.bit.research.BitResearchRequirement;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ItemBitInfo implements BitInfoResearchable<Item> {
    protected final Item item;
    protected final double value;
    protected final long research;
    protected final boolean researchable;
    protected final boolean automatable;
    protected final List<ResearchRequirement> researchRequirements;

    public ItemBitInfo(Item item, double value, long research, boolean researchable, boolean automatable, List<ResearchRequirement> researchRequirements) {
        this.item = item;
        this.value = value;
        this.research = research;
        this.researchable = researchable;
        this.automatable = automatable;
        this.researchRequirements = researchRequirements;
    }


    @Override
    public Item getResource() {
        return item;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public long getResearch() {
        return research;
    }

    @Override
    public boolean isResearchable() {
        return researchable;
    }

    @Override
    public ResearchRequirement createResearchRequirement() {
        return new BitResearchRequirement(item, BitRegistries.ITEM);
    }

    @Override
    public List<ResearchRequirement> getResearchRequirements() {
        return researchRequirements;
    }

    @Override
    public <V> BitKnowledge<Item> getKnowledge(V provider) {
        return BitComponents.ITEM_KNOWLEDGE.get(provider);
    }

    public boolean isAutomatable() {
        return automatable;
    }

    @Override
    public ItemBitInfo copy() {
        return new ItemBitInfo(item, value, research, researchable, automatable, researchRequirements);
    }

    @Override
    public Text getDisplayName() {
        return item.getName();
    }

    @Override
    public void showResearchMessage(PlayerEntity player) {
        player.sendMessage(new LiteralText("Researched item: ").formatted(Formatting.LIGHT_PURPLE).append(item.getDefaultStack().toHoverableText()), false);
    }
}
