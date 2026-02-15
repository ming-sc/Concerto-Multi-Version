package top.gregtao.concerto.core.bridge;

public interface IComponent {
    String getTranslatable(String key);

    void displayClientMessage(boolean actionBar, String key, Object... args);
}
