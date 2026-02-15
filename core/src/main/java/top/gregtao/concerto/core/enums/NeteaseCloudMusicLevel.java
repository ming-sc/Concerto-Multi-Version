package top.gregtao.concerto.core.enums;

import top.gregtao.concerto.core.api.SimpleStringIdentifiable;

public enum NeteaseCloudMusicLevel implements SimpleStringIdentifiable {
    STANDARD,
    HIGHER,
    EXHIGH,
    LOSSLESS,
    HIRES;

    @Override
    public String getSerializedName() {
        return this.toString().toLowerCase();
    }
}
