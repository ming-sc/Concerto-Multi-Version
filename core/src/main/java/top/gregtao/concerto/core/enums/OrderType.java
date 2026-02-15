package top.gregtao.concerto.core.enums;

import top.gregtao.concerto.core.api.SimpleStringIdentifiable;
import top.gregtao.concerto.core.bridge.ComponentImpl;

public enum OrderType implements SimpleStringIdentifiable {
    NORMAL,
    RANDOM,
    REVERSED,
    LOOP;

    public String getI18nString() {
        return ComponentImpl.getTranslatable("concerto.order." + this.getSerializedName());
    }

    @Override
    public String getSerializedName() {
        return this.toString().toLowerCase();
    }
}