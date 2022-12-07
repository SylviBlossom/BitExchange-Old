package moe.sylvi.bitexchange.data.api;

import net.minecraft.tag.TagKey;

import java.util.function.Consumer;

public class ItemOrTag<R> {
    public static <R> ItemOrTag<R> of(R resource) {
        return new ItemOrTag<>(resource);
    }
    public static <R> ItemOrTag<R> of(TagKey<R> tag) {
        return new ItemOrTag<>(tag);
    }

    private final R item;
    private final TagKey<R> tag;
    private final boolean isTag;

    public ItemOrTag(R item) {
        this.item = item;
        this.tag = null;
        this.isTag = false;
    }

    public ItemOrTag(TagKey<R> tag) {
        this.item = null;
        this.tag = tag;
        this.isTag = true;
    }


    public R getItem() {
        return item;
    }

    public TagKey<R> getTag() {
        return tag;
    }


    public boolean isTag() {
        return isTag;
    }

    public boolean isItem() {
        return !isTag;
    }


    public ItemOrTag<R> consumeItem(Consumer<R> action) {
        if (!isTag) {
            action.accept(item);
        }
        return this;
    }

    public ItemOrTag<R> consumeTag(Consumer<TagKey<R>> action) {
        if (isTag) {
            action.accept(tag);
        }
        return this;
    }
}
