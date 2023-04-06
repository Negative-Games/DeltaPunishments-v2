package games.negative.punishments.api;

import games.negative.punishments.api.structure.PersistentPunishment;
import games.negative.punishments.api.structure.PunishmentType;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.util.Collection;
import java.util.UUID;

public interface PunishSQLManager {

    Connection getConnection();

    Collection<PersistentPunishment> getPunishments(@NotNull UUID uuid, @NotNull PunishmentType type);

}
