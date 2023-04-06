package games.negative.punishments.menus;

import games.negative.punishments.api.PunishAPI;
import games.negative.punishments.api.PunishManager;
import games.negative.punishments.api.structure.Punishment;
import games.negative.punishments.api.structure.config.ConfigurableGUI;
import games.negative.punishments.api.structure.config.ConfigurableItem;
import games.negative.framework.gui.GUI;
import games.negative.framework.util.ItemBuilder;
import games.negative.framework.util.version.VersionChecker;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

public class PunishmentConfirmMenu extends GUI {
    public PunishmentConfirmMenu(OfflinePlayer offender, Punishment punishment, ConfigurableGUI gui) {
        super(gui.getTitle(), gui.getRows());

        VersionChecker versionChecker = VersionChecker.getInstance();

        PunishManager punishManager = PunishAPI.getInstance().getPunishManager();

        boolean legacy = versionChecker.isLegacy();
        ConfigurableItem fillerItem = gui.getFiller();
        ItemStack filler;
        if (legacy)
            filler = new ItemBuilder(fillerItem.getMaterial()).setDurability(fillerItem.getData()).setName(" ").build();
        else
            filler = new ItemBuilder(fillerItem.getMaterial()).setName(" ").build();

        if (!gui.getFillerSlots().isEmpty())
            gui.getFillerSlots().forEach(integer -> setItem((integer - 1), player -> filler));

        ConfigurableItem silentItem = gui.getSlots().get("silent");
        if (silentItem == null) {
            System.out.println("Could not find the item in gui `confirm-punishment` named `silent`");
            return;
        }

        ItemBuilder silent;
        if (legacy)
            silent = new ItemBuilder(silentItem.getMaterial()).setDurability(silentItem.getData());
        else
            silent = new ItemBuilder(silentItem.getMaterial());

        silent.setName(silentItem.getDisplayName());

        setItemClickEvent((silentItem.getSlot() - 1), player -> silent.build(), (player, event) ->
                punishManager.executePunishment(player, offender, punishment, true, false));

        ConfigurableItem publicItem = gui.getSlots().get("public");
        if (publicItem == null) {
            System.out.println("Could not find the item in gui `confirm-punishment` named `public`");
            return;
        }

        ItemBuilder pubic;
        if (legacy)
            pubic = new ItemBuilder(publicItem.getMaterial()).setDurability(publicItem.getData());
        else
            pubic = new ItemBuilder(publicItem.getMaterial());

        pubic.setName(publicItem.getDisplayName());

        setItemClickEvent((publicItem.getSlot() - 1), player -> pubic.build(), (player, event) ->
                punishManager.executePunishment(player, offender, punishment, false, false));
    }
}
