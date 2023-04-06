package games.negative.punishments.api.structure;

import lombok.Data;

/**
 * Represents a Punishment Entry
 */
@Data
public class PersistentPunishment {

    private final long id;
    private final String reason;
    private final PunishmentType category;
    private final String bannedBy;
    private final String uuid;
    private final long time;
    private final long until;
    private boolean active;
    private String removed;


}
