package games.negative.punishments.menus.history;

import games.negative.punishments.DeltaPunishments;
import games.negative.punishments.api.managers.GUIManager;
import games.negative.punishments.api.structure.PersistentPunishment;
import games.negative.punishments.api.structure.PunishmentType;
import games.negative.punishments.api.structure.config.ConfigurableGUI;
import games.negative.punishments.core.structure.PersistentOfflinePlayer;
import games.negative.punishments.menus.modify.ModifyBanPunishmentMenu;
import games.negative.framework.gui.GUI;
import games.negative.framework.util.ItemBuilder;
import games.negative.framework.util.TimeUtil;
import games.negative.framework.util.version.VersionChecker;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class HistoryBanMenu extends GUI {
    public HistoryBanMenu(Player staff, PersistentOfflinePlayer offender, int page) {
        super("&6&lViewing Bans of &e&l" + offender.getName(), 5);

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

        Collection<PersistentPunishment> rawBans = DeltaPunishments.getInstance().getCacheManager().getPunishments(offender.getUniqueID(), PunishmentType.BAN);
        LinkedList<PersistentPunishment> bans = rawBans.stream()
                .sorted(Comparator.comparingLong(PersistentPunishment::getTime).reversed())
                .skip((page - 1) * limit).limit(limit)
                .collect(Collectors.toCollection(LinkedList::new));

        for (PersistentPunishment punishment : bans) {
            long originDate = punishment.getTime();
            long removalDate = punishment.getUntil();
            long now = System.currentTimeMillis();
            boolean active = punishment.isActive();
            String removedBy = punishment.getRemoved();
            String bannedBy = punishment.getBannedBy();

            String originFormatted = TimeUtil.format(now, originDate) + " ago";
            String untilFormatted = (removalDate != 0 ?
                    (now > removalDate ? TimeUtil.format(now, removalDate) + " ago"
                            : TimeUtil.format(removalDate, now)) : "None");

            List<String> lore = new ArrayList<>();
            if (!active) {
                lore.add(" ");
                lore.add("&9&lTHIS PUNISHMENT HAS EXPIRED");
            }
            if (removedBy != null && !removedBy.contains("n/a")) {
                String removed = (removedBy.equalsIgnoreCase("Console") ? "Console" : Bukkit.getOfflinePlayer(UUID.fromString(removedBy)).getName());
                lore.add(" ");
                lore.add("&7(Removed by &e" + removed + "&7)");
            }

            lore.add(" ");
            lore.add("&7Banned by: &f" + bannedBy);
            lore.add("&7Reason: &c" + punishment.getReason());
            lore.add(" ");
            lore.add("&7Original Ban Date");
            lore.add("&f" + originFormatted);
            lore.add(" ");
            lore.add("&7Ban Removal Date");
            lore.add("&f" + untilFormatted);
            lore.add(" ");
            lore.add("&e&oClick to modify punishment");

            ItemStack record = new ItemBuilder(Material.REDSTONE_BLOCK).setName("&c" + punishment.getId())
                    .setLore(lore)
                    .build();

            addItemClickEvent(player -> record, (player, event) -> {
                player.closeInventory();
                new ModifyBanPunishmentMenu(punishment).open(player);
            });
        }

        if (rawBans.size() > page * limit) {
            ItemStack next = new ItemBuilder(Material.ARROW).setName("&aNext Page").build();
            setItemClickEvent(44, player -> next, (player, event) ->
                    new HistoryBanMenu(staff, offender, page + 1).open(player));
        }

        if (page > 1) {
            ItemStack previous = new ItemBuilder(Material.ARROW).setName("&cPrevious Page").build();
            setItemClickEvent(36, player -> previous, (player, event) ->
                    new HistoryBanMenu(staff, offender, page - 1).open(player));
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
