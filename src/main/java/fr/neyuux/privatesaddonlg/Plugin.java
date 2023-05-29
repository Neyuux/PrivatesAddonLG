package fr.neyuux.privatesaddonlg;

import fr.ph1lou.werewolfapi.GetWereWolfAPI;
import fr.ph1lou.werewolfapi.annotations.Author;
import fr.ph1lou.werewolfapi.annotations.ModuleWerewolf;
import fr.ph1lou.werewolfapi.enums.UniversalMaterial;
import fr.ph1lou.werewolfapi.events.game.game_cycle.StartEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


@ModuleWerewolf(key = "privatesaddon.name",
        loreKeys = {"privatesaddon.description"},
        item = UniversalMaterial.COBBLESTONE,
        defaultLanguage = "fr_FR",
        authors = @Author(uuid = "0234db8c-e6e5-45e5-8709-ea079fa575bb", name = "Neyuux_"))
public class Plugin extends JavaPlugin implements Listener, CommandExecutor {

    @Getter
    private static final String Prefix = "§bPrivatesAddon §8» §r";

    @Getter
    private WereWolfAPI currentGame;


    @Override
    public void onEnable() {

        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getPluginManager().registerEvents(new WorldChangesListener(), this);

        this.getCommand("khqbib").setExecutor(this);

        GetWereWolfAPI ww = getServer().getServicesManager().load(GetWereWolfAPI.class);

        this.currentGame = ww.getWereWolfAPI();

        super.onEnable();
    }


    @EventHandler
    public void onGameStart(StartEvent ev) {
        this.currentGame.setGameName("@Neyuux_");
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        sender.sendMessage(getPrefix() + "§a§lFuck Khqbib");
        Bukkit.getOnlinePlayers().stream().filter(player -> player.getUniqueId().toString().equals("f4943527-fce1-4552-bc02-70b130cb273b")).findFirst().ifPresent(player -> player.sendMessage(getPrefix() + "§c§l" + sender.getName() + " §afucks you"));
        return true;
    }

}
