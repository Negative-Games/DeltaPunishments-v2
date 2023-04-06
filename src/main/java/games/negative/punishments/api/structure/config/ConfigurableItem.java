package games.negative.punishments.api.structure.config;

import lombok.Data;
import org.bukkit.Material;

import java.util.List;

@Data
public class ConfigurableItem {

    private final String displayName;
    private final Material material;
    private final byte data;
    private final List<String> lore;
    private final int slot;

}
