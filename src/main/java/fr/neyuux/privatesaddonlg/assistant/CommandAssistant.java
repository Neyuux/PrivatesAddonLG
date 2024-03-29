package fr.neyuux.privatesaddonlg.assistant;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.neyuux.privatesaddonlg.listeners.WorldChangesListener;
import fr.ph1lou.werewolfapi.game.IConfiguration;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.versions.VersionUtils;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
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

    @Getter
    private final AssistantCompo compo;

    @Getter
    private final AssistantConfig config;

    @Getter
    private final AssistantScenarios scenarios;


    public CommandAssistant(Plugin main) {
        this.main = main;
        this.compo = new AssistantCompo(main);
        this.config = new AssistantConfig(main);
        this.scenarios = new AssistantScenarios(main);
    }


    public static String getPrefix() {
        return "§3§lAssistant §8» §r";
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!main.isLoaded())
            return true;

        if (!(sender instanceof Player))
            return true;

        Player player = (Player)sender;

        if (args.length == 0) {
            sender.sendMessage(getPrefix() + "§cVous devez renseigner un argument. Argument disponibles : " + options
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
        List<TextComponent> summaryScens = this.scenarios.getSummary();
        List<TextComponent> summaryCompo = compo.getSummary(this.getPlayerCount());
        List<TextComponent> summaryConfig = this.config.getSummary();

        switch (args[0]) {

            case "clickablemessage":
                if (args.length > 1) {
                    switch (args[1]) {
                        case "removecouples":
                            main.getRegisterManager().getLoversRegister()
                                    .forEach(loverRegister -> config.setLoverCount(loverRegister.getMetaDatas().key(), 0));

                            sender.sendMessage(getPrefix() + "§fCouples supprimés avec §asuccès §f!");
                            player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            break;

                        case "setprotectiondiamond":
                            if (args.length > 2 && StringUtils.isNumeric(args[2])) {
                                config.setLimitProtectionDiamond(Integer.parseInt(args[2]));

                                sender.sendMessage(getPrefix() + "§fLimite de Protection en §bDiamant §fmis à §a§l"+args[2]+" §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "setprotectioniron":
                            if (args.length > 2 && StringUtils.isNumeric(args[2])) {
                                config.setLimitProtectionIron(Integer.parseInt(args[2]));

                                sender.sendMessage(getPrefix() + "§fLimite de Protection en §7Fer §fmis à §a§l"+args[2]+" §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "setpower":
                            if (args.length > 2 && StringUtils.isNumeric(args[2])) {
                                config.setLimitPowerBow(Integer.parseInt(args[2]));

                                sender.sendMessage(getPrefix() + "§fLimite de Puissance §fmis à §a§l"+args[2]+" §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "setsharpnessiron":
                            if (args.length > 2 && StringUtils.isNumeric(args[2])) {
                                config.setLimitSharpnessIron(Integer.parseInt(args[2]));

                                sender.sendMessage(getPrefix() + "§fLimite de Tranchant en §7Fer §fmis à §a§l"+args[2]+" §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "setsharpnessdiamond":
                            if (args.length > 2 && StringUtils.isNumeric(args[2])) {
                                config.setLimitSharpnessDiamond(Integer.parseInt(args[2]));

                                sender.sendMessage(getPrefix() + "§fLimite de Tranchant en §bDiamant §fmis à §a§l"+args[2]+" §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "setrandomcouplecupid":
                            if (args.length > 2) {
                                config.setConfig(args[2] + ".roles.cupid.configurations.random_cupid", true);

                                sender.sendMessage(getPrefix() + "§fLe §dCouple Aléatoire §fdu Cupidon a été §aactivé §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "removeautorezwitch":
                            if (args.length > 2) {
                                config.setConfig(args[2] + ".roles.witch.configurations.auto_rez_witch", false);

                                sender.sendMessage(getPrefix() + "§fL'§dAuto-Résurrection §fde la Sorcière a été §cdésactivée §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "removeautorezipdl":
                            if (args.length > 2) {
                                config.setConfig(args[2] + ".roles.cupid.configurations.auto_rez", false);

                                sender.sendMessage(getPrefix() + "§fL'§dAuto-Résurrection §fde l'IPDL a été §cdésactivée §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "setcroupier":
                            if (args.length > 2) {
                                config.setConfig(args[2] + ".roles.croupier.configurations.croupier_every_other_day", true);

                                sender.sendMessage(getPrefix() + "§fL'option \"§aCroupier§f un jour sur deux\" a été §aactivée §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "activate":
                            if (args.length > 2) {
                                config.setConfig(args[2], true);

                                sender.sendMessage(getPrefix() + "§f\"§e" + game.translate(args[2]) + "§f\" a été §aactivé §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "desactivate":
                            if (args.length > 2) {
                                config.setConfig(args[2], false);

                                sender.sendMessage(getPrefix() + "§f\"§e" + game.translate(args[2]) + "§f\" a été §cdésactivé §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "set":
                            if (args.length > 3 && StringUtils.isNumeric(args[3])) {
                                config.setValue(args[2], Integer.parseInt(args[3]));

                                sender.sendMessage(getPrefix() + "§f\"§e" + game.translate(args[2]).replace("&number&%", "").replace("%", "").trim() + "§f\" a été mis à §a§l"+args[3]+" §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "desactivatescenario":
                            if (args.length > 2) {
                                config.setScenario(args[2], false);

                                sender.sendMessage(getPrefix() + "§f\"§e" + game.translate(args[2]) + "§f\" a été §cdésactivé §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "activatescenario":
                            if (args.length > 2) {
                                config.setScenario(args[2], true);

                                sender.sendMessage(getPrefix() + "§f\"§e" + game.translate(args[2]) + "§f\" a été §aactivé §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;

                        case "addrole":
                            if (args.length > 2) {
                                config.addOneRole(args[2]);
                                game.setTotalRoles(game.getTotalRoles() + 1);

                                sender.sendMessage(getPrefix() + "§fVous avez ajouté un §e"+game.translate(args[2])+" §favec succès !");
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                            }
                            break;
                    }
                }
                break;

            case "compo":
            case "roles":

                if (!summaryCompo.isEmpty()) {
                    player.sendMessage("§f§m                                                                           §r");
                    player.sendMessage(getPrefix() + "§dConseils pour améliorer la composition : ");
                    player.sendMessage("");

                    summaryCompo
                            .forEach(msg -> player.spigot().sendMessage(msg));
                    player.sendMessage("§f§m                                                                           §r");

                } else {
                    player.sendMessage(getPrefix() + "§aL'Assistant n'a plus de conseils à donner pour la composition.");
                }
                break;

            case "map":
            case "roofed":
                if (this.checkRoofedSize()) {
                    player.sendMessage("§f§m                                                                           §r");
                    player.sendMessage(getPrefix() + "§2Conseils pour améliorer la carte : ");

                    player.spigot().sendMessage(this.getClickableMessageRoofedSize());

                    player.sendMessage("§f§m                                                                           §r");
                } else {
                    player.sendMessage(getPrefix() + "§aL'Assistant n'a plus de conseils à donner pour la carte.");
                }
                break;

            case "config":
                if (!summaryConfig.isEmpty()) {
                    player.sendMessage("§f§m                                                                           §r");
                    player.sendMessage(getPrefix() + "§eConseils pour améliorer la configuration : ");
                    player.sendMessage("");

                    summaryConfig
                            .forEach(msg -> player.spigot().sendMessage(msg));
                    player.sendMessage("§f§m                                                                           §r");
                } else {
                    player.sendMessage(getPrefix() + "§aL'Assistant n'a plus de conseils à donner pour la configuration.");
                }
                break;

            case "scenarios":
            case "scenario":
                if (!summaryScens.isEmpty()) {
                    player.sendMessage("§f§m                                                                           §r");
                    player.sendMessage(getPrefix() + "§bConseils pour améliorer les scénarios : ");
                    player.sendMessage("");

                    summaryScens
                            .forEach(msg -> player.spigot().sendMessage(msg));
                    player.sendMessage("§f§m                                                                           §r");
                } else {
                    player.sendMessage(getPrefix() + "§aL'Assistant n'a plus de conseils à donner pour les scénarios.");
                }
                break;

            case "save":
                sender.sendMessage(Plugin.getPrefix() + "§c-----------COMING SOON-----------");
                break;

            case "storage":
                sender.sendMessage(Plugin.getPrefix() + "§c-----------COMING SOON-----------");
                break;

            case "all":
                if (summaryCompo.isEmpty() && summaryConfig.isEmpty() && summaryScens.isEmpty() && !this.checkRoofedSize()) {
                    player.sendMessage(getPrefix() + "§aL'Assistant n'a plus de conseils à donner. Vous avez fini le jeu. (Neyuux_ n'a plus le droit de se plaindre ez)");
                    return true;
                }

                player.sendMessage("§f§m                                                                           §r");
                player.sendMessage(getPrefix() + "§bConseils pour §3§l"+this.getPlayerCount()+"§b joueurs :");

                if (!summaryCompo.isEmpty()) {
                    player.sendMessage("");
                    player.sendMessage(" §3§l■ §dConseils sur la composition : ");
                    player.sendMessage("");

                    summaryCompo
                            .forEach(msg -> player.spigot().sendMessage(insertSpace(msg)));
                }

                if (this.checkRoofedSize()) {
                    player.sendMessage("");
                    player.sendMessage(" §3§l■ §2Conseils sur la carte : ");
                    player.sendMessage("");

                    player.spigot().sendMessage(insertSpace(this.getClickableMessageRoofedSize()));
                }

                if (!summaryConfig.isEmpty()) {
                    player.sendMessage("");
                    player.sendMessage(" §3§l■ §eConseils sur la configuration : ");
                    player.sendMessage("");

                    summaryConfig
                            .forEach(msg -> player.spigot().sendMessage(insertSpace(msg)));
                }

                if (!summaryScens.isEmpty()) {
                    player.sendMessage("");
                    player.sendMessage(" §3§l■ §bConseils sur les scénarios : ");
                    player.sendMessage("");

                    summaryScens
                            .forEach(msg -> player.spigot().sendMessage(insertSpace(msg)));
                }

                player.sendMessage("§f§m                                                                           §r");
                break;

            case "setplayers":
            case "players":
            case "setcount":
                if (args.length > 1 && StringUtils.isNumeric(args[1])) {
                    this.playerCount = Integer.parseInt(args[1]);

                    player.sendMessage(getPrefix() + "Le nombre de joueurs a bien été mis à §7§l" + args[1] + "§f.");
                    player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);

                } else player.sendMessage(Plugin.getPrefixWithColor(ChatColor.RED) + "§cPrécisez le nombre de joueurs de la partie.");
                break;

            case "lg":
            case "baselg":
                player.sendMessage("§f§m                                                                           §r");

                player.sendMessage(" §0§l■ §cNombre de LGs recommandé : §l" + compo.getRecommandedLGs(this.getPlayerCount()));
                player.spigot().sendMessage(compo.getClickableMessageBaseLG(compo.checkBaseWerewolves(this.getPlayerCount())));

                player.sendMessage("§f§m                                                                           §r");
                break;

            case "lgpotential":
            case "totallg":
            case "lgtotal":
            case "potentiallg":
                player.sendMessage("§f§m                                                                           §r");

                player.sendMessage(" §0§l■ §cNombre de LGs potentiels recommandé : §l" + compo.getRecommandedPotentialLGs(this.getPlayerCount()));
                player.spigot().sendMessage(compo.getClickableMessagePotentialLG(compo.checkPotentialWerewolves(this.getPlayerCount())));

                player.sendMessage("§f§m                                                                           §r");
                break;

            case "informations":
            case "info":
            case "infos":
                player.sendMessage("§f§m                                                                           §r");

                player.spigot().sendMessage(compo.getClickableMessageInfos(compo.checkInformationRoles(this.getPlayerCount())));

                player.sendMessage("§f§m                                                                           §r");
                break;

            case "informationspoints":
            case "points":
            case "infospoints":
            case "infopoints":
            case "pointsinfo":
                InformationsPointsGUI.INVENTORY.open(player);
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
        else if (this.getPlayerCount() <= 22)
            return WorldChangesListener.RoofedSize.MEDIUM;
        else if (this.getPlayerCount() <= 25)
            return WorldChangesListener.RoofedSize.LARGE;
        else
            return WorldChangesListener.RoofedSize.XXL;
    }

    public boolean checkRoofedSize() {
        return Plugin.getINSTANCE().getWorldListener().getSize() != this.getRecommandedRoofedSize();
    }

    public TextComponent getClickableMessageRoofedSize() {
        return VersionUtils.getVersionUtils().createClickableText(" §0§l■ §fMettre la taille de la map en §a" + this.getRecommandedRoofedSize(), "/a roofedsize " + this.getRecommandedRoofedSize().name(), ClickEvent.Action.RUN_COMMAND, "Cliquez ici pour mettre la taille de la roofed en §a" + this.getRecommandedRoofedSize());
    }


    private static TextComponent insertSpace(TextComponent component) {
        TextComponent newComponent = new TextComponent(component);
        String text = component.getText();
        if (text.length() > 6) {
            String firstPart = text.substring(0, 5);
            String secondPart = text.substring(5);
            newComponent.setText(firstPart + " " + secondPart);
        }
        return newComponent;
    }
}
