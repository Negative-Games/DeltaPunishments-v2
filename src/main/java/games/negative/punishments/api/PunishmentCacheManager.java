package games.negative.punishments.api;

import games.negative.punishments.api.structure.PersistentPunishment;
import games.negative.punishments.api.structure.PunishmentType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public interface PunishmentCacheManager {

    void cache();

    void clearCache();

    BukkitTask getCacheTask();

    @NotNull
    Collection<PersistentPunishment> getPunishments(@NotNull UUID uuid, @Nullable PunishmentType type);

}
