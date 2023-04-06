package games.negative.punishments.menus.history;

import games.negative.punishments.api.structure.config.ConfigurableGUI;
import games.negative.punishments.api.structure.config.ConfigurableItem;
import games.negative.punishments.core.structure.PersistentOfflinePlayer;
import games.negative.framework.gui.GUI;
import games.negative.framework.util.ItemBuilder;
import games.negative.framework.util.version.VersionChecker;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HistoryMenu extends GUI {

    public HistoryMenu(Player staff, PersistentOfflinePlayer offender, ConfigurableGUI gui) {
        super(gui.getTitle(), gui.getRows());

        VersionChecker versionChecker = VersionChecker.getInstance();

        ItemStack filler;
        ConfigurableItem fillerItem = gui.getFiller();
        boolean legacy = versionChecker.isLegacy();
        if (legacy)
            filler = new ItemBuilder(fillerItem.getMaterial()).setDurability(fillerItem.getData()).setName(" ").build();
        else
            filler = new ItemBuilder(fillerItem.getMaterial()).setName(" ").build();

        if (!gui.getFillerSlots().isEmpty())
            gui.getFillerSlots().forEach(integer -> setItem((integer - 1), player -> filler));

        ConfigurableItem banItem = gui.getSlots().get("bans");
        if (banItem == null) {
            System.out.println("Could not find the item in gui `history` named `bans`");
            return;
        }

        ItemBuilder bans;
        if (legacy)
            bans = new ItemBuilder(banItem.getMaterial()).setDurability(banItem.getData());
        else
            bans = new ItemBuilder(banItem.getMaterial());

        bans.setName(banItem.getDisplayName());

        setItemClickEvent((banItem.getSlot() - 1), player -> bans.build(), (player, event) -> {
            new HistoryBanMenu(staff, offender,  1).open(player);
        });

        ConfigurableItem muteItem = gui.getSlots().get("mutes");
        if (muteItem == null) {
            System.out.println("Could not find the item in gui `history` named `mutes`");
            return;
        }

        ItemBuilder mutes;
        if (legacy)
            mutes = new ItemBuilder(muteItem.getMaterial()).setDurability(muteItem.getData());
        else
            mutes = new ItemBuilder(muteItem.getMaterial());

        mutes.setName(muteItem.getDisplayName());

        setItemClickEvent((muteItem.getSlot() - 1), player -> mutes.build(), (player, event) -> {
            new HistoryMuteMenu(staff, offender,  1).open(player);
        });

        ConfigurableItem warnsItem = gui.getSlots().get("warns");
        if (warnsItem == null) {
            System.out.println("Could not find the item in gui `history` named `warns`");
            return;
        }

        ItemBuilder warns;
        if (legacy)
            warns = new ItemBuilder(warnsItem.getMaterial()).setDurability(warnsItem.getData());
        else
            warns = new ItemBuilder(warnsItem.getMaterial());

        warns.setName(warnsItem.getDisplayName());

        setItemClickEvent((warnsItem.getSlot() - 1), player -> warns.build(), (player, event) -> {
            new HistoryWarnMenu(staff, offender,  1).open(player);
        });

        ConfigurableItem kicksItem = gui.getSlots().get("kicks");
        if (kicksItem == null) {
            System.out.println("Could not find the item in gui `history` named `kicks`");
            return;
        }

        ItemBuilder kicks;
        if (legacy)
            kicks = new ItemBuilder(kicksItem.getMaterial()).setDurability(kicksItem.getData());
        else
            kicks = new ItemBuilder(kicksItem.getMaterial());

        kicks.setName(kicksItem.getDisplayName());

        setItemClickEvent((kicksItem.getSlot() - 1), player -> kicks.build(), (player, event) -> {
            new HistoryKickMenu(staff, offender,  1).open(player);
        });

    }

}
