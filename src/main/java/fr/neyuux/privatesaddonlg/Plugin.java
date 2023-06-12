package fr.neyuux.privatesaddonlg;

import fr.ph1lou.werewolfapi.GetWereWolfAPI;
import fr.ph1lou.werewolfapi.annotations.Author;
import fr.ph1lou.werewolfapi.annotations.ModuleWerewolf;
import fr.ph1lou.werewolfapi.enums.StateGame;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.enums.UniversalMaterial;
import fr.ph1lou.werewolfapi.events.game.game_cycle.StartEvent;
import fr.ph1lou.werewolfapi.events.game.game_cycle.WinEvent;
import fr.ph1lou.werewolfapi.events.game.utils.WinConditionsCheckEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.utils.ItemBuilder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;


@ModuleWerewolf(key = "privatesaddon.name",
        loreKeys = {"privatesaddon.description"},
        item = UniversalMaterial.COBBLESTONE,
        defaultLanguage = "fr_FR",
        authors = @Author(uuid = "0234db8c-e6e5-45e5-8709-ea079fa575bb", name = "Neyuux_"))
public class Plugin extends JavaPlugin implements Listener, CommandExecutor {

    @Getter
    private static final String Prefix = "§bPrivatesAddon §8» §r";

    private GetWereWolfAPI ww;

    @Getter
    private static Plugin INSTANCE;

    @Getter
    private final HashMap<UUID, Integer> groupsWarning = new HashMap<>();


    @Override
    public void onEnable() {
        this.ww = getServer().getServicesManager().load(GetWereWolfAPI.class);

        INSTANCE = this;

        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getPluginManager().registerEvents(new WorldChangesListener(), this);

        this.getCommand("coco").setExecutor(this);
        this.getCommand("say").setExecutor(new CommandSay());
        this.getCommand("pioche").setExecutor(new CommandPioche());

        super.onEnable();
    }


    @EventHandler
    public void onGameStart(StartEvent ev) {
        Bukkit.getScheduler().runTaskLater(this, () -> this.getGame().setGameName("@Neyuux_"), 20L);
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        if (isCoco(event.getEntity())) {
            event.getDrops().clear();
            event.setDroppedExp(0);
            event.getDrops().add(new ItemBuilder(Material.COBBLESTONE).setDisplayName("Repas de Coco").build());
        }
    }

    @EventHandler
    public void onCocoDamage(EntityDamageByEntityEvent ev) {
        if (isCoco(ev.getDamager()))
            ev.setDamage(0);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player && this.getGame() != null) {
            Player player = (Player) sender;

            if ((this.getGame().getState() == StateGame.START ||this.getGame().getState() == StateGame.GAME) && player.getGameMode().equals(GameMode.SURVIVAL)) {

                Enderman enderMan = (Enderman) player.getWorld().spawnEntity(player.getLocation(), EntityType.ENDERMAN);

                enderMan.setHealth(4);
                enderMan.setCustomName("thatnwordcoco");
                enderMan.setCustomNameVisible(true);
                enderMan.setCarriedMaterial(new MaterialData(Material.COBBLESTONE));
                Bukkit.getScheduler().runTaskLater(this, () -> enderMan.setTarget(player), 10L);

                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/entitydata @e[type=Enderman,name=thatnwordcoco] {Attributes:[{Name:\"generic.movementSpeed\",Base:0.8f}]}");
            }
        }
        return true;
    }
    
    private boolean isCoco(Entity entity) {
        return entity instanceof Enderman && entity.getCustomName().equals("thatnwordcoco");
    }


    public String getGroupsWarningRatio(UUID uuid) {
        if (this.getGame() == null)
            return "NaN";

        Optional<IPlayerWW> optionalplayerWW = this.getGame().getPlayerWW(uuid);

        if (!optionalplayerWW.isPresent())
            return "NaN";

        IPlayerWW playerWW = optionalplayerWW.get();

        float time = (playerWW.isState(StatePlayer.ALIVE) ? this.getGame().getTimer() : playerWW.getDeathTime()) / 60f;

        return String.format("%.2f", this.groupsWarning.get(uuid) / time);
    }

    public WereWolfAPI getGame() {
        return this.ww.getWereWolfAPI();
    }
}
