package games.negative.punishments.core.structure;

import lombok.Data;

@Data
public class SQLCredentials {

    private final String host;
    private final String port;
    private final String database;
    private final String username;
    private final String password;

    private final String bansTable;
    private final String mutesTable;
    private final String kicksTable;
    private final String warnsTable;

}
