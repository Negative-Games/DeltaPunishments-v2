package games.negative.punishments.api.structure;

import lombok.Getter;

public enum PunishmentType {
    BAN("ban", "bans"),
    MUTE("mute", "mutes"),
    KICK("kick", "kicks"),
    WARN("warn", "warnings"),
    ;

    @Getter
    private final String name;
    @Getter
    private final String tableName;

    PunishmentType(String name, String tableName) {
        this.name = name;
        this.tableName = tableName;
    }
}
