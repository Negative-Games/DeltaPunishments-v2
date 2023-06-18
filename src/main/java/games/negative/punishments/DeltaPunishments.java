package games.negative.punishments;

import games.negative.framework.commands.CommandBuilder;
import games.negative.punishments.api.PunishSQLManager;
import games.negative.punishments.api.PunishmentCacheManager;
import games.negative.punishments.commands.CommandHistory;
import games.negative.punishments.commands.CommandPunish;
import games.negative.punishments.commands.CommandPunishReload;
import games.negative.punishments.core.Locale;
import games.negative.punishments.core.implementation.PunishSQLManagerProvider;
import games.negative.punishments.core.implementation.PunishmentAPIProvider;
import games.negative.punishments.core.implementation.PunishmentCacheManagerProvider;
import games.negative.punishments.core.structure.SQLCredentials;
import games.negative.punishments.core.util.ConfigUtils;
import games.negative.punishments.core.util.Permissions;
import games.negative.framework.BasePlugin;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.SQLException;

public final class DeltaPunishments extends BasePlugin {

    @Getter
    private static DeltaPunishments instance;
    @Getter
    private PunishSQLManager sqlManager;

    @Getter
    private PunishmentCacheManager cacheManager;

    @Override
    public void onEnable() {
        super.onEnable();
        // Plugin startup logic
        long start = System.currentTimeMillis();
        instance = this;

        loadFiles(
                this, "punishments.yml",
                "permissions.yml",
                "guis.yml",
                "sql.yml"
        );

        Locale.init(this);
        Permissions.loadPermissions();

        FileConfiguration sqlConfig = new ConfigUtils("sql").getConfig();
        boolean enabled = sqlConfig.getBoolean("enabled", false);
        SQLCredentials credentials = new SQLCredentials(
                sqlConfig.getString("host"),
                sqlConfig.getString("port"),
                sqlConfig.getString("database"),
                sqlConfig.getString("username"),
                sqlConfig.getString("password"),
                sqlConfig.getString("bans-table"),
                sqlConfig.getString("mutes-table"),
                sqlConfig.getString("kicks-table"),
                sqlConfig.getString("warns-table")
        );

        this.sqlManager = new PunishSQLManagerProvider(credentials, enabled);
        this.cacheManager = new PunishmentCacheManagerProvider(this);

        new PunishmentAPIProvider(this, false);

        regCommands();
    }

    private void regCommands() {
        // Register /history command
        registerCommand(
                new CommandBuilder(new CommandHistory(this))
                        .name("history").aliases("hist").params("player").playerOnly()
                        .permission(Permissions.HISTORY)
        );

        // Register /punish command
        registerCommand(
            new CommandBuilder(new CommandPunish(this))
                    .name("punish").aliases("pu").params("player").permission(Permissions.PUNISH)
        );

        // Register /punishreload command
        registerCommand(
                new CommandBuilder(new CommandPunishReload())
                        .name("punishreload").aliases("pureload").permission(Permissions.RELOAD)
        );
    }

    @Override
    public void onDisable() {
        if (sqlManager != null) {
            Connection connection = sqlManager.getConnection();
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

}
