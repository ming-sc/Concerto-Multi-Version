package top.gregtao.concerto.core.api;

public interface SimpleStringIdentifiable {
    default String getSerializedName() {
        return this.toString().toLowerCase();
    }
}
