package top.gregtao.concerto.core.bridge;

import java.util.Objects;

public class ComponentImpl {

    public static IComponent INSTANCE;

    public static void init(IComponent component) {
        INSTANCE = component;
    }

    public static String getTranslatable(String key) {
        Objects.requireNonNull(INSTANCE);
        return INSTANCE.getTranslatable(key);
    }

    public static void displayClientMessage(boolean actionBar, String key, Object... args) {
        Objects.requireNonNull(INSTANCE);
        INSTANCE.displayClientMessage(actionBar, key, args);
    }
}
