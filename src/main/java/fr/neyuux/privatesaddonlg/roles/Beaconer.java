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
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.role.impl.RoleImpl;
import fr.ph1lou.werewolfapi.role.interfaces.IAffectedPlayers;
import fr.ph1lou.werewolfapi.role.interfaces.IPower;
import fr.ph1lou.werewolfapi.role.utils.DescriptionBuilder;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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

    private final HashMap<IPlayerWW, BeaconType> affectedPlayers = new HashMap<>();

    private boolean power = true;


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
        private boolean isAvailable;

        BeaconType(String name) {
            this.name = name;
            this.isAvailable = true;
        }


        public void addActivationProgression(float added) {
            this.activationProgression += added;
        }

        public void sendInfos() {

        }

        public void giveEffects(Beaconer beaconer) {
            Player player = Bukkit.getPlayer(beaconer.getPlayerUUID());

            if (player != null)
                player.setWalkSpeed(player.getWalkSpeed() * 1.10f);
        }

        public void removeEffects(Beaconer beaconer) {
            Player player = Bukkit.getPlayer(beaconer.getPlayerUUID());

            if (player != null)
                player.setWalkSpeed(player.getWalkSpeed() / 1.10f);
        }
        
        public String getDescription() {
            StringBuilder sb = new StringBuilder("§bBalise " + this.getName() + " §f: ");

            if (this.isAvailable())
                sb.append("Disponible");
            else {

                sb.append((this.getActivationProgression() >= 100f ? "Activée (" : (this.getActivationProgression() <= 0f ? "Posage (" : "Posée (")) + Math.round(Math.min(100f, this.getActivationProgression())) + "%)");
            }

            return sb.toString();
        }
    }
}
