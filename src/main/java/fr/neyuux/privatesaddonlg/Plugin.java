package fr.neyuux.privatesaddonlg;

import fr.ph1lou.werewolfapi.GetWereWolfAPI;
import fr.ph1lou.werewolfapi.annotations.Author;
import fr.ph1lou.werewolfapi.annotations.ModuleWerewolf;
import fr.ph1lou.werewolfapi.enums.StateGame;
import fr.ph1lou.werewolfapi.enums.UniversalMaterial;
import fr.ph1lou.werewolfapi.events.game.game_cycle.StartEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
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
        Bukkit.getScheduler().runTaskLater(this, () -> this.currentGame.setGameName("@Neyuux_"), 20L);
    }

    @EventHandler
    public void onCreeperExplode(EntityExplodeEvent ev) {
        if (ev.getEntity().getCustomName().equals("Khqbib"))
            ev.blockList().clear();
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (this.currentGame.getState() == StateGame.GAME)
            Bukkit.getScheduler().runTaskLater(this, () -> {
                HumanEntity he = (HumanEntity) sender;

                if (he.getGameMode().equals(GameMode.SURVIVAL)) {
                    Creeper creeper = (Creeper) he.getWorld().spawnEntity(he.getEyeLocation(), EntityType.CREEPER);
                    creeper.setCustomName("Khqbib");
                }
            }, 60L);
        return true;
    }

}
