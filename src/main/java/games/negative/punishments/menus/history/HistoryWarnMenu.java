package games.negative.punishments.menus.history;

import games.negative.punishments.DeltaPunishments;
import games.negative.punishments.api.managers.GUIManager;
import games.negative.punishments.api.structure.PersistentPunishment;
import games.negative.punishments.api.structure.PunishmentType;
import games.negative.punishments.api.structure.config.ConfigurableGUI;
import games.negative.punishments.core.structure.PersistentOfflinePlayer;
import games.negative.punishments.menus.modify.ModifyWarningPunishmentMenu;
import games.negative.framework.gui.GUI;
import games.negative.framework.util.ItemBuilder;
import games.negative.framework.util.TimeUtil;
import games.negative.framework.util.version.VersionChecker;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class HistoryWarnMenu extends GUI {
    public HistoryWarnMenu(Player staff, PersistentOfflinePlayer offender, int page) {
        super("&6&lViewing Warns of &e&l" + offender.getName(), 5);

        VersionChecker versionChecker = VersionChecker.getInstance();

        ItemStack filler;
        if (versionChecker.isLegacy())
            filler = new ItemBuilder(Material.STAINED_GLASS_PANE).setDurability((short) 15).setName(" ").build();
        else
            filler = new ItemBuilder(Material.valueOf("BLACK_STAINED_GLASS_PANE")).setName(" ").build();

        for (int i = 0; i < 9; i++) {
            setItem(i, player -> filler);
        }

        for (int i = 36; i < 45; i++) {
            setItem(i, player -> filler);
        }


        long limit = 27;

        Collection<PersistentPunishment> raw = DeltaPunishments.getInstance().getCacheManager().getPunishments(offender.getUniqueID(), PunishmentType.WARN);

        LinkedList<PersistentPunishment> warns = raw.stream()
                .sorted(Comparator.comparingLong(PersistentPunishment::getTime).reversed())
                .skip((page - 1) * limit).limit(limit)
                .collect(Collectors.toCollection(LinkedList::new));

        for (PersistentPunishment punishment : warns) {
            long originDate = punishment.getTime();
            long now = System.currentTimeMillis();
            String bannedBy = punishment.getBannedBy();

            String originFormatted = TimeUtil.format(now, originDate) + " ago";

            List<String> lore = new ArrayList<>();
            lore.add(" ");
            lore.add("&7Warned by: &f" + bannedBy);
            lore.add("&7Reason: &c" + punishment.getReason());
            lore.add(" ");
            lore.add("&7Original Warn Date");
            lore.add("&f" + originFormatted);
            lore.add(" ");
            lore.add("&e&oClick to modify punishment");

            ItemStack record = new ItemBuilder(Material.REDSTONE_BLOCK).setName("&c" + punishment.getId())
                    .setLore(lore)
                    .build();

            addItemClickEvent(player -> record, (player, event) -> {
                player.closeInventory();
                new ModifyWarningPunishmentMenu(punishment).open(player);
            });
        }

        if (warns.size() > page * limit) {
            ItemStack next = new ItemBuilder(Material.ARROW).setName("&aNext Page").build();
            setItemClickEvent(44, player -> next, (player, event) ->
                    new HistoryWarnMenu(staff, offender, page + 1).open(player));
        }

        if (page > 1) {
            ItemStack previous = new ItemBuilder(Material.ARROW).setName("&cPrevious Page").build();
            setItemClickEvent(36, player -> previous, (player, event) ->
                    new HistoryWarnMenu(staff, offender, page - 1).open(player));
        }

        ItemStack mainMenu = new ItemBuilder(Material.NETHER_STAR).setName("&b&lReturn to Main Menu").build();
        Optional<ConfigurableGUI> gui = GUIManager.getInstance().getGUI("history");
        if (!gui.isPresent()) {
            System.out.println("Could not find GUI named `history`");
            return;
        }

        setItemClickEvent(40, player -> mainMenu, (player, event) -> new HistoryMenu(staff, offender, gui.get()).open(player));
    }
}
