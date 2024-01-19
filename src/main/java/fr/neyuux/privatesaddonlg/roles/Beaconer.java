package fr.neyuux.privatesaddonlg.roles;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.annotations.IntValue;
import fr.ph1lou.werewolfapi.annotations.Role;
import fr.ph1lou.werewolfapi.annotations.Timer;
import fr.ph1lou.werewolfapi.enums.*;
import fr.ph1lou.werewolfapi.events.ActionBarEvent;
import fr.ph1lou.werewolfapi.events.game.day_cycle.DayEvent;
import fr.ph1lou.werewolfapi.game.IConfiguration;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.impl.PotionModifier;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.role.impl.RoleImpl;
import fr.ph1lou.werewolfapi.role.interfaces.IAffectedPlayers;
import fr.ph1lou.werewolfapi.role.interfaces.IPower;
import fr.ph1lou.werewolfapi.role.utils.DescriptionBuilder;
import fr.ph1lou.werewolfapi.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.text.DecimalFormat;
import java.util.*;

@Role(
        key = "privatesaddon.roles.beaconer.display",
        attribute = RoleAttribute.MINOR_INFORMATION,
        category = Category.VILLAGER,
        timers = {
                @Timer(key = "privatesaddon.roles.beaconer.timers.activation_duration", defaultValue = 300, meetUpValue = 180),
                @Timer(key = "privatesaddon.roles.beaconer.timers.plant_duration", defaultValue = 30, meetUpValue = 30, step = 1)},
        configValues = {
                @IntValue(key = "privatesaddon.roles.beaconer.configurations.distance_plant", defaultValue = 15, meetUpValue = 15, step = 1, item = UniversalMaterial.DANDELION),
                @IntValue(key = "privatesaddon.roles.beaconer.configurations.distance_activated", defaultValue = 20, meetUpValue = 20, step = 1, item = UniversalMaterial.PUFFERFISH),
                @IntValue(key = "privatesaddon.roles.beaconer.configurations.distance_to_activate", defaultValue = 60, meetUpValue = 50, step = 5, item = UniversalMaterial.STONE_SHOVEL)}
)
public class Beaconer extends RoleImpl implements IAffectedPlayers, IPower {

    public final HashMap<IPlayerWW, BeaconType> affectedPlayers = new HashMap<>();

    private boolean power = true;

    @Getter
    @Setter
    @Nullable
    private BeaconType nextBeacon;


    public Beaconer(WereWolfAPI api, IPlayerWW playerWW) {
        super(api, playerWW);
    }

    @Override
    public @NotNull String getDescription() {
        DescriptionBuilder descBuilder = new DescriptionBuilder(this.game, this);
        IConfiguration config = game.getConfig();

        descBuilder.setDescription("Vous pouvez poser des balises sur des joueurs en leur faisant un clic droit dessus. \n Poser une balise prend §b" +
                config.getTimerValue("privatesaddon.roles.beaconer.timers.plant_duration") +
                " secondes §foù il faut rester à moins de §b" +
                config.getValue("privatesaddon.roles.beaconer.configurations.distance_plant") +
                " blocs §fdu joueur. \n Puis, pour activer la balise, il faut rester §b" +
                Utils.conversion(config.getTimerValue("privatesaddon.roles.beaconer.timers.activation_duration")) +
                " §fà plus de §b" +
                config.getValue("privatesaddon.roles.beaconer.configurations.distance_to_activate") +
                " blocs §fdu joueur. \n Vous possèdez 2 balises : une balise qui donne les effets à l'activation et une balise qui donne le camp à l'activation.");
        descBuilder.setEffects("La Balise §bEffets §fvous donne §9Speed 0.5 §fsi vous êtes à plus de §b" +
                config.getValue("privatesaddon.roles.beaconer.configurations.distance_activated") +
                " blocs §fd'elle lorsqu'elle est activée. \n " +
                "La Balise §bCamp §fvous donne en fonction du camp, si vous êtes à plus de §b" +
                config.getValue("privatesaddon.roles.beaconer.configurations.distance_activated") +
                " blocs §fd'elle après son activation : \n - §cForce §fsi le joueur n'est pas du camp du Village \n - §7Résistance §fs'il est du Village");
        descBuilder.setCommand("/ww balise pour équiper une balise");

        Arrays.stream(BeaconType.values())
                .forEach(type -> descBuilder.addExtraLines(type.getDescription(this) + "\n"));

        return descBuilder.build();
    }

    @Override
    public void addAffectedPlayer(IPlayerWW iPlayerWW) {
        this.affectedPlayers.put(iPlayerWW, BeaconType.CAMP);
    }

    @Override
    public void removeAffectedPlayer(IPlayerWW iPlayerWW) {
        this.affectedPlayers.remove(iPlayerWW);
    }

    @Override
    public void clearAffectedPlayer() {
        this.affectedPlayers.clear();
    }

    @Override
    @Unmodifiable
    public List<? extends IPlayerWW> getAffectedPlayers() {
        return new ArrayList<>(this.affectedPlayers.keySet());
    }

    @Unmodifiable
    public List<BeaconType> getUnavailableBeacons() {
        return new ArrayList<>(this.affectedPlayers.values());
    }

    @Override
    public void setPower(boolean b) {
        this.power = b;
    }

    @Override
    public boolean hasPower() {
        return power;
    }

    @Override
    public void recoverPower() {

    }


    @EventHandler
    public void onPlayerRightClick(PlayerInteractAtEntityEvent ev) {
        if (!this.isAbilityEnabled() || !this.hasPower() || !this.getPlayerWW().isState(StatePlayer.ALIVE))
            return;

        if (ev.getRightClicked().getType() != EntityType.PLAYER)
            return;

        Player player = ev.getPlayer();

        if (!player.getUniqueId().equals(this.getPlayerUUID()))
            return;

        Player target = (Player) ev.getRightClicked();
        this.game.getPlayerWW(target.getUniqueId()).ifPresent(targetWW -> {
            if (this.nextBeacon != null) {
                if (!this.getAffectedPlayers().contains(targetWW)) {

                    this.affectedPlayers.put(targetWW, this.nextBeacon);
                    Plugin.sendTitle(player, 20, 40, 20, "§a§lBalise posée !", "§bVous avez posé une balise " + this.nextBeacon.getName() + " sur " + target.getName() + ".");
                    player.sendMessage(Plugin.getPrefix() + "§fPour activer la balise, vous devez rester §b" + game.getConfig().getValue("privatesaddon.roles.beaconer.timers.plant_duration") + " secondes §fà moins de §b" + game.getConfig().getValue("privatesaddon.roles.beaconer.configurations.distance_plant") + " blocs §fde §e" + targetWW.getName() + "§f.");
                    player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);

                    this.setPower(false);
                    this.setNextBeacon(null);
                } else {
                    player.sendMessage(Plugin.getPrefix() + "§cVous avez déjà posé une balise sur ce joueur.");
                }
            }
        });
    }

    @EventHandler
    public void onDay(DayEvent ev) {
        if (!this.hasPower() && this.isAbilityEnabled()) {
            this.setPower(true);
            this.getPlayerWW().sendMessage(new TextComponent(Plugin.getPrefix() + "§fVous pouvez de nouveau poser une balise."));
        }
    }

    @EventHandler
    public void onActionBarBeacons(ActionBarEvent ev) {
        if (!ev.getPlayerUUID().equals(this.getPlayerUUID()))
            return;

        Player player = Bukkit.getPlayer(this.getPlayerUUID());

        if (player == null)
            return;

        this.affectedPlayers.forEach((playerWW, beacon) -> {
            if (beacon.getPlantProgression() <= 100f && this.canPlant(player, playerWW)) {
                Plugin.addActionBar(ev, "§ePosage §l"+playerWW.getName()+"§e : §l" + new DecimalFormat("#.0").format(beacon.getPlantProgression()) + "%");

            } else if (beacon.getActivationProgression() <= 100f && this.canActivate(player, playerWW)) {
                Plugin.addActionBar(ev, "§eActivation §l"+playerWW.getName()+"§e : §l" + new DecimalFormat("#.0").format(beacon.getPlantProgression()) + "%");

            }
        });
    }


    @Override
    public void second() {
        if (this.affectedPlayers.isEmpty())
            return;

        Player player = Bukkit.getPlayer(this.getPlayerUUID());

        if (player == null)
            return;

        this.affectedPlayers.forEach((playerWW, beacon) -> {

            if (beacon.getPlantProgression() <= 100f) {
                if (this.canPlant(player, playerWW)) {

                    beacon.setPlantProgression(beacon.getPlantProgression() + 100.0f / (float)game.getConfig().getTimerValue("privatesaddon.roles.beaconer.timers.plant_duration"));

                    if (beacon.getPlantProgression() > 100f) {
                        player.sendMessage(Plugin.getPrefix() + "§fVous avez bien posé votre balise \"§b§l" + beacon.getName() + "§f\" sur §e" + playerWW.getName() + "§f. Pour l'activer, vous devez désormais rester à plus de §b" + game.getConfig().getValue("privatesaddon.roles.beaconer.configurations.distance_to_activate") + " blocs §fpendant §b" + Utils.conversion(game.getConfig().getTimerValue("privatesaddon.roles.beaconer.timers.activation_duration")) + "§f.");
                        player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);
                    }

                }
            } else if (beacon.getActivationProgression() <= 100f) {
                if (this.canActivate(player, playerWW)) {

                    beacon.setActivationProgression(beacon.getActivationProgression() + 100f / (float)game.getConfig().getTimerValue("privatesaddon.roles.beaconer.timers.activation_duration"));

                    if (beacon.getActivationProgression() > 100f) {
                        player.sendMessage(Plugin.getPrefix() + "§fVotre balise \"§b§l" + beacon.getName() + "§f\" vient de s'activer !");
                        player.playSound(player.getLocation(), Sound.LEVEL_UP, 8f, 1.8f);

                        beacon.sendInfos(player, playerWW);
                    }
                } else {
                    Player target = Bukkit.getPlayer(playerWW.getUUID());

                    if (target != null) {
                        double distanceSquared = player.getLocation().distanceSquared(target.getLocation());
                        double square = this.square("privatesaddon.roles.beaconer.configurations.distance_activated");

                        if (distanceSquared >= square && !beacon.isHasEffects())
                            beacon.giveEffects(this);
                        else if (distanceSquared < square && beacon.isHasEffects())
                            beacon.removeEffects(this);
                    }
                }
            }
        });
    }

    private boolean canPlant(Player player, IPlayerWW targetWW) {
        Player target = Bukkit.getPlayer(targetWW.getUUID());

        if (target == null)
            return false;

        return player.getLocation().distanceSquared(target.getLocation()) <= square("privatesaddon.roles.beaconer.configurations.distance_plant");
    }

    private boolean canActivate(Player player, IPlayerWW targetWW) {
        Player target = Bukkit.getPlayer(targetWW.getUUID());

        if (target == null)
            return false;

        return player.getLocation().distanceSquared(target.getLocation()) >= square("privatesaddon.roles.beaconer.configurations.distance_to_activate");
    }

    private double square(String key) {
        return Math.pow(game.getConfig().getValue(key), 2);
    }

    @Getter
    public enum BeaconType {
        CAMP("Camp"),
        EFFECTS("Effets");

        private final String name;
        
        @Setter
        private float activationProgression;

        @Setter
        private float plantProgression;

        private boolean hasEffects;

        BeaconType(String name) {
            this.name = name;
        }


        public boolean isAvailable(Beaconer beaconer) {
            return !beaconer.getUnavailableBeacons().contains(this);
        }

        public void sendInfos(Player player, IPlayerWW targetWW) {
            switch (this) {

                case CAMP:
                    String displayCamp = targetWW.getRole().getDisplayCamp();

                    switch (Arrays.stream(Camp.values())
                            .filter(camp -> camp.getKey().equals(displayCamp))
                            .findFirst().get()) {

                        case WEREWOLF:
                            player.sendMessage(Plugin.getPrefix() + "§cVous apprenez que §e" + targetWW.getName() + "§c appartient au camp des §lLoups-Garous§c. §fA partir de maintenant, vous obtiendrez l'effet §cForce §fsi vous êtes à plus de §b" + Plugin.getINSTANCE().getGame().getConfig().getValue("privatesaddon.roles.beaconer.configurations.distance_activated") + " blocs §fde lui.");
                            break;
                        case NEUTRAL:
                            player.sendMessage(Plugin.getPrefix() + "§6Vous apprenez que §e" + targetWW.getName() + "§6 n'appartient à §lAucun §6des deux camps principaux. §fA partir de maintenant, vous obtiendrez l'effet §cForce §fsi vous êtes à plus de §b" + Plugin.getINSTANCE().getGame().getConfig().getValue("privatesaddon.roles.beaconer.configurations.distance_activated") + " blocs §fde lui.");
                            break;
                        case VILLAGER:
                            player.sendMessage(Plugin.getPrefix() + "§aVous apprenez que §e" + targetWW.getName() + "§a appartient au camp du §lVillage§a. §fA partir de maintenant, vous obtiendrez l'effet §7Résistance §fsi vous êtes à plus de §b" + Plugin.getINSTANCE().getGame().getConfig().getValue("privatesaddon.roles.beaconer.configurations.distance_activated") + " blocs §fde lui.");
                            break;
                    }

                    break;
                case EFFECTS:
                    break;
            }
        }

        public void giveEffects(Beaconer beaconer) {
            Player player = Bukkit.getPlayer(beaconer.getPlayerUUID());

            switch (this) {
                case CAMP:
                    beaconer.affectedPlayers.entrySet()
                            .stream()
                            .filter(entry -> entry.getValue() == CAMP)
                            .map(entry -> entry.getKey().getRole())
                            .findFirst()
                            .ifPresent(role -> {
                                if (role.isWereWolf() || role.isSolitary())
                                    beaconer.getPlayerWW().addPotionModifier(PotionModifier.add(PotionEffectType.INCREASE_DAMAGE, beaconer.getKey()));
                                else
                                    beaconer.getPlayerWW().addPotionModifier(PotionModifier.add(PotionEffectType.DAMAGE_RESISTANCE, beaconer.getKey()));
                            });

                    break;
                case EFFECTS:
                    if (player != null)
                        player.setWalkSpeed(player.getWalkSpeed() + 0.20f * 0.10f);
                    break;
            }

            this.hasEffects = true;
        }

        public void removeEffects(Beaconer beaconer) {
            Player player = Bukkit.getPlayer(beaconer.getPlayerUUID());

            switch (this) {
                case CAMP:
                    beaconer.getPlayerWW().getPotionModifiers()
                            .stream()
                            .filter(pot -> pot.getIdentifier().equals(beaconer.getKey()))
                            .forEach(pot -> beaconer.getPlayerWW().addPotionModifier(PotionModifier.remove(pot.getPotionEffectType(), pot.getIdentifier())));
                    break;
                case EFFECTS:
                    if (player != null)
                        player.setWalkSpeed(player.getWalkSpeed() - 0.20f * 0.10f);
                    break;
            }

            this.hasEffects = false;
        }
        
        public String getDescription(Beaconer beaconer) {
            StringBuilder sb = new StringBuilder("§bBalise " + this.getName() + " §f: ");

            if (this.isAvailable(beaconer))
                sb.append("Disponible");
            else {

                sb.append((this.getActivationProgression() >= 100f ? "Activée (" : (this.getPlantProgression() >= 100f ? "Posée (" : "Posage (")) + Math.round(Math.min(100f, this.getActivationProgression())) + "%)");
            }

            return sb.toString();
        }
    }
}
