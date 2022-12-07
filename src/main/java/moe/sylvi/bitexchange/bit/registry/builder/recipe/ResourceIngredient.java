package moe.sylvi.bitexchange.bit.registry.builder.recipe;

import moe.sylvi.bitexchange.bit.BitResource;
import moe.sylvi.bitexchange.bit.info.BitInfo;

import java.util.List;

public interface ResourceIngredient<R, I extends BitInfo<R>> {
    static <R, I extends BitInfo<R>> ResourceIngredient<R, I> of(List<BitResource<R, I>> resources) {
        return new ResourceIngredientImpl<>(resources, -1);
    }
    static <R, I extends BitInfo<R>> ResourceIngredient<R, I> of(List<BitResource<R, I>> resources, int index) {
        return new ResourceIngredientImpl<>(resources, index);
    }

    List<BitResource<R, I>> getResources();

    int getIndex();

    default boolean isEmpty() {
        return getResources().isEmpty();
    }
}
