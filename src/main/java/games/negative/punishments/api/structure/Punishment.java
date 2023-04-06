package games.negative.punishments.api.structure;

import lombok.Data;

import java.util.List;

/**
 * Represents a Punishment Type from punishments.yml
 */
@Data
public class Punishment {

    private final String id;
    private final String reason;

    // The "forgiveness-offset" is the amount of time that must
    // pass before a player can be "forgiven" for a punishment
    // and would not be considered in the stackables calculation
    private final long forgivenessOffset;
    private List<String> stackables;

}
