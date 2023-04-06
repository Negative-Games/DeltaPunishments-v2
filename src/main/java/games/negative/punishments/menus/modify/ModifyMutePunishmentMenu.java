package games.negative.punishments.menus.modify;

import games.negative.punishments.api.PunishAPI;
import games.negative.punishments.api.PunishDataManager;
import games.negative.punishments.api.PunishManager;
import games.negative.punishments.api.structure.PersistentPunishment;
import games.negative.punishments.core.Locale;
import games.negative.punishments.core.util.Permissions;
import games.negative.framework.gui.GUI;
import games.negative.framework.inputlistener.InputListener;
import games.negative.framework.message.Message;
import games.negative.framework.util.ItemBuilder;
import games.negative.framework.util.TimeUtil;
import games.negative.framework.util.version.VersionChecker;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ModifyMutePunishmentMenu extends GUI {
    public ModifyMutePunishmentMenu(PersistentPunishment punishment) {
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

        setItemClickEvent(15, player -> removePunishment, (player, event) -> {
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


        ItemStack unPunish = new ItemBuilder(Material.BARRIER).setName("&c&lUnmute User").build();
        setItemClickEvent(13, player -> unPunish, (player, event) -> {
            player.closeInventory();

            String uuid = punishment.getUuid();
            if (uuid.length() < 10) {
                // Invalid uuid?
                new Message("&cThe UUID of the user seems to be invalid...").send(player);
                return;
            }

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
            player.performCommand("unmute " + offlinePlayer.getName());
        });

        ItemStack updateTime;
        if (legacy)
            updateTime = new ItemBuilder(Material.WATCH).setName("&e&lUpdate Duration").build();
        else
            updateTime = new ItemBuilder(Material.valueOf("CLOCK")).setName("&e&lUpdate Duration").build();

        setItemClickEvent(11, player -> updateTime, (player, event) -> {

            player.closeInventory();
            Locale.NEW_DURATION.send(player);
            InputListener.listen(player.getUniqueId(), response -> {
                String message = response.getMessage();
                response.setCancelled(true);
                if (message.equalsIgnoreCase("cancel"))
                    return;

                Long time = null;
                if (message.equalsIgnoreCase("perm"))
                    time = -1L;
                if (time == null) {
                    try {
                        time = TimeUtil.longFromString(message);
                    } catch (Exception e) {
                        Locale.USE_VALID_NUMBER.send(response.getPlayer());
                        return;
                    }
                }

                assert dataManager != null;
                dataManager.updateTime(punishment.getCategory(), punishment.getId(), time).whenCompleteAsync((unused, throwable)
                        -> Locale.MODIFIED_DURATION.replace("%s%", message).send(player));

            });

        });
    }
}
