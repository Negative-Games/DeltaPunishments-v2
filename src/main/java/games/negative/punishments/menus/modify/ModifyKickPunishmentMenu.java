package games.negative.punishments.menus.modify;

import games.negative.punishments.api.PunishAPI;
import games.negative.punishments.api.PunishDataManager;
import games.negative.punishments.api.PunishManager;
import games.negative.punishments.api.structure.PersistentPunishment;
import games.negative.punishments.core.Locale;
import games.negative.punishments.core.util.Permissions;
import games.negative.framework.gui.GUI;
import games.negative.framework.util.ItemBuilder;
import games.negative.framework.util.version.VersionChecker;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ModifyKickPunishmentMenu extends GUI {
    public ModifyKickPunishmentMenu(PersistentPunishment punishment) {
        super("&6&lModifying Punishment &e&l" + punishment.getId(), 3);

        VersionChecker versionChecker = VersionChecker.getInstance();
        PunishManager punishManager = PunishAPI.getInstance().getPunishManager();
        PunishDataManager dataManager = punishManager.getDataManager();

        ItemStack filler;
        boolean legacy = versionChecker.isLegacy();
        if (legacy)
            filler = new ItemBuilder(Material.STAINED_GLASS_PANE).setDurability((short) 15).setName(" ").build();
        else
            filler = new ItemBuilder(Material.valueOf("BLACK_STAINED_GLASS_PANE")).setName(" ").build();

        for (int i = 0; i < (3 * 9); i++) {
            setItem(i, player -> filler);
        }

        ItemStack removePunishment = new ItemBuilder(Material.BEDROCK).setName("&4&lRemove Punishment")
                .addLoreLine("&c&lNOTE! &7This will remove this ban from the Database!").build();

        setItemClickEvent(13, player -> removePunishment, (player, event) -> {
            if (!player.hasPermission(Permissions.DATABASE)) {
                Locale.NO_PERM.send(player);
                return;
            }

            assert dataManager != null;
            dataManager.deletePunishment(punishment).whenCompleteAsync((unused, throwable) -> {
                Locale.REMOVED_FROM_DATABASE.send(player);
                player.closeInventory();
            });
        });

    }
}
