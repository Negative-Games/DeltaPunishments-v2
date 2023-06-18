package games.negative.punishments.core.implementation;

import com.google.common.collect.Lists;
import games.negative.punishments.api.PunishSQLManager;
import games.negative.punishments.api.structure.PersistentPunishment;
import games.negative.punishments.api.structure.PunishmentType;
import games.negative.punishments.core.structure.SQLCredentials;
import litebans.api.Database;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PunishSQLManagerProvider implements PunishSQLManager {

    private Connection connection;
    private final SQLCredentials credentials;

    public PunishSQLManagerProvider(SQLCredentials credentials, boolean enabled) {
        this.credentials = credentials;
        if (enabled) {
            try {
                this.connection = DriverManager.getConnection("jdbc:mysql://" +
                                credentials.getHost() + ":" + credentials.getPort() + "/" +
                                credentials.getDatabase() + "?useSSL=false&?autoReconnect=true",
                        credentials.getUsername(), credentials.getPassword());
            } catch (SQLException ignored) {

            }
        }
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public Collection<PersistentPunishment> getPunishments(@NotNull UUID uuid, @NotNull PunishmentType type) {
        List<PersistentPunishment> punishments = Lists.newArrayList();
        if (connection != null) {
            // Determine which table to use
            String query = "SELECT * FROM {table} WHERE uuid=?";
            switch (type) {
                case BAN: {
                    query = query.replace("{table}", credentials.getBansTable());
                    break;
                }
                case MUTE: {
                    query = query.replace("{table}", credentials.getMutesTable());
                    break;
                }
                case KICK: {
                    query = query.replace("{table}", credentials.getKicksTable());
                    break;
                }
                case WARN: {
                    query = query.replace("{table}", credentials.getWarnsTable());
                    break;
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uuid.toString());

                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        long id = result.getLong("id");
                        String reason = result.getString("reason");
                        String banned_by_uuid = result.getString("banned_by_name");
                        String removed_by_uuid = result.getString("removed_by_uuid");
                        long until = result.getLong("until");
                        long time = result.getLong("time");

                        PersistentPunishment punishment = new PersistentPunishment(id, reason, type, banned_by_uuid, uuid.toString(), time, until);
                        punishment.setRemoved(removed_by_uuid);
                        punishments.add(punishment);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            try (PreparedStatement statement = Database.get().prepareStatement("SELECT * FROM {" + type.getTableName() + "} where uuid=?")) {
                statement.setString(1, uuid.toString());

                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        long id = result.getLong("id");
                        String reason = result.getString("reason");
                        String banned_by_uuid = result.getString("banned_by_name");
                        String removed_by_uuid = null;
                        try {
                            removed_by_uuid = result.getString("removed_by_uuid");
                        } catch (SQLException ignored) {

                        }

                        long until = result.getLong("until");
                        long time = result.getLong("time");

                        PersistentPunishment punishment = new PersistentPunishment(id, reason, type, banned_by_uuid, uuid.toString(), time, until);
                        punishment.setRemoved(removed_by_uuid);
                        punishments.add(punishment);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return punishments;
    }
}
