package moe.sylvi.bitexchange.bit.research;

import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public interface ResearchRequirement {
    static <T> BitResearchRequirement<T> of(T resource, BitRegistry<T, BitInfo<T>> registry) {
        return new BitResearchRequirement<>(resource, registry);
    }

    boolean isMet(PlayerEntity player);

    void createTooltip(PlayerEntity player, List<Text> lines, Formatting failColor, Formatting metColor, boolean met);
    default void createTooltip(PlayerEntity player, List<Text> lines, Formatting failColor, Formatting metColor) {
        createTooltip(player, lines, failColor, metColor, isMet(player));
    }
}
