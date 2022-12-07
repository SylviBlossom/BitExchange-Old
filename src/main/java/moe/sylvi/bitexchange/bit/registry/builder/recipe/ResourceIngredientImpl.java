package moe.sylvi.bitexchange.bit.registry.builder.recipe;

import moe.sylvi.bitexchange.bit.BitResource;
import moe.sylvi.bitexchange.bit.info.BitInfo;

import java.util.List;

public record ResourceIngredientImpl<R, I extends BitInfo<R>>(
        List<BitResource<R, I>> resources, int index) implements ResourceIngredient<R, I> {

    @Override
    public List<BitResource<R, I>> getResources() {
        return resources;
    }

    @Override
    public int getIndex() {
        return index;
    }
}
