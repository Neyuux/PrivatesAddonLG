package fr.neyuux.privatesaddonlg.assistant;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.neyuux.privatesaddonlg.listeners.WorldChangesListener;
import fr.ph1lou.werewolfapi.game.IConfiguration;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandAssistant implements CommandExecutor, TabCompleter {

    private final Plugin main;

    private int playerCount = -1;

    private static final List<String> options = Collections.unmodifiableList(Arrays.asList("compo", "map", "config", "players", "scenarios", "save", "storage", "all", "lg", "lgpotential", "infos", "points"));

    private final AssistantCompo compo;
    private final AssistantConfig config;

    public CommandAssistant(Plugin main) {
        this.main = main;
        this.compo = new AssistantCompo(main);
        this.config = new AssistantConfig(main);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!main.isLoaded())
            return true;

        if (!(sender instanceof Player))
            return true;

        Player player = (Player)sender;

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
        IConfiguration config = game.getConfig();

        switch (args[0]) {

            case "clickablemessage":
                if (args.length > 1) {
                    switch (args[1]) {
                        case "removecouples":
                            main.getRegisterManager().getLoversRegister()
                                    .forEach(loverRegister -> config.setLoverCount(loverRegister.getMetaDatas().key(), 0));

                            sender.sendMessage(Plugin.getPrefix() + "§fCouples supprimés avec §asuccès §f!");
                            player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            break;

                        case "setprotectiondiamond":
                            if (args.length > 2 && StringUtils.isNumeric(args[2])) {
                                config.setLimitProtectionDiamond(Integer.parseInt(args[2]));

                                sender.sendMessage(Plugin.getPrefix() + "§fLimite de Protection en §bDiamant §fmis à §a§l"+args[2]+" §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "setprotectioniron":
                            if (args.length > 2 && StringUtils.isNumeric(args[2])) {
                                config.setLimitProtectionIron(Integer.parseInt(args[2]));

                                sender.sendMessage(Plugin.getPrefix() + "§fLimite de Protection en §7Fer §fmis à §a§l"+args[2]+" §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "setpower":
                            if (args.length > 2 && StringUtils.isNumeric(args[2])) {
                                config.setLimitPowerBow(Integer.parseInt(args[2]));

                                sender.sendMessage(Plugin.getPrefix() + "§fLimite de Puissance §fmis à §a§l"+args[2]+" §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "setsharpnessiron":
                            if (args.length > 2 && StringUtils.isNumeric(args[2])) {
                                config.setLimitSharpnessIron(Integer.parseInt(args[2]));

                                sender.sendMessage(Plugin.getPrefix() + "§fLimite de Tranchant en §7Fer §fmis à §a§l"+args[2]+" §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "setsharpnessdiamond":
                            if (args.length > 2 && StringUtils.isNumeric(args[2])) {
                                config.setLimitSharpnessDiamond(Integer.parseInt(args[2]));

                                sender.sendMessage(Plugin.getPrefix() + "§fLimite de Tranchant en §bDiamant §fmis à §a§l"+args[2]+" §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "setrandomcouplecupid":
                            if (args.length > 2) {
                                config.setConfig(args[2] + ".roles.cupid.configurations.random_cupid", true);

                                sender.sendMessage(Plugin.getPrefix() + "§fLe §dCouple Aléatoire §fdu Cupidon a été §aactivé §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "removeautorezwitch":
                            if (args.length > 2) {
                                config.setConfig(args[2] + ".roles.cupid.configurations.auto_rez_witch", false);

                                sender.sendMessage(Plugin.getPrefix() + "§fL'§dAuto-Résurrection §fde la Sorcière a été §cdésactivée §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "removeautorezipdl":
                            if (args.length > 2) {
                                config.setConfig(args[2] + ".roles.cupid.configurations.auto_rez", false);

                                sender.sendMessage(Plugin.getPrefix() + "§fL'§dAuto-Résurrection §fde l'IPDL a été §cdésactivée §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "activate":
                            if (args.length > 2) {
                                config.setConfig(args[2], true);

                                sender.sendMessage(Plugin.getPrefix() + "§f\"§e" + game.translate(args[2]) + "§f\" a été §aactivé §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "desactivate":
                            if (args.length > 2) {
                                config.setConfig(args[2], false);

                                sender.sendMessage(Plugin.getPrefix() + "§f\"§e" + game.translate(args[2]) + "§f\" a été §cdésactivé §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "set":
                            if (args.length > 3 && StringUtils.isNumeric(args[3])) {
                                config.setValue(args[2], Integer.parseInt(args[3]));

                                sender.sendMessage(Plugin.getPrefix() + "§f\"§e" + game.translate(args[2]) + "§f\" a été mis à §a§l"+args[3]+" §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "desactivatescenario":
                            if (args.length > 2) {
                                config.setScenario(args[2], false);

                                sender.sendMessage(Plugin.getPrefix() + "§f\"§e" + game.translate(args[2]) + "§f\" a été §cdésactivé §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "activatescenario":
                            if (args.length > 2) {
                                config.setScenario(args[2], true);

                                sender.sendMessage(Plugin.getPrefix() + "§f\"§e" + game.translate(args[2]) + "§f\" a été §aactivé §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;
                    }
                }
                break;

            case "compo":
            case "roles":

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
        return (playerCount == -1 ? main.getGame().getPlayersCount() : playerCount);
    }


    public WorldChangesListener.RoofedSize getRecommandedRoofedSize() {
        if (this.getPlayerCount() <= 17)
            return WorldChangesListener.RoofedSize.SMALL;
        else if (this.getPlayerCount() <= 23)
            return WorldChangesListener.RoofedSize.MEDIUM;
        else
            return WorldChangesListener.RoofedSize.LARGE;
    }

    public boolean checkRoofedSize() {
        return Plugin.getINSTANCE().getWorldListener().getSize() != this.getRecommandedRoofedSize();
    }
}
