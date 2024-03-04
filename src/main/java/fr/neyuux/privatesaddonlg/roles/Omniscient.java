package fr.neyuux.privatesaddonlg.roles;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.annotations.Role;
import fr.ph1lou.werewolfapi.enums.Aura;
import fr.ph1lou.werewolfapi.enums.Category;
import fr.ph1lou.werewolfapi.enums.RoleAttribute;
import fr.ph1lou.werewolfapi.events.UpdateNameTagEvent;
import fr.ph1lou.werewolfapi.events.UpdatePlayerNameTagEvent;
import fr.ph1lou.werewolfapi.events.game.permissions.UpdateModeratorNameTagEvent;
import fr.ph1lou.werewolfapi.events.random_events.DiscordEvent;
import fr.ph1lou.werewolfapi.events.random_events.SwapEvent;
import fr.ph1lou.werewolfapi.events.roles.infect_father_of_the_wolves.InfectionEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.role.impl.RoleNeutral;
import fr.ph1lou.werewolfapi.role.utils.DescriptionBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;


@Role(key = "privatesaddon.roles.omniscient.display",
        defaultAura = Aura.NEUTRAL,
        category = Category.NEUTRAL,
        attribute = RoleAttribute.NEUTRAL)
public class Omniscient extends RoleNeutral {

    public Omniscient(WereWolfAPI api, IPlayerWW playerWW) {
        super(api, playerWW);
    }

    @Override
    public @NotNull String getDescription() {

        return new DescriptionBuilder(game, this)
                .setDescription(game.translate("privatesaddon.roles.omniscient.description"))
                .setItems(game.translate("privatesaddon.roles.omniscient.items"))
                .setCommand(game.translate("privatesaddon.roles.omniscient.command"))
                .build();
    }


    @Override
    public void recoverPower() {
        Plugin.getINSTANCE().getGame().getPlayersWW()
                .forEach(playerWW -> Bukkit.getPluginManager().callEvent(new UpdateNameTagEvent(playerWW)));
    }


    @Override
    public void recoverPotionEffect() {

    }

    @Override
    public void disableAbilitiesRole() {
        Plugin.getINSTANCE().getGame().getPlayersWW()
                .forEach(playerWW -> Bukkit.getPluginManager().callEvent(new UpdateNameTagEvent(playerWW)));
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onNameTagUpdate(UpdatePlayerNameTagEvent ev) {
        Player player = Bukkit.getPlayer(ev.getPlayerUUID());
        Player target = Bukkit.getPlayer(ev.getTargetUUID());

        if (player == null || target == null || !this.isAbilityEnabled())
            return;

        if (!target.getUniqueId().equals(this.getPlayerUUID()))
            return;


        StringBuilder prefix = new StringBuilder(ev.getPrefix());
        StringBuilder suffix = new StringBuilder(ev.getSuffix());

        game.getPlayerWW(target.getUniqueId()).flatMap(targetWW ->
                game.getPlayerWW(player.getUniqueId())).ifPresent(playerWW -> {
            UpdateModeratorNameTagEvent modNameTag = new UpdateModeratorNameTagEvent(player.getUniqueId());
            Bukkit.getPluginManager().callEvent(modNameTag);

            prefix.append(modNameTag.getPrefix());

            suffix.append(modNameTag.getSuffix());
        });

        ev.setPrefix(String.valueOf(prefix));
        ev.setSuffix(String.valueOf(suffix));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInfection(InfectionEvent ev) {
        if (ev.getPlayerWW().equals(this.getPlayerWW())) {
            ev.setCancelled(true);
            ev.setInformInfectionCancelledMessage(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDiscord(DiscordEvent ev) {
        if (ev.getPlayerWWs().contains(this.getPlayerWW())) {
            ev.setCancelled(true);
            this.getPlayerWW().sendMessage(new TextComponent(Plugin.getPrefix() + "§fVous avez sélectionné pour la §e§lZizanie§f. Cependant, à cause de votre rôle, celle-ci ne s'est §cpas activée§f."));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSwap(SwapEvent ev) {
        if (ev.getPlayerWW1().getUUID().equals(this.getPlayerUUID()) || ev.getPlayerWW2().getUUID().equals(this.getPlayerUUID())) {
            ev.setCancelled(true);
            this.getPlayerWW().sendMessage(new TextComponent(Plugin.getPrefix() + "§fVous avez sélectionné pour le §e§lSwap§f. Cependant, à cause de votre rôle, celui-ci ne s'est §cpas activé§f."));
        }
    }
}