package games.negative.punishments.menus;

import games.negative.punishments.api.PunishAPI;
import games.negative.punishments.api.PunishManager;
import games.negative.punishments.api.managers.GUIManager;
import games.negative.punishments.api.structure.PersistentPunishment;
import games.negative.punishments.api.structure.Punishment;
import games.negative.punishments.api.structure.config.ConfigurableGUI;
import games.negative.punishments.api.structure.config.ConfigurableItem;
import games.negative.framework.gui.GUI;
import games.negative.framework.message.Message;
import games.negative.framework.util.ItemBuilder;
import games.negative.framework.util.Utils;
import games.negative.framework.util.version.VersionChecker;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PunishMenu extends GUI {
    public PunishMenu(Player staff, OfflinePlayer offender, ConfigurableGUI gui, Collection<PersistentPunishment> punishments) {
        super(new Message(gui.getTitle()).replace("%offender%", offender.getName()).getMessage(), gui.getRows());

        VersionChecker versionChecker = VersionChecker.getInstance();

        boolean legacy = versionChecker.isLegacy();
        ConfigurableItem fillerItem = gui.getFiller();
        ItemStack filler;
        if (legacy)
            filler = new ItemBuilder(fillerItem.getMaterial()).setDurability(fillerItem.getData()).setName(" ").build();
        else
            filler = new ItemBuilder(fillerItem.getMaterial()).setName(" ").build();

        if (!gui.getFillerSlots().isEmpty())
            gui.getFillerSlots().forEach(integer -> setItem((integer - 1), player -> filler));

        UUID uuid = offender.getUniqueId();
        PunishManager punishManager = PunishAPI.getInstance().getPunishManager();

        gui.getSlots().entrySet().stream().filter(stringMapEntry -> punishManager.findPunishment(stringMapEntry.getKey()).isPresent()).forEach(entry -> {
            assert punishManager.findPunishment(entry.getKey()).isPresent();
            Punishment punishment = punishManager.findPunishment(entry.getKey()).get();
            int amountOfPunishments = (int) punishments.stream()
                    .filter(persistentPunishment -> persistentPunishment.getReason().equalsIgnoreCase(punishment.getReason()))
                    .count();

            ConfigurableItem configurableItem = entry.getValue();

            ItemBuilder item;
            if (legacy) {
                item = new ItemBuilder(configurableItem.getMaterial())
                        .setDurability(configurableItem.getData())
                        .setName("&c" + configurableItem.getDisplayName());
            } else {
                item = new ItemBuilder(configurableItem.getMaterial())
                        .setName("&c" + configurableItem.getDisplayName());
            }
            List<String> lore = configurableItem.getLore();
            List<String> filteredLore = new ArrayList<>();

            lore.forEach(s -> {
                s = s.replaceAll("%total%", Utils.decimalFormat((long) amountOfPunishments));
                filteredLore.add(s);
            });

            item.setLore(filteredLore);

            setItemClickEvent((configurableItem.getSlot() - 1), player -> item.build(), (player, event) -> {
                Optional<ConfigurableGUI> confirmGUI = GUIManager.getInstance().getGUI("confirm-punishment");
                if (!confirmGUI.isPresent()) {
                    System.out.println("Could not find GUI named `confirm-punishment`");
                    return;
                }

                ConfigurableGUI confirm = confirmGUI.get();
                if (confirm.isEnabled())
                    new PunishmentConfirmMenu(offender, punishment, confirm).open(player);
                else
                    punishManager.executePunishment(player, offender, punishment, true, false);
            });

        });


    }

}
