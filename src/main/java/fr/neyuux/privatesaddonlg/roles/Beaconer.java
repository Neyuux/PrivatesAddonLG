package fr.neyuux.privatesaddonlg.roles;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.annotations.IntValue;
import fr.ph1lou.werewolfapi.annotations.Role;
import fr.ph1lou.werewolfapi.annotations.Timer;
import fr.ph1lou.werewolfapi.enums.Category;
import fr.ph1lou.werewolfapi.enums.RoleAttribute;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.enums.UniversalMaterial;
import fr.ph1lou.werewolfapi.events.game.day_cycle.DayEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.impl.PotionModifier;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.role.impl.RoleImpl;
import fr.ph1lou.werewolfapi.role.interfaces.IAffectedPlayers;
import fr.ph1lou.werewolfapi.role.interfaces.IPower;
import fr.ph1lou.werewolfapi.role.utils.DescriptionBuilder;
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

        descBuilder.setDescription(this.game.translate("privatesaddon.roles.beaconer.description"));
        descBuilder.setEffects(this.game.translate("privatesaddon.roles.beaconer.effects"));

        Arrays.stream(BeaconType.values())
                .forEach(type -> descBuilder.addExtraLines(type.getDescription()));

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


    @Override
    public void second() {

    }

    @Getter
    private enum BeaconType {
        CAMP("Camp"),
        EFFECTS("Effets");

        private final String name;
        
        @Setter
        private float activationProgression;

        @Setter
        private float plantProgression;
        
        @Setter
        private boolean isAvailable;

        BeaconType(String name) {
            this.name = name;
            this.isAvailable = true;
        }


        public void addActivationProgression(float added) {
            this.activationProgression += added;
        }

        public void addPlantProgression(int added) {
            this.plantProgression += added;
        }

        public void sendInfos() {

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
                                    beaconer.getPlayerWW().addPotionModifier(PotionModifier.add(PotionEffectType.INCREASE_DAMAGE, "privatesaddon.roles.beaconer.display"));
                                else
                                    beaconer.getPlayerWW().addPotionModifier(PotionModifier.add(PotionEffectType.DAMAGE_RESISTANCE, "privatesaddon.roles.beaconer.display"));
                            });

                    break;
                case EFFECTS:
                    if (player != null)
                        player.setWalkSpeed(player.getWalkSpeed() * 1.10f);
                    break;
            }
        }

        public void removeEffects(Beaconer beaconer) {
            Player player = Bukkit.getPlayer(beaconer.getPlayerUUID());

            switch (this) {
                case CAMP:
                    break;
                case EFFECTS:
                    if (player != null)
                        player.setWalkSpeed(player.getWalkSpeed() / 1.10f);
                    break;
            }
        }
        
        public String getDescription() {
            StringBuilder sb = new StringBuilder("§bBalise " + this.getName() + " §f: ");

            if (this.isAvailable())
                sb.append("Disponible");
            else {

                sb.append((this.getActivationProgression() >= 100f ? "Activée (" : (this.getPlantProgression() >= 100f ? "Posée (" : "Posage (")) + Math.round(Math.min(100f, this.getActivationProgression())) + "%)");
            }

            return sb.toString();
        }
    }
}
