package fr.neyuux.privatesaddonlg;

import fr.neyuux.privatesaddonlg.commands.CommandPioche;
import fr.neyuux.privatesaddonlg.commands.CommandSay;
import fr.ph1lou.werewolfapi.GetWereWolfAPI;
import fr.ph1lou.werewolfapi.annotations.Author;
import fr.ph1lou.werewolfapi.annotations.ModuleWerewolf;
import fr.ph1lou.werewolfapi.enums.StateGame;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.enums.UniversalMaterial;
import fr.ph1lou.werewolfapi.events.game.game_cycle.StartEvent;
import fr.ph1lou.werewolfapi.events.game.life_cycle.ResurrectionEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.utils.ItemBuilder;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;


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

    private final HashSet<LivingEntity> customEntities = new HashSet<>();


    @Override
    public void onEnable() {
        this.ww = getServer().getServicesManager().load(GetWereWolfAPI.class);

        INSTANCE = this;

        PluginManager pm = this.getServer().getPluginManager();
        CommandPioche commandPioche = new CommandPioche();

        pm.registerEvents(this, this);
        pm.registerEvents(new WorldChangesListener(), this);
        pm.registerEvents(commandPioche, this);
        pm.registerEvents(new RoleBuffListener(), this);
        
        this.getCommand("manon").setExecutor(this);
        this.getCommand("say").setExecutor(new CommandSay());
        this.getCommand("pioche").setExecutor(commandPioche);

        super.onEnable();
    }


    @EventHandler
    public void onGameStart(StartEvent ev) {
        Bukkit.getScheduler().runTaskLater(this, () -> this.getGame().setGameName("@Neyuux_"), 20L);
        this.groupsWarning.clear();
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        if (this.customEntities.stream().anyMatch(livingEntity -> livingEntity.getUniqueId().equals(event.getEntity().getUniqueId()))) {
            event.getDrops().clear();
            event.setDroppedExp(0);
            if (event.getEntityType() == EntityType.WOLF)
                event.getDrops().add(new ItemBuilder(Material.BONE).setDisplayName("Bon toutou ♥").build());
        }
    }

    @EventHandler
    public void onCustomDamage(EntityDamageByEntityEvent ev) {
        if (this.customEntities.stream().anyMatch(livingEntity -> livingEntity.getUniqueId().equals(ev.getDamager().getUniqueId())))
            ev.setDamage(0);
    }

    @EventHandler
    public void onPlayerDeathBugPotions(PlayerDeathEvent ev) {
        Bukkit.getScheduler().runTaskLater(Plugin.getINSTANCE(), () -> ev.getEntity().damage(20), 9L);
    }

    @EventHandler
    public void onResurrection(ResurrectionEvent ev){
        if (!ev.isCancelled())
            ev.getPlayerWW().addPlayerHealth(32);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player && this.getGame() != null) {
            Player player = (Player) sender;

            if ((this.getGame().getState() == StateGame.START ||this.getGame().getState() == StateGame.GAME) && player.getGameMode().equals(GameMode.SURVIVAL)) {

                Location loc = player.getLocation().add(3, 0, 0);
                Villager manon = (Villager) player.getWorld().spawnEntity(this.getHighestLoc(loc), EntityType.VILLAGER);

                manon.setProfession(Villager.Profession.BLACKSMITH);
                manon.setCustomName("§dMa__non");

                Wolf khqbib = this.createChien(loc.getWorld(), loc.add(2, 0, 0), "Khqbib", true);
                Wolf neyzz = this.createChien(loc.getWorld(), loc.add(-4, 0, 0), "NeyZz", false);
                Wolf sotark = this.createChien(loc.getWorld(), loc.add(0, 0, 2), "Sotark_", false);

                this.removeCustomEntities(5, manon, khqbib, neyzz, sotark);
            }
        }
        return true;
    }


    private void removeCustomEntities(int seconds, LivingEntity... entities) {
        this.customEntities.addAll(Arrays.asList(entities));

        Bukkit.getScheduler().runTaskLater(this, () -> {
            this.customEntities.forEach(livingEntity -> {
                if (livingEntity != null && !livingEntity.isDead())
                    livingEntity.damage(10000);
            });
        }, 20L * seconds);
    }

    private Wolf createChien(World world, Location loc, String name, boolean enraged) {
        Wolf wolf = (Wolf) world.spawnEntity(loc, EntityType.WOLF);

        wolf.setCustomName(name);
        wolf.setAngry(enraged);
        return wolf;
    }

    private Location getHighestLoc(Location loc) {
        return new Location(loc.getWorld(), loc.getX(), loc.getWorld().getHighestBlockYAt(loc), loc.getZ());
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
