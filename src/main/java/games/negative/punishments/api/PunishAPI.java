package games.negative.punishments.api;

import lombok.Getter;
import lombok.Setter;

public abstract class PunishAPI {

    @Getter @Setter
    private static PunishAPI instance;

    public abstract PunishManager getPunishManager();

}
