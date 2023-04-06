package games.negative.punishments.core.implementation;

import games.negative.punishments.api.managers.GUIManager;
import games.negative.punishments.api.structure.config.ConfigurableGUI;
import games.negative.punishments.api.structure.config.ConfigurableItem;
import games.negative.punishments.core.util.ConfigUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class GUIManagerProvider extends GUIManager {

    private final ArrayList<ConfigurableGUI> guis = new ArrayList<>();

    public GUIManagerProvider() {
        setInstance(this);

        reload();
    }

    @Override
    public ArrayList<ConfigurableGUI> getGUIs() {
        return guis;
    }

    @Override
    public Optional<ConfigurableGUI> getGUI(String input) {
        return guis.stream().filter(gui -> gui.getId().equalsIgnoreCase(input)).findFirst();
    }

    @Override
    public void reload() {
        guis.clear();
        FileConfiguration config = new ConfigUtils("guis").getConfig();

        config.getConfigurationSection("").getKeys(false).forEach(menuID -> {

            String title = config.getString(menuID + ".title");
            int rows = config.getInt(menuID + ".rows");
            List<Integer> fillerList = new ArrayList<>();
            boolean enabled = config.getBoolean(menuID + ".enabled", true);
            config.getStringList(menuID + ".fillers").forEach(fillerPosition -> fillerList.add(Integer.parseInt(fillerPosition)));

            String fillerMat = config.getString(menuID + ".filler-item.material");
            int fillerData = config.getInt(menuID + ".filler-item.data");

            Map<String, ConfigurableItem> slots = new HashMap<>();
            config.getConfigurationSection(menuID + ".slots").getKeys(false).forEach(slotID -> {

                String name = config.getString(menuID + ".slots." + slotID + ".name");
                List<String> lore = config.getStringList(menuID + ".slots." + slotID + ".lore");
                String material = config.getString(menuID + ".slots." + slotID + ".material");
                int data = config.getInt(menuID + ".slots." + slotID + ".data");
                int position = config.getInt(menuID + ".slots." + slotID + ".position");

                ConfigurableItem configurableItem = new ConfigurableItem(name, Material.valueOf(material), (byte) data, lore, position);
                slots.put(slotID, configurableItem);
            });

            ConfigurableItem configurableItem = new ConfigurableItem(" ", Material.valueOf(fillerMat), (byte) fillerData, Collections.emptyList(), 0);
            ConfigurableGUI gui = new ConfigurableGUI(menuID, title, rows, fillerList, enabled, slots, configurableItem);
            guis.add(gui);
        });
    }
}
