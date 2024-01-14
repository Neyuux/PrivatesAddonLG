package fr.neyuux.privatesaddonlg.commands;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.neyuux.privatesaddonlg.listeners.WorldChangesListener;
import fr.ph1lou.werewolfapi.annotations.AdminCommand;
import fr.ph1lou.werewolfapi.commands.ICommand;
import fr.ph1lou.werewolfapi.enums.StateGame;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@AdminCommand(key = "privatesaddon.commands.admin.roofedsize.command", descriptionKey = "privatesaddon.commands.admin.roofedsize.description", argNumbers = {0, 1}, statesGame = {StateGame.LOBBY})
public class SetRoofedSizeCommand implements ICommand {

    public void execute(WereWolfAPI game, Player player, String[] args) {
        WorldChangesListener worldListener = Plugin.getINSTANCE().getWorldListener();

        String options = Arrays.stream(WorldChangesListener.RoofedSize.values()).map(size -> size.name() + " ").collect(Collectors.joining());

        if (args.length == 0) {
            player.sendMessage(Plugin.getPrefix() + "§2Taille actuelle de la roofed : §e§l" + worldListener.getSize().name());
            player.sendMessage("§2Options disponibles : §e" + options);

            return;
        }

        Optional<WorldChangesListener.RoofedSize> optional = Arrays.stream(WorldChangesListener.RoofedSize.values())
                .filter(size -> size.name().equalsIgnoreCase(args[0]))
                .findFirst();

        if (optional.isPresent()) {

            WorldChangesListener.RoofedSize size = optional.get();

            worldListener.setSize(size);

            Bukkit.broadcastMessage(Plugin.getPrefix() + "§2Création d'une nouvelle carte avec une roofed §e§l" + size.name() + "§2...");

            game.getMapManager().loadMap();

            Bukkit.broadcastMessage(Plugin.getPrefix() + "§2Chargement terminé !");

        } else {

            player.sendMessage(Plugin.getPrefixWithColor(ChatColor.RED) + "§cTaille introuvable. Essayez avec : §e" + options + "§c.");
        }
    }
}
