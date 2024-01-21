package fr.neyuux.privatesaddonlg;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.minuskube.inv.InventoryManager;
import fr.neyuux.privatesaddonlg.assistant.CommandAssistant;
import fr.neyuux.privatesaddonlg.commands.CommandDLimits;
import fr.neyuux.privatesaddonlg.commands.CommandPioche;
import fr.neyuux.privatesaddonlg.commands.CommandSay;
import fr.neyuux.privatesaddonlg.commands.DonCommand;
import fr.neyuux.privatesaddonlg.commands.roles.ItemCommand;
import fr.neyuux.privatesaddonlg.commands.roles.TranscenderCommand;
import fr.neyuux.privatesaddonlg.listeners.ArmorListener;
import fr.neyuux.privatesaddonlg.listeners.RoleBuffListener;
import fr.neyuux.privatesaddonlg.listeners.WorldChangesListener;
import fr.neyuux.privatesaddonlg.utils.Reflection;
import fr.ph1lou.werewolfapi.GetWereWolfAPI;
import fr.ph1lou.werewolfapi.annotations.Author;
import fr.ph1lou.werewolfapi.annotations.ModuleWerewolf;
import fr.ph1lou.werewolfapi.enums.RoleAttribute;
import fr.ph1lou.werewolfapi.enums.StateGame;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.enums.UniversalMaterial;
import fr.ph1lou.werewolfapi.events.ActionBarEvent;
import fr.ph1lou.werewolfapi.events.game.game_cycle.StartEvent;
import fr.ph1lou.werewolfapi.events.game.game_cycle.StopEvent;
import fr.ph1lou.werewolfapi.events.game.game_cycle.WinEvent;
import fr.ph1lou.werewolfapi.events.game.life_cycle.ResurrectionEvent;
import fr.ph1lou.werewolfapi.events.game.permissions.UpdateModeratorNameTagEvent;
import fr.ph1lou.werewolfapi.events.game.timers.RepartitionEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.registers.IRegisterManager;
import fr.ph1lou.werewolfapi.role.interfaces.IRole;
import fr.ph1lou.werewolfapi.utils.ItemBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.jline.internal.InputStreamReader;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;


@ModuleWerewolf(key = "privatesaddon.name",
        loreKeys = {"privatesaddon.description"},
        item = UniversalMaterial.COBBLESTONE,
        defaultLanguage = "fr_FR",
        authors = @Author(uuid = "0234db8c-e6e5-45e5-8709-ea079fa575bb", name = "Neyuux_"))
public class Plugin extends JavaPlugin implements Listener, CommandExecutor {

    @Getter
    private static final String Prefix = "§bPrivatesAddon §8» §r";

    public static String getPrefixWithColor(ChatColor color) {
        return color + "PrivatesAddon §8» §r";
    }


    private GetWereWolfAPI ww;

    @Getter
    private static Plugin INSTANCE;

    @Getter
    private WorldChangesListener worldListener;

    @Getter
    private CommandAssistant assistant;

    @Getter
    private final HashMap<UUID, Integer> groupsWarning = new HashMap<>();

    private final HashSet<LivingEntity> customEntities = new HashSet<>();

    private final List<EntityPlayer> customNPCs = new ArrayList<>();

    private final HashMap<UUID, HashMap<IPlayerWW, String>> neyuuxCommand = new HashMap<>();

    private int customCount = 0;

    private final HashSet<Consumer<WereWolfAPI>> serviceLoadTasks = new HashSet<>();


    @Override
    public void onEnable() {
        this.ww = getServer().getServicesManager().load(GetWereWolfAPI.class);

        INSTANCE = this;

        new BukkitRunnable() {
            @Override
            public void run() {
                getServer().getServicesManager().getRegistrations(GetWereWolfAPI.class).stream().filter(provider -> provider.getService().equals(GetWereWolfAPI.class)).findFirst().ifPresent(provider ->
                        ww = provider.getProvider());

                if (ww != null && ww.getWereWolfAPI() != null) {
                    Bukkit.getLogger().info("PrivatesAddon -> Service WereWolfAPI found");
                    Plugin.this.serviceLoadTasks.forEach(consumer -> consumer.accept(ww.getWereWolfAPI()));
                    cancel();
                }
            }
        }.runTaskTimer(this, 1L, 40L);

        PluginManager pm = this.getServer().getPluginManager();
        CommandPioche commandPioche = new CommandPioche();

        this.assistant = new CommandAssistant(this);
        this.worldListener = new WorldChangesListener();

        pm.registerEvents(this, this);
        pm.registerEvents(this.worldListener, this);
        pm.registerEvents(commandPioche, this);
        pm.registerEvents(new RoleBuffListener(), this);
        pm.registerEvents(new ItemCommand(), this);
        pm.registerEvents(new ArmorListener(Collections.emptyList()), this);
        pm.registerEvents(new DonCommand(), this);

        this.getCommand("say").setExecutor(new CommandSay());
        this.getCommand("pioche").setExecutor(commandPioche);
        this.getCommand("dlimits").setExecutor(new CommandDLimits());
        this.getCommand("don").setExecutor(new DonCommand());
        this.getCommand("assistant").setExecutor(assistant);
        this.getCommand("neyuux").setExecutor(this);

        super.onEnable();
    }


    @EventHandler
    public void onGameStart(StartEvent ev) {
        Bukkit.getScheduler().runTaskLater(this, () -> this.getGame().setGameName("@Neyuux_"), 20L);
        this.groupsWarning.clear();

        this.getGame().getPlayersWW()
                .forEach(playerWW -> this.neyuuxCommand.put(playerWW.getUUID(), new HashMap<>()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onStatsSending(WinEvent ev) {
        if (!this.isLoaded())
            return;

        try {
            System.out.println("[Reflection] starting set crack value to : false");
            Reflection.setValue(this.getGame(), "crack", false);
            System.out.println("[Reflection] finish set crack");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onModTabHeart(UpdateModeratorNameTagEvent ev) {
        ev.setSuffix(ev.getSuffix().replace('♥', '❤'));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRepartitionNeyuuxCOmmand(RepartitionEvent ev) {
        this.neyuuxCommand.forEach((id, map) -> {
            getGame().getPlayerWW(id).ifPresent(playerWW -> {

                List<IPlayerWW> players = new ArrayList<>(getGame().getPlayersWW());

                List<String> roles = getGame().getPlayersWW()
                    .stream()
                    .filter(playerWW3 -> !playerWW3.getUUID().equals(playerWW.getUUID()))
                    .map(playerWW3 -> playerWW3.getRole().getKey())
                    .collect(Collectors.toList());

                Collections.shuffle(players);

                map.put(playerWW, playerWW.getRole().getKey());

                for (IPlayerWW playerWW2 : players) {
                    if (!playerWW2.getUUID().equals(playerWW.getUUID())) {
                        map.put(playerWW2, roles.remove(0));
                    }
                }
            });
        });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!this.isLoaded())
            return true;

        if (!(sender instanceof Player))
            return true;

        Player player = (Player)sender;

        WereWolfAPI game = this.getGame();

        if (game.isState(StateGame.GAME)) {
            sender.sendMessage("§f§m                                                                           §r");
            sender.sendMessage(Plugin.getPrefix() + "Liste des rôles de la partie : ");
            sender.sendMessage("");

            neyuuxCommand.get(player.getUniqueId()).forEach((playerWW1, role) -> {
                AtomicReference<ChatColor> c = new AtomicReference<>(ChatColor.GREEN);

                getGame().getPlayersWW()
                        .stream()
                        .filter(playerWW -> playerWW.getRole().getKey().equals(role))
                        .findFirst().ifPresent(playerWW -> {
                            switch (playerWW.getRole().getCamp()) {
                                case WEREWOLF:
                                    c.set(ChatColor.RED);
                                    break;
                                case NEUTRAL:
                                    c.set(ChatColor.GOLD);
                                    break;
                                case VILLAGER:
                                    c.set(ChatColor.GREEN);
                                    break;
                            }
                        });

                player.sendMessage(c.get() + playerWW1.getName() + " §fest " + c.get() + Plugin.getRoleTranslated(role));
            });

            sender.sendMessage("§f§m                                                                           §r");
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
            this.customCount--;
        }, 20L * seconds);
    }

    private Wolf createChien(World world, Location loc, String name, boolean enraged) {
        Wolf wolf = (Wolf) world.spawnEntity(loc, EntityType.WOLF);

        wolf.setCustomName(name);
        wolf.setAngry(enraged);
        wolf.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 2, false, false));
        return wolf;
    }

    private Location getHighestLoc(Location loc) {
        return new Location(loc.getWorld(), loc.getX(), loc.getWorld().getHighestBlockYAt(loc), loc.getZ());
    }



    public boolean isLoaded() {
        return this.ww != null;
    }

    public void addServiceLoadTask(Consumer<WereWolfAPI> consumer) {
        this.serviceLoadTasks.add(consumer);
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

    public void doToAllPlayersWithRole(String role, Consumer<IPlayerWW> consumer) {
        Plugin.getINSTANCE().getGame().getPlayersWW().stream().filter(playerWW -> playerWW.getRole() != null && playerWW.getRole().isKey(role)).forEach(consumer);
    }

    public WereWolfAPI getGame() {
        return this.ww.getWereWolfAPI();
    }

    public boolean hasAttribute(RoleAttribute attribute, IRole role) {
        return this.ww.getRegisterManager().getRolesRegister()
                .stream()
                .filter(roleRegister -> roleRegister.getClazz().equals(role.getClass()))
                .anyMatch(roleRegister -> roleRegister.getMetaDatas().attribute() == attribute);
    }

    public InventoryManager getInvManager() {
        return this.ww.getInvManager();
    }

    public IRegisterManager getRegisterManager() {
        return this.ww.getRegisterManager();
    }


    public static String getRoleTranslated(String key) {
        return Plugin.getINSTANCE().getGame().translate(key);
    }

    public static void addActionBar(ActionBarEvent ev, String s) {
        StringBuilder sb = new StringBuilder(ev.getActionBar());

        sb.append(" §7| ").append(s);

        ev.setActionBar(sb.toString());
    }


    private static String[] getSkinTextures(String name) {
        try {
            URL url_0 = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            InputStreamReader reader_0 = new InputStreamReader(url_0.openStream());

            String uuid = new JsonParser().parse(reader_0).getAsJsonObject().get("id").getAsString();
            URL url_1 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            InputStreamReader reader_1 = new InputStreamReader(url_1.openStream());

            JsonObject textureProperty = new JsonParser().parse(reader_1).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
            String texture = textureProperty.get("value").getAsString();
            String signature = textureProperty.get("signature").getAsString();
            return new String[]{texture,signature};
        }catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Envoyer un title à un joueur
     *
     * @param player Le joueur ciblé
     * @param fadeIn Le fondu entrant (en ticks)
     * @param stay Le temps que le title va rester (en ticks)
     * @param fadeOut Le fondu sortant (en ticks)
     * @param title Le texte du titre
     */
    public static void sendTitle(@NonNull Player player, int fadeIn, int stay, int fadeOut, @NonNull String title) {
        sendTitle(player, fadeIn, stay, fadeOut, title, null);
    }

    /**
     * Envoyer un title à un joueur
     *
     * @param player Le joueur ciblé
     * @param fadeIn Le fondu entrant (en ticks)
     * @param stay Le temps que le title va rester (en ticks)
     * @param fadeOut Le fondu sortant (en ticks)
     * @param title Le texte du titre
     * @param subtitle Le texte du sous-titre
     */
    public static void sendTitle(@NonNull Player player, int fadeIn, int stay, int fadeOut, @Nullable String title, @Nullable String subtitle) {
        // Pour entrer juste un subtitle, il faut un title vide. Pas besoin pour le subtitle
        if (title == null) title = "";
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

        PacketPlayOutTitle packetTimes = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, null, fadeIn, stay, fadeOut);
        connection.sendPacket(packetTimes);

        IChatBaseComponent titleMain = new ChatComponentText(title);
        PacketPlayOutTitle packetPlayOutTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, titleMain);
        connection.sendPacket(packetPlayOutTitle);

        if (subtitle != null) {
            IChatBaseComponent titleSub = new ChatComponentText(subtitle);
            PacketPlayOutTitle packetPlayOutSubTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, titleSub);
            connection.sendPacket(packetPlayOutSubTitle);
        }
    }

    public static String translatePotionEffect(PotionEffectType pet) {
        String s;
        String name = pet.getName();
        switch (name) {
            case "ABSORPTION":
                s = "Absorption";
                break;
            case "BLINDNESS":
                s = "Cécité";
                break;
            case "CONFUSION":
                s = "Nausée";
                break;
            case "DAMAGE_RESISTANCE":
                s = "Résistance";
                break;
            case "FAST_DIGGING":
                s = "Hâte";
                break;
            case "FIRE_RESISTANCE":
                s = "Résitance au Feu";
                break;
            case "HARM":
                s = "Dégâts instantanés";
                break;
            case "HEAL":
                s = "Soins instantanés";
                break;
            case "HEALTH_BOOST":
                s = "Bonus de Vie";
                break;
            case "HUNGER":
                s = "Faim";
                break;
            case "INCREASE_DAMAGE":
                s = "Force";
                break;
            case "INVISIBILITY":
                s = "Invisibilité";
                break;
            case "JUMP":
                s = "Bonus de Saut";
                break;
            case "NIGHT_VISION":
                s = "Vision Nocturne";
                break;
            case "POISON":
                s = "Poison";
                break;
            case "REGENERATION":
                s = "Régénération";
                break;
            case "SATURATION":
                s = "Saturation";
                break;
            case "SLOW":
                s = "Lenteur";
                break;
            case "SLOW_DIGGING":
                s = "Fatigue";
                break;
            case "SPEED":
                s = "Rapidité";
                break;
            case "WATER_BREATHING":
                s = "Apnée";
                break;
            case "WEAKNESS":
                s = "Faiblesse";
                break;
            case "WITHER":
                s = "Wither";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + pet);
        }
        return s;
    }
}
