package games.negative.punishments.commands;

import games.negative.punishments.DeltaPunishments;
import games.negative.framework.command.Command;
import games.negative.framework.command.annotation.CommandInfo;
import games.negative.framework.util.HasteBin;
import litebans.api.Database;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

/*
    Extremely scuffed code just to temporarily see if we can get the SQL process list when a leak happens.
 */

@CommandInfo(name = "processlist", description = "get process list", consoleOnly = true)
public class CommandGetProcessList extends Command {
    @Override
    public void onCommand(CommandSender commandSender, String[] strings) {
        new DelayedProcessList().runTaskLaterAsynchronously(DeltaPunishments.getInstance(), 5);
    }

    private void getProcessList() {
        ArrayList<String> dump = new ArrayList<>();
        try (PreparedStatement st = Database.get().prepareStatement("SHOW PROCESSLIST")) {
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("Id");
                    String user = rs.getString("User");
                    String host = rs.getString("Host");
                    String db = rs.getString("db");
                    String command = rs.getString("Command");
                    String time = rs.getString("Time");
                    String state = rs.getString("State");
                    String info = rs.getString("Info");
                    dump.add("====================================");
                    dump.add("id:" + id);
                    dump.add("user:" + user);
                    dump.add("host:" + host);
                    dump.add("db:" + db);
                    dump.add("command:" + command);
                    dump.add("time:" + time);
                    dump.add("state:" + state);
                    dump.add("info:" + info);
                    dump.add("====================================");
                }
                StringBuilder dumpBuilder = new StringBuilder();
                dump.forEach(k -> dumpBuilder.append(k).append("\n"));
                String[] dumpLog = new String[]{dumpBuilder.toString()};
                StringBuilder urlBuilder = new StringBuilder();
                Arrays.stream(dumpLog).forEach(line -> urlBuilder.append(line).append("\n"));
                try {
                    String url = new HasteBin().post(urlBuilder.toString(), false);
                    System.out.println(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                close(rs, st);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void close(@Nullable ResultSet result, @Nullable PreparedStatement statement) {
        if (result != null) {
            try {
                result.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @RequiredArgsConstructor
    private class DelayedProcessList extends BukkitRunnable {
        @Override
        public void run() {
            getProcessList();
        }
    }
}
