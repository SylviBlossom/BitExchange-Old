package moe.sylvi.bitexchange.bit;

public interface Recursable<T> {
    static <O> Recursable<O> of(O value, boolean recursive) {
        return new RecursableImpl<>(value, recursive);
    }

    T get();

    boolean isRecursive();

    default boolean notNullOrRecursive() {
        return !isRecursive() && get() != null;
    }

    default T consumeRecursive(Runnable action) {
        if (isRecursive()) {
            action.run();
        }
        return get();
    }

    default <O> Recursable<O> into(O value) {
        return of(value, isRecursive());
    }
}
