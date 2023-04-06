package games.negative.punishments.core.implementation;

import games.negative.punishments.api.PunishmentCacheManager;
import games.negative.punishments.api.structure.PersistentPunishment;
import games.negative.punishments.api.structure.PunishmentType;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import litebans.api.Database;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class PunishmentCacheManagerProvider implements PunishmentCacheManager {

    private final Multimap<UUID, PersistentPunishment> cache;
    private final BukkitTask task;

    public PunishmentCacheManagerProvider(JavaPlugin plugin) {
        this.cache = ArrayListMultimap.create();
        this.task = new CacheTask().runTaskTimerAsynchronously(plugin, 0, 20 * 10);
    }

    @Override
    public void cache() {
        cache.clear();

        Collection<PersistentPunishment> all = Lists.newArrayList();
        all.addAll(loadPunishments(PunishmentType.BAN));
        all.addAll(loadPunishments(PunishmentType.MUTE));
        all.addAll(loadPunishments(PunishmentType.KICK));
        all.addAll(loadPunishments(PunishmentType.WARN));

        int validPunishments = 0;
        int invalidPunishments = 0;
        for (PersistentPunishment punishment : all) {
            UUID uuid;
            try {
                uuid = UUID.fromString(punishment.getUuid());
                validPunishments++;
            } catch (Exception e) {
                invalidPunishments++;
                continue;
            }

            cache.put(uuid, punishment);
        }
//        System.out.println("Loaded " + validPunishments + " valid punishments and " + invalidPunishments + " invalid punishments");

        all.clear();
    }

    @Override
    public void clearCache() {
        cache.clear();
    }

    @Override
    public BukkitTask getCacheTask() {
        return task;
    }

    @Override
    public @NotNull Collection<PersistentPunishment> getPunishments(@NotNull UUID uuid, @Nullable PunishmentType type) {
        return (type == null ? cache.get(uuid) : cache.get(uuid).stream().filter(entry -> entry.getCategory().equals(type)).collect(Collectors.toList()));
    }

    private Collection<PersistentPunishment> loadPunishments(@NotNull PunishmentType type) {
        Collection<PersistentPunishment> collection = Lists.newArrayList();
        try (PreparedStatement statement = Database.get().prepareStatement("SELECT * FROM {" + type.getTableName() + "}")){
            try (ResultSet result = statement.executeQuery()){
                while(result.next()) {
                    long id = result.getLong("id");
                    String reason = result.getString("reason");
                    String bannedByName = result.getString("banned_by_name");
                    String uuid = result.getString("uuid");
                    long time = result.getLong("time");
                    long until = result.getLong("until");
                    boolean active = result.getBoolean("active");
                    String removed = null;
                    try {
                        removed = result.getString("removed_by_uuid");
                    } catch (Exception ignored) {

                    }

                    PersistentPunishment punishment = new PersistentPunishment(id, reason, type, bannedByName, uuid, time, until);

                    punishment.setActive(active);
                    punishment.setRemoved(removed);

                    collection.add(punishment);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return collection;
    }

    private class CacheTask extends BukkitRunnable {

        @Override
        public void run() {
            cache();
        }
    }
}
