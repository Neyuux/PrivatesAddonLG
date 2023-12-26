package fr.neyuux.privatesaddonlg.commands;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.annotations.PlayerCommand;
import fr.ph1lou.werewolfapi.commands.ICommand;
import fr.ph1lou.werewolfapi.enums.Sound;
import fr.ph1lou.werewolfapi.enums.StateGame;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.events.lovers.DonEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.lovers.ILover;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.player.utils.Formatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DonCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        WereWolfAPI game = Plugin.getINSTANCE().getGame();

        if (!(sender instanceof Player)) {
            return true;
        }

        if (!game.isState(StateGame.GAME))
            return true;

        if (args.length == 0) {
            sender.sendMessage(Plugin.getPrefixWithColor(ChatColor.LIGHT_PURPLE) + "§cVous devez mettre le % de vie que vous voulez donner en paramètre.");
            return true;
        }
        
        Player player = (Player) sender;
        int heart;
        String playerName = player.getName();
        UUID uuid = player.getUniqueId();
        IPlayerWW playerWW = game.getPlayerWW(uuid).orElse(null);
        if (playerWW == null) {
            return true;
        }
        if (playerWW.getLovers().isEmpty()) {
            playerWW.sendMessageWithKey("werewolf.prefix.red", "werewolf.lovers.lover.not_in_pairs", new Formatter[0]);
            return true;
        }
        try {
            heart = Integer.parseInt(args[0]);
        } catch (NumberFormatException ignored) {
            playerWW.sendMessageWithKey("werewolf.prefix.red", "werewolf.check.number_required", new Formatter[0]);
            return true;
        }
        if (heart >= 100) {
            playerWW.sendMessageWithKey("werewolf.prefix.green", "werewolf.lovers.lover.100", new Formatter[0]);
            return true;
        }
        if (args.length == 1) {
            List<ILover> lovers = playerWW.getLovers().stream().filter(loverAPI1 -> !loverAPI1.isKey("werewolf.lovers.cursed_lover.display")).filter(loverAPI1 -> !loverAPI1.isKey("werewolf.lovers.amnesiac_lover.display") || game.getConfig().getLoverCount("werewolf.lovers.amnesiac_lover.display") >= 1).collect(Collectors.toList());
            if (lovers.isEmpty()) {
                playerWW.sendMessageWithKey("werewolf.prefix.red", "werewolf.lovers.lover.not_in_pairs", new Formatter[0]);
                return true;
            }
            lovers.forEach(loverAPI1 -> {
                double health = player.getMaxHealth() * (double)heart / 100.0;
                AtomicReference<Double> temp = new AtomicReference<>(0.0);
                double don = health / (double)(loverAPI1.getLovers().size() - 1);
                List<IPlayerWW> lovers2 = loverAPI1.getLovers().stream().filter(playerWW1 -> !playerWW.equals(playerWW1)).filter(playerWW1 -> playerWW1.isState(StatePlayer.ALIVE)).collect(Collectors.toList());
                if (lovers2.isEmpty()) {
                    playerWW.sendMessageWithKey("werewolf.prefix.red", "werewolf.check.player_not_found", new Formatter[0]);
                    return;
                }
                if (health > player.getHealth()) {
                    playerWW.sendMessage(new TextComponent(Plugin.getPrefixWithColor(ChatColor.LIGHT_PURPLE) + "§cVous n'avez pas assez de vie pour faire ce don !"));
                    return;
                }
                lovers2.forEach(playerWW1 -> {
                    Player playerCouple = Bukkit.getPlayer(playerWW1.getUUID());
                    if (playerCouple != null) {
                        if (playerWW1.getMaxHealth() - playerCouple.getHealth() >= don) {
                            DonEvent donEvent = new DonEvent(playerWW, playerWW1, heart);
                            Bukkit.getPluginManager().callEvent(donEvent);
                            if (!donEvent.isCancelled()) {
                                playerCouple.setHealth(playerCouple.getHealth() + don);
                                temp.updateAndGet(v -> v + don);
                                playerWW1.sendMessageWithKey("werewolf.prefix.yellow", "werewolf.lovers.lover.received", Formatter.number(heart), Formatter.player(playerName));
                                playerWW.sendMessageWithKey("werewolf.prefix.green", "werewolf.lovers.lover.complete", Formatter.number(heart), Formatter.player(playerCouple.getName()));
                                playerWW.sendSound(Sound.PORTAL);
                            } else {
                                playerWW.sendMessageWithKey("werewolf.prefix.red", "werewolf.check.cancel", new Formatter[0]);
                            }
                        } else {
                            playerWW.sendMessageWithKey("werewolf.prefix.red", "werewolf.lovers.lover.too_many_heart", Formatter.player(playerCouple.getName()));
                        }
                    }
                });
                player.setHealth(player.getHealth() - temp.get());
            });
        } else {
            if (args[1].equals(playerName)) {
                playerWW.sendMessageWithKey("werewolf.prefix.red", "werewolf.check.not_yourself", new Formatter[0]);
                return true;
            }
            Player playerCouple = Bukkit.getPlayer(args[1]);
            if (playerCouple == null) {
                playerWW.sendMessageWithKey("werewolf.prefix.red", "werewolf.check.offline_player", new Formatter[0]);
                return true;
            }
            UUID argUUID = playerCouple.getUniqueId();
            IPlayerWW playerWW1 = game.getPlayerWW(argUUID).orElse(null);
            if (playerWW1 == null) {
                return true;
            }
            if (!playerWW1.isState(StatePlayer.ALIVE)) {
                playerWW.sendMessageWithKey("werewolf.prefix.red", "werewolf.check.player_not_found", new Formatter[0]);
                return true;
            }
            double don = player.getMaxHealth() * (double)heart / 100.0;
            if (don > player.getHealth()) {
                playerWW.sendMessage(new TextComponent(Plugin.getPrefixWithColor(ChatColor.LIGHT_PURPLE) + "§cVous n'avez pas assez de vie pour faire ce don !"));
                return true;
            }
            playerWW.getLovers().forEach(iLover -> {
                if (iLover.isKey("werewolf.lovers.cursed_lover.display")) {
                    playerWW.sendMessageWithKey("werewolf.prefix.red", "werewolf.lovers.lover.not_lover", new Formatter[0]);
                    return;
                }

                if (!iLover.getLovers().contains(playerWW1)) {
                    playerWW.sendMessageWithKey("werewolf.prefix.red", "werewolf.lovers.lover.not_in_pairs", new Formatter[0]);
                    return;
                }

                if (iLover.isKey("werewolf.lovers.amnesiac_lover.display") && game.getConfig().getLoverCount("werewolf.lovers.amnesiac_lover.display") == 0) {
                    playerWW.sendMessageWithKey("werewolf.prefix.red", "werewolf.lovers.lover.not_in_pairs", new Formatter[0]);
                    return;
                }


                if (playerWW1.getMaxHealth() - playerCouple.getHealth() >= (double)heart) {
                    DonEvent donEvent = new DonEvent(playerWW, playerWW1, heart);
                    Bukkit.getPluginManager().callEvent(donEvent);
                    if (!donEvent.isCancelled()) {
                        playerCouple.setHealth(playerCouple.getHealth() + don);
                        player.setHealth(player.getHealth() - don);
                        playerWW1.sendMessageWithKey("werewolf.prefix.yellow", "werewolf.lovers.lover.received", Formatter.number(heart), Formatter.player(playerName));
                        playerWW.sendMessageWithKey("werewolf.prefix.green", "werewolf.lovers.lover.complete", Formatter.number(heart), Formatter.player(playerCouple.getName()));
                        playerWW.sendSound(Sound.PORTAL);
                    } else {
                        playerWW.sendMessageWithKey("werewolf.prefix.red", "werewolf.check.cancel", new Formatter[0]);
                    }
                } else {
                    playerWW.sendMessageWithKey("werewolf.prefix.red", "werewolf.lovers.lover.too_many_heart", Formatter.player(playerCouple.getName()));
                }

            });
        }

        return true;
    }
}
