package games.negative.punishments.core.structure;

import lombok.Data;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Data
public class PersistentOfflinePlayer {

    private final UUID uniqueID;
    private final String name;

    public static PersistentOfflinePlayer ofOfflinePlayer(OfflinePlayer player) {
        return new PersistentOfflinePlayer(player.getUniqueId(), player.getName());
    }

    public static PersistentOfflinePlayer ofFloodgatePlayer(@NotNull UUID uuid, @NotNull String gamerTag) {
        return new PersistentOfflinePlayer(uuid, gamerTag);
    }

}
