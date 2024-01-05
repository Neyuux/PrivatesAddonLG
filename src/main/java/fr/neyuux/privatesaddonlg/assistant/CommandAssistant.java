package fr.neyuux.privatesaddonlg.assistant;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandAssistant implements CommandExecutor, TabCompleter {

    private final Plugin main;

    private int playerCount = -1;

    private static final List<String> options = Collections.unmodifiableList(Arrays.asList("compo", "map", "config", "players", "scenarios", "save", "storage", "all", "lg", "lgpotential", "infos", "points"));

    public CommandAssistant(Plugin main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!main.isLoaded())
            return true;

        if (args.length == 0) {
            sender.sendMessage(Plugin.getPrefix() + "§cVous devez renseigner un argument. Argument disponibles : " + options
                    .stream()
                    .map(s -> {
                        if (options.get(options.size() - 1).equals(s))
                            return "§e" + s;

                        return "§e" + s + "§c, ";
                    })
                    .collect(Collectors.joining()));
            return true;
        }

        WereWolfAPI game = main.getGame();

        switch (args[0]) {
            case "compo":
                break;

            case "map":
            case "roofed":
                break;

            case "config":
                break;

            case "scenarios":
            case "scenario":
                break;

            case "save":
                sender.sendMessage(Plugin.getPrefix() + "§c-----------COMING SOON-----------");
                break;

            case "storage":
                sender.sendMessage(Plugin.getPrefix() + "§c-----------COMING SOON-----------");
                break;

            case "all":
                break;

            case "setplayers":
            case "players":
            case "setcount":
                break;

            case "lg":
            case "baselg":
                break;

            case "lgpotential":
            case "totallg":
            case "lgtotal":
            case "potentiallg":
                break;

            case "informations":
            case "info":
            case "infos":
                break;

            case "informationspoints":
            case "points":
            case "infospoints":
            case "infopoints":
            case "pointsinfo":
                break;
        }

        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length > 0)
            return options.stream().filter(s -> s.startsWith(args[0])).collect(Collectors.toList());

        return options;
    }



    public int getPlayerCount() {
        if (!main.isLoaded()) return -1;
        return (this.playerCount == -1 ? main.getGame().getPlayersCount() : this.playerCount);
    }
}
