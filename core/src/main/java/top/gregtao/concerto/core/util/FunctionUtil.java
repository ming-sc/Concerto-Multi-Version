package top.gregtao.concerto.core.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FunctionUtil {
    public static Runnable emptyRunnable() {
        return () -> {};
    }

    public static <T> Consumer<T> emptyConsumer() {
        return t -> {};
    }

    public static <T, R> Function<T, R> emptyFunction() {
        return t -> null;
    }

    public static <T> Supplier<T> emptySupplier() {
        return () -> null;
    }
}
