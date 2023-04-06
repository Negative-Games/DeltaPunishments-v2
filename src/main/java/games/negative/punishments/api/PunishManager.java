package games.negative.punishments.api;

import games.negative.punishments.api.structure.Punishment;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public interface PunishManager {

     /**
      * Find a punishment through a String
      * @param input Input
      * @return If the string is valid punishment, return. If not, return empty.
      */
     Optional<Punishment> findPunishment(@NotNull String input);

     /**
      * Code to execute a punishment
      * @param staff Staff Member
      * @param offender Offender
      * @param punishment Punishment
      * @param silent Is the punishment silent?
      * @param skipToFinal Skip to the final offense?
      */
     void executePunishment(CommandSender staff, OfflinePlayer offender, Punishment punishment, boolean silent, boolean skipToFinal);

     /**
      * Get all active punishable offenses
      * @return {@link Map} of {@link Punishment}
      */
     Map<String, Punishment> getPunishmentMap();

     void reload();

     @Nullable
     PunishDataManager getDataManager();
    
}
