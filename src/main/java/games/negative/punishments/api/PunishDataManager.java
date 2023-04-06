package games.negative.punishments.api;

import games.negative.punishments.api.structure.PersistentPunishment;
import games.negative.punishments.api.structure.PunishmentType;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Represents the Data Management class which
 * will be used to gather and manipulate punishments
 * across plugins.
 */
public interface PunishDataManager {

    Collection<PersistentPunishment> getBans();

    Collection<PersistentPunishment> getBans(@NotNull UUID uuid);

    Collection<PersistentPunishment> getBans(@NotNull Player player);

    Collection<PersistentPunishment> getBans(@NotNull OfflinePlayer player);

    Collection<PersistentPunishment> getMutes();

    Collection<PersistentPunishment> getMutes(@NotNull UUID uuid);

    Collection<PersistentPunishment> getMutes(@NotNull Player player);

    Collection<PersistentPunishment> getMutes(@NotNull OfflinePlayer player);

    Collection<PersistentPunishment> getKicks();

    Collection<PersistentPunishment> getKicks(@NotNull UUID uuid);

    Collection<PersistentPunishment> getKicks(@NotNull Player player);

    Collection<PersistentPunishment> getKicks(@NotNull OfflinePlayer player);

    Collection<PersistentPunishment> getWarns();

    Collection<PersistentPunishment> getWarns(@NotNull UUID uuid);

    Collection<PersistentPunishment> getWarns(@NotNull Player player);

    Collection<PersistentPunishment> getWarns(@NotNull OfflinePlayer player);

    /**
     * Get all punishments of a specified {@link PunishmentType}.
     * @param type {@link PunishmentType}
     * @return {@link Collection} of {@link PersistentPunishment}
     */
    CompletableFuture<Collection<PersistentPunishment>> getPunishments(@NotNull PunishmentType type);

    /**
     * Gets all punishments of a specific {@link UUID}
     * @param uuid {@link UUID}
     * @return {@link Collection} of {@link PersistentPunishment}
     */
    CompletableFuture<Collection<PersistentPunishment>> getPunishments(@NotNull UUID uuid);

    /**
     * Gets all punishments of a specific {@link Player}
     * @param player {@link Player}
     * @return {@link Collection} of {@link PersistentPunishment}
     */
    CompletableFuture<Collection<PersistentPunishment>> getPunishments(@NotNull Player player);

    /**
     * Gets all punishments of a specific {@link OfflinePlayer}
     * @param player {@link OfflinePlayer}
     * @return {@link Collection} of {@link PersistentPunishment}
     */
    CompletableFuture<Collection<PersistentPunishment>> getPunishments(@NotNull OfflinePlayer player);


    /**
     * Updates a Punishment in the database
     * @param category Category of the Punishment
     * @param id ID to the Punishment
     * @param updateFunction Code to be run before update
     */
    CompletableFuture<Void> updatePunishment(PunishmentType category, long id, @NotNull Consumer<PersistentPunishment> updateFunction);

    /**
     * Deletes the Punishment from the database
     * @param category Category of the Punishment
     * @param id ID of the Punishment
     */
    CompletableFuture<Void> deletePunishment(PunishmentType category, long id);

    /**
     * Deletes the Punishment from the database
     * @param punishment {@link PersistentPunishment}
     */
    CompletableFuture<Void> deletePunishment(@NotNull PersistentPunishment punishment);

    /**
     * Update the duration of the Punishment in the database
     * @param category Category of the Punishment
     * @param id ID of the Punishment
     * @param duration Duration of the Punishment in {@link Long}
     */
    CompletableFuture<Void> updateTime(PunishmentType category, long id, long duration);

    /**
     * Update the activity of the Punishment
     * @param category Category of the Punishment
     * @param id ID of the Punishment
     * @param active Will the Punishment be active?
     */
    CompletableFuture<Void> updateActive(PunishmentType category, long id, boolean active);
}
