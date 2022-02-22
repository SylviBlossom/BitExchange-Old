package moe.sylvi.bitexchange.bit.research;

import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.info.BitInfoResearchable;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Objects;

public class BitResearchRequirement<T> implements ResearchRequirement {
    public final T resource;
    public final BitRegistry<T, BitInfo<T>> registry;

    public BitResearchRequirement(T resource, BitRegistry<T, BitInfo<T>> registry) {
        this.resource = resource;
        this.registry = registry;
    }

    @Override
    public boolean isMet(PlayerEntity player) {
        if (registry.get(resource) instanceof BitInfoResearchable<T> researchable) {
            return researchable.getKnowledge(player).hasLearned(resource);
        }
        return false;
    }

    public Text getName(PlayerEntity player) {
        return registry.get(resource).getDisplayName();
    }

    @Override
    public void createTooltip(PlayerEntity player, List<Text> lines, Formatting failColor, Formatting metColor, boolean met) {
        Formatting formatting = met ? metColor : failColor;
        lines.add(new LiteralText(met ? "✔ " : "✘ ").formatted(formatting).append(getName(player).shallowCopy().formatted(formatting)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BitResearchRequirement<?> that = (BitResearchRequirement<?>) o;
        return Objects.equals(resource, that.resource) && Objects.equals(registry, that.registry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resource, registry);
    }
}
