package games.negative.punishments.core.implementation;

import games.negative.punishments.DeltaPunishments;
import games.negative.punishments.api.PunishDataManager;
import games.negative.punishments.api.structure.PersistentPunishment;
import games.negative.punishments.api.structure.PunishmentType;
import litebans.api.Database;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PunishmentDataManagerProvider implements PunishDataManager {

    private final ArrayList<PersistentPunishment> bans = new ArrayList<>();
    private final ArrayList<PersistentPunishment> mutes = new ArrayList<>();
    private final ArrayList<PersistentPunishment> kicks = new ArrayList<>();
    private final ArrayList<PersistentPunishment> warns = new ArrayList<>();

    public PunishmentDataManagerProvider() {
        DeltaPunishments plugin = DeltaPunishments.getInstance();
//        new CacheTask().runTaskTimerAsynchronously(plugin, 0, 20 * 15);
    }

    @Override
    public Collection<PersistentPunishment> getBans() {
        return bans;
    }

    @Override
    public Collection<PersistentPunishment> getBans(@NotNull UUID uuid) {
        return bans.stream().filter(punishment -> punishment.getUuid().equals(uuid.toString())).collect(Collectors.toList());
    }

    @Override
    public Collection<PersistentPunishment> getBans(@NotNull Player player) {
        return getBans(player.getUniqueId());
    }

    @Override
    public Collection<PersistentPunishment> getBans(@NotNull OfflinePlayer player) {
        return getBans(player.getUniqueId());
    }

    @Override
    public Collection<PersistentPunishment> getMutes() {
        return mutes;
    }

    @Override
    public Collection<PersistentPunishment> getMutes(@NotNull UUID uuid) {
        return mutes.stream().filter(punishment -> punishment.getUuid().equals(uuid.toString())).collect(Collectors.toList());
    }

    @Override
    public Collection<PersistentPunishment> getMutes(@NotNull Player player) {
        return getMutes(player.getUniqueId());
    }

    @Override
    public Collection<PersistentPunishment> getMutes(@NotNull OfflinePlayer player) {
        return getMutes(player.getUniqueId());
    }

    @Override
    public Collection<PersistentPunishment> getKicks() {
        return kicks;
    }

    @Override
    public Collection<PersistentPunishment> getKicks(@NotNull UUID uuid) {
        return kicks.stream().filter(punishment -> punishment.getUuid().equals(uuid.toString())).collect(Collectors.toList());
    }

    @Override
    public Collection<PersistentPunishment> getKicks(@NotNull Player player) {
        return getKicks(player.getUniqueId());
    }

    @Override
    public Collection<PersistentPunishment> getKicks(@NotNull OfflinePlayer player) {
        return getKicks(player.getUniqueId());
    }

    @Override
    public Collection<PersistentPunishment> getWarns() {
        return warns;
    }

    @Override
    public Collection<PersistentPunishment> getWarns(@NotNull UUID uuid) {
        return warns.stream().filter(punishment -> punishment.getUuid().equals(uuid.toString())).collect(Collectors.toList());
    }

    @Override
    public Collection<PersistentPunishment> getWarns(@NotNull Player player) {
        return getWarns(player.getUniqueId());
    }

    @Override
    public Collection<PersistentPunishment> getWarns(@NotNull OfflinePlayer player) {
        return getWarns(player.getUniqueId());
    }

    @Override
    public CompletableFuture<Collection<PersistentPunishment>> getPunishments(@NotNull PunishmentType type) {
        return CompletableFuture.supplyAsync(() -> loadPunishments(type));
    }

    private Collection<PersistentPunishment> loadPunishments(@NotNull PunishmentType type) {
        Collection<PersistentPunishment> collection = new LinkedList<>();
        try (PreparedStatement st = Database.get().prepareStatement("SELECT id FROM {" + type.getTableName() + "}")) {
            try (ResultSet rs = st.executeQuery()) {
                List<Long> ids = new ArrayList<>();
                while (rs.next()) {
                    long id = rs.getLong("id");
                    ids.add(id);
                }
                close(rs, st);

                for (Long id : ids) {
                    String reason = getReason(id, type);
                    String bannedByName = getBannedName(id, type);
                    String uuid = getUUID(id, type);
                    long time = getTime(id, type);
                    long until = getUntil(id, type);
                    boolean active = getActive(id, type);
                    String removed = (getRemovedUUID(id, type) == null ? "n/a" : getRemovedUUID(id, type));

                    PersistentPunishment punishment = new PersistentPunishment(id, reason, type, bannedByName, uuid, time, until);

                    punishment.setActive(active);
                    punishment.setRemoved(removed);

                    collection.add(punishment);
                }
            }
            close(null, st);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return collection;
    }

    @Override
    public CompletableFuture<Collection<PersistentPunishment>> getPunishments(@NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {

            Collection<PersistentPunishment> collection = new LinkedList<>();
            loadPunishments(PunishmentType.BAN).stream().filter(punishment -> punishment.getUuid().equals(uuid.toString()))
                    .forEach(collection::add);

            loadPunishments(PunishmentType.MUTE).stream().filter(punishment -> punishment.getUuid().equals(uuid.toString()))
                    .forEach(collection::add);

            loadPunishments(PunishmentType.KICK).stream().filter(punishment -> punishment.getUuid().equals(uuid.toString()))
                    .forEach(collection::add);

            loadPunishments(PunishmentType.WARN).stream().filter(punishment -> punishment.getUuid().equals(uuid.toString()))
                    .forEach(collection::add);

            return collection;
        });
    }

    @Override
    public CompletableFuture<Collection<PersistentPunishment>> getPunishments(@NotNull Player player) {
        return getPunishments(player.getUniqueId());
    }

    @Override
    public CompletableFuture<Collection<PersistentPunishment>> getPunishments(@NotNull OfflinePlayer player) {
        return getPunishments(player.getUniqueId());
    }

    @Override
    public CompletableFuture<Void> updatePunishment(PunishmentType category, long id, @NotNull Consumer<PersistentPunishment> updateFunction) {
        return CompletableFuture.runAsync(()
                -> getPunishments(category).whenCompleteAsync((punishments, throwable)
                -> punishments.stream().filter(punishment -> punishment.getId() == id).findFirst().ifPresent(updateFunction)));
    }

    @Override
    public CompletableFuture<Void> deletePunishment(PunishmentType category, long id) {
        return CompletableFuture.runAsync(() -> {
            try (PreparedStatement st = Database.get().prepareStatement("DELETE FROM {" + category.getTableName() + "} WHERE id=?")) {
                st.closeOnCompletion();
                st.setLong(1, id);
                st.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        });
    }

    @Override
    public CompletableFuture<Void> deletePunishment(@NotNull PersistentPunishment punishment) {
        return CompletableFuture.runAsync(() -> {
            try (PreparedStatement st = Database.get().prepareStatement("DELETE FROM {" + punishment.getCategory().getTableName() + "} WHERE id=?")) {
                st.closeOnCompletion();
                st.setLong(1, punishment.getId());
                st.executeUpdate();

                switch (punishment.getCategory()) {
                    case BAN: {
                        bans.remove(punishment);
                        break;
                    }

                    case KICK: {
                        kicks.remove(punishment);
                        break;
                    }

                    case MUTE: {
                        mutes.remove(punishment);
                        break;
                    }

                    case WARN: {
                        warns.remove(punishment);
                        break;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        });
    }

    @Override
    public CompletableFuture<Void> updateTime(PunishmentType category, long id, long duration) {
        return CompletableFuture.runAsync(() -> {
            try (PreparedStatement st = Database.get().prepareStatement("UPDATE {" + category.getTableName() + "} SET until=? WHERE id=?")) {
                st.setLong(1, (duration == -1 ? -1 : (System.currentTimeMillis() + duration)));
                st.setLong(2, id);
                st.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public CompletableFuture<Void> updateActive(PunishmentType category, long id, boolean active) {
        return CompletableFuture.runAsync(() -> {
            try (PreparedStatement st = Database.get().prepareStatement("UPDATE {" + category.getTableName() + "} SET active=? WHERE id=?")) {
                st.closeOnCompletion();
                st.setBoolean(1, active);
                st.setLong(2, id);
                st.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }


    private String getReason(long id, PunishmentType type) {
        try (PreparedStatement st = Database.get().prepareStatement("SELECT reason FROM {" + type.getTableName() + "} WHERE id=?")) {
            st.setLong(1, id);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    String result = rs.getString("reason");
                    close(rs, st);
                    return result;
                }
                close(rs, st);
            } catch (SQLException e) {
                close(null, st);
                return null;
            }
        } catch (SQLException e) {
            return null;
        }
        return null;
    }

    private String getBannedName(long id, PunishmentType type) {
        try (PreparedStatement st = Database.get().prepareStatement("SELECT banned_by_name FROM {" + type.getTableName() + "} WHERE id=?")) {
            st.setLong(1, id);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    String result = rs.getString("banned_by_name");
                    close(rs, st);
                    return result;
                }
                close(rs, st);
            } catch (SQLException e) {
                close(null, st);
                return null;
            }
        } catch (SQLException e) {
            return null;
        }
        return null;
    }

    private String getUUID(long id, PunishmentType type) {
        try (PreparedStatement st = Database.get().prepareStatement("SELECT uuid FROM {" + type.getTableName() + "} WHERE id=?")) {
            st.setLong(1, id);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    String result = rs.getString("uuid");
                    close(rs, st);
                    return result;
                }
                close(rs, st);
            } catch (SQLException e) {
                close(null, st);
                return null;
            }
        } catch (SQLException e) {
            return null;
        }
        return null;
    }

    private long getTime(long id, PunishmentType type) {
        try (PreparedStatement st = Database.get().prepareStatement("SELECT time FROM {" + type.getTableName() + "} WHERE id=?")) {
            st.setLong(1, id);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    long result = rs.getLong("time");
                    close(rs, st);
                    return result;
                }
                close(rs, st);
            } catch (SQLException e) {
                close(null, st);
                return 0;
            }
        } catch (SQLException e) {
            return 0;
        }
        return 0;
    }

    private long getUntil(long id, PunishmentType type) {
        try (PreparedStatement st = Database.get().prepareStatement("SELECT until FROM {" + type.getTableName() + "} WHERE id=?")) {
            st.setLong(1, id);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    long result = rs.getLong("until");
                    close(rs, st);
                    return result;
                }
            } catch (SQLException e) {
                close(null, st);
                return 0;
            }
        } catch (SQLException e) {
            return 0;
        }
        return 0;
    }

    private boolean getActive(long id, PunishmentType type) {
        try (PreparedStatement st = Database.get().prepareStatement("SELECT active FROM {" + type.getTableName() + "} WHERE id=?")) {
            st.setLong(1, id);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    boolean result = rs.getBoolean("active");
                    close(rs, st);
                    return result;
                }
                close(rs, st);
            } catch (SQLException e) {
                close(null, st);
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
        return false;
    }

    private String getRemovedUUID(long id, PunishmentType type) {
        try (PreparedStatement st = Database.get().prepareStatement("SELECT removed_by_uuid FROM {" + type.getTableName() + "} WHERE id=?")) {
            st.setLong(1, id);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    String result = rs.getString("removed_by_uuid");
                    close(rs, st);
                    return result;
                }
                close(rs, st);
            } catch (SQLException e) {
                close(null, st);
                return null;
            }

        } catch (SQLException e) {
            return null;
        }
        return null;
    }

    private class CacheTask extends BukkitRunnable {

        @Override
        public void run() {
            bans.clear();
            mutes.clear();
            kicks.clear();
            warns.clear();

            bans.addAll(loadPunishments(PunishmentType.BAN));
            mutes.addAll(loadPunishments(PunishmentType.MUTE));
            kicks.addAll(loadPunishments(PunishmentType.KICK));
            warns.addAll(loadPunishments(PunishmentType.WARN));
        }
    }

    private void close(@Nullable ResultSet result, @Nullable PreparedStatement statement) {
        if (result != null) {
            try {
                result.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
