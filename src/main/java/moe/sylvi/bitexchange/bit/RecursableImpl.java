package moe.sylvi.bitexchange.bit;

public class RecursableImpl<T> implements Recursable<T> {
    private final T value;
    private final boolean recursive;

    public RecursableImpl(T value, boolean recursive) {
        this.value = value;
        this.recursive = recursive;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public boolean isRecursive() {
        return recursive;
    }
}
