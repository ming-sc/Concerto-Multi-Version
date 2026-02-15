package top.gregtao.concerto.core.enums;

import top.gregtao.concerto.core.api.SimpleStringIdentifiable;
import top.gregtao.concerto.core.bridge.ComponentImpl;

public enum Sources implements SimpleStringIdentifiable {
    LOCAL_FILE,
    INTERNET,
    NETEASE_CLOUD,
    QQ_MUSIC,
    KUGOU_MUSIC,
    BILIBILI,
    SHARED
    ;

    public String getKey(String main) {
        return "concerto." + main + "." + this.getSerializedName();
    }

    public String getI18nString() {
        return ComponentImpl.getTranslatable("concerto.source." + this.getSerializedName());
    }
}