package fr.neyuux.privatesaddonlg.roles;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.annotations.IntValue;
import fr.ph1lou.werewolfapi.annotations.Role;
import fr.ph1lou.werewolfapi.annotations.Timer;
import fr.ph1lou.werewolfapi.enums.*;
import fr.ph1lou.werewolfapi.events.ActionBarEvent;
import fr.ph1lou.werewolfapi.events.game.day_cycle.DayEvent;
import fr.ph1lou.werewolfapi.events.roles.fox.SniffEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.impl.AuraModifier;
import fr.ph1lou.werewolfapi.player.impl.PotionModifier;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.player.utils.Formatter;
import fr.ph1lou.werewolfapi.role.impl.RoleImpl;
import fr.ph1lou.werewolfapi.role.interfaces.IAffectedPlayers;
import fr.ph1lou.werewolfapi.role.interfaces.ILimitedUse;
import fr.ph1lou.werewolfapi.role.interfaces.IPower;
import fr.ph1lou.werewolfapi.role.interfaces.IProgress;
import fr.ph1lou.werewolfapi.role.utils.DescriptionBuilder;
import fr.ph1lou.werewolfapi.utils.BukkitUtils;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Role(key="privatesaddon.roles.fox.display",
        category=Category.VILLAGER,
        auraDescriptionSpecialUseCase = "werewolf.roles.fox.aura",
        attribute=RoleAttribute.INFORMATION,
        timers={@Timer(key="privatesaddon.roles.fox.timers.fox_smell_duration", defaultValue=90, meetUpValue=30)},
        configValues={
                @IntValue(key="privatesaddon.roles.fox.configurations.fox_smell_number", defaultValue=3, meetUpValue=3, step=1, item=UniversalMaterial.CARROT),
                @IntValue(key="privatesaddon.roles.fox.configurations.distance", defaultValue=20, meetUpValue=20, step=2, item=UniversalMaterial.ORANGE_WOOL),
                @IntValue(key = "privatesaddon.roles.fox.configurations.timer_reveal", meetUpValue = 120, defaultValue = 120, step = 5, item = UniversalMaterial.ENDER_PEARL)})
public class OldFox
        extends RoleImpl
        implements IProgress,
        ILimitedUse,
        IAffectedPlayers,
        IPower {
    private final List<IPlayerWW> affectedPlayer = new ArrayList<>();
    private float progress = 0.0f;
    private int use = 0;
    private boolean power = true;

    public OldFox(WereWolfAPI api, IPlayerWW playerWW) {
        super(api, playerWW);
    }

    @Override
    public void setPower(boolean power) {
        this.power = power;
    }

    @Override
    public boolean hasPower() {
        return this.power;
    }

    @Override
    public void addAffectedPlayer(IPlayerWW playerWW) {
        this.affectedPlayer.add(playerWW);
    }

    @Override
    public void removeAffectedPlayer(IPlayerWW playerWW) {
        this.affectedPlayer.remove(playerWW);
    }

    @Override
    public void clearAffectedPlayer() {
        this.affectedPlayer.clear();
    }

    public List<IPlayerWW> getAffectedPlayers() {
        return this.affectedPlayer;
    }

    @Override
    public int getUse() {
        return this.use;
    }

    @Override
    public void setUse(int use) {
        this.use = use;
    }

    @Override
    public float getProgress() {
        return this.progress;
    }

    @Override
    public void setProgress(float progress) {
        this.progress = progress;
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerDeathByFox(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() == null) {
            return;
        }
        Player killer = event.getEntity().getKiller();
        if (!this.getPlayerUUID().equals(killer.getUniqueId())) {
            return;
        }
        this.getPlayerWW().addPotionModifier(PotionModifier.add(PotionEffectType.SPEED, 3600, 0, this.getKey()));
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onDay(DayEvent event) {
        if (this.getUse() >= this.game.getConfig().getValue("privatesaddon.roles.fox.configurations.fox_smell_number")) {
            return;
        }
        this.setPower(true);
        if (!this.getPlayerWW().isState(StatePlayer.ALIVE)) {
            return;
        }

        Player player = Bukkit.getPlayer(this.getPlayerUUID());

        if (player == null) {
            this.getPlayerWW().sendMessage(new TextComponent(Plugin.getPrefix() + "§bVous pouvez à nouveau flairer un joueur. Si un flair est toujours en cours, il sera remplacé et la progression remise à zéro. §bFlairs restants : §e§l" + (this.game.getConfig().getValue("privatesaddon.roles.fox.configurations.fox_smell_number") - this.getUse())));
        } else {
            player.sendMessage(Plugin.getPrefix() + "§bVous pouvez à nouveau flairer un joueur. Si un flair est toujours en cours, il sera remplacé et la progression remise à zéro. §bFlairs restants : §e§l" + (this.game.getConfig().getValue("privatesaddon.roles.fox.configurations.fox_smell_number") - this.getUse()));
        }
    }

    @Override
    @NotNull
    public String getDescription() {
        return new DescriptionBuilder(this.game, this).setDescription(this.game.translate("privatesaddon.roles.fox.description", Formatter.number(this.game.getConfig().getValue("privatesaddon.roles.fox.configurations.distance")), Formatter.timer(this.game, "privatesaddon.roles.fox.timers.fox_smell_duration"), Formatter.format("&number1&", this.game.getConfig().getValue("privatesaddon.roles.fox.configurations.fox_smell_number") - this.use))).setEffects(this.game.translate("privatesaddon.roles.fox.effect")).setPower(this.game.translate("privatesaddon.roles.fox.progress", Formatter.format("&progress&", Math.min(100.0, Math.floor(this.getProgress()))))).build();
    }

    @Override
    public void recoverPower() {
    }

    @Override
    public void second() {
        if (!this.getPlayerWW().isState(StatePlayer.ALIVE)) {
            return;
        }
        if (this.getAffectedPlayers().isEmpty()) {
            return;
        }
        IPlayerWW playerWW = this.getAffectedPlayers().get(0);
        if (!playerWW.isState(StatePlayer.ALIVE)) {
            return;
        }
        Location renardLocation = this.getPlayerWW().getLocation();
        Location playerLocation = playerWW.getLocation();
        if (renardLocation.getWorld() != playerLocation.getWorld()) {
            return;
        }
        if (renardLocation.distanceSquared(playerLocation) > Math.pow(this.game.getConfig().getValue("privatesaddon.roles.fox.configurations.distance"), 2)) {
            return;
        }
        float temp = this.getProgress() + 100.0f / (float)(this.game.getConfig().getTimerValue("privatesaddon.roles.fox.timers.fox_smell_duration") + 1);
        this.setProgress(temp);

        if (temp >= 100.0f) {
            Player player = Bukkit.getPlayer(this.getPlayerUUID());
            boolean isWereWolf = playerWW.getRole().isDisplayCamp(Camp.WEREWOLF.getKey()) || playerWW.getRole().getDisplayCamp().equals(playerWW.getRole().getCamp().getKey()) && playerWW.getRole().isWereWolf();
            SniffEvent sniffEvent = new SniffEvent(this.getPlayerWW(), playerWW, isWereWolf);
            Bukkit.getPluginManager().callEvent(sniffEvent);
            if (!sniffEvent.isCancelled()) {
                int delaySeconds = this.game.getConfig().getValue("privatesaddon.roles.fox.configurations.timer_reveal");
                long delayLong = delaySeconds * 20L;

                if (sniffEvent.isWereWolf()) {
                    player.sendMessage(Plugin.getPrefixWithColor(ChatColor.RED) + "§c§l" + sniffEvent.getTargetWW().getName() + " §cfait partie du camp des §lLoups-Garous§c. Il sera averti que vous l'avez flairé dans §b" + delaySeconds + " secondes§c.");
                    this.addAuraModifier(new AuraModifier(this.getKey(), Aura.DARK, 1, false));
                } else {
                    player.sendMessage(Plugin.getPrefixWithColor(ChatColor.RED) + "§a§l" + sniffEvent.getTargetWW().getName() + " §ane fait pas partie du camp des Loups-Garous.");
                }
                if (sniffEvent.isWereWolf()) {
                    BukkitUtils.scheduleSyncDelayedTask(this.game, () -> {
                        playerWW.sendMessage(new TextComponent(Plugin.getPrefixWithColor(ChatColor.YELLOW) + "§bVous avez été flairé par le §a§lRenard §bil y a §e" + delaySeconds + " secondes§b."));
                        playerWW.sendSound(Sound.DONKEY_ANGRY);
                    }, delayLong);
                }
            } else {
                player.sendMessage(Plugin.getPrefixWithColor(ChatColor.RED) + "§cVotre pouvoir a été annulé !");
            }
            this.clearAffectedPlayer();
            this.setProgress(0.0f);
        }
    }

    @EventHandler
    public void onActionBarRequest(ActionBarEvent event) {
        if (!this.getPlayerUUID().equals(event.getPlayerUUID())) {
            return;
        }

        StringBuilder stringBuilder = new StringBuilder(event.getActionBar());
        Player player = Bukkit.getPlayer(event.getPlayerUUID());
        if (player == null) {
            return;
        }

        if (!this.getPlayerWW().isState(StatePlayer.ALIVE)) {
            return;
        }
        if (this.getAffectedPlayers().isEmpty()) {
            return;
        }
        IPlayerWW playerWW = this.getAffectedPlayers().get(0);
        if (!playerWW.isState(StatePlayer.ALIVE)) {
            return;
        }
        Location renardLocation = this.getPlayerWW().getLocation();
        Location playerLocation = playerWW.getLocation();
        if (renardLocation.getWorld() != playerLocation.getWorld()) {
            return;
        }
        if (renardLocation.distance(playerLocation) > (double)this.game.getConfig().getValue("privatesaddon.roles.fox.configurations.distance")) {
            return;
        }

        float temp = this.getProgress() + 100.0f / (float)(this.game.getConfig().getTimerValue("privatesaddon.roles.fox.timers.fox_smell_duration") + 1);

        stringBuilder.append(" §7| §eFlair ").append(playerWW.getName()).append(" : §l").append(Math.min(100.0, Math.floor(temp))).append("%");
        event.setActionBar(stringBuilder.toString());
    }
}