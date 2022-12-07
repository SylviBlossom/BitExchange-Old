package moe.sylvi.bitexchange.bit.registry.builder.recipe;

import moe.sylvi.bitexchange.bit.BitResource;
import moe.sylvi.bitexchange.bit.info.BitInfo;

import java.util.Optional;

public class RecipeHandlerOutput<R, I extends BitInfo<R>> {
    public final BitResource<R, I> resource;
    public final Optional<Long> ratio;

    public RecipeHandlerOutput(BitResource<R, I> resource) {
        this.resource = resource;
        this.ratio = Optional.empty();
    }

    public RecipeHandlerOutput(BitResource<R, I> resource, long ratio) {
        this.resource = resource;
        this.ratio = Optional.of(ratio);
    }

    public long getActualRatio() {
        var info = resource.getOrProcessInfo();
        if (info.notNullOrRecursive()) {
            return info.get().getRatio();
        } else {
            return ratio.orElse(1L);
        }
    }
}
