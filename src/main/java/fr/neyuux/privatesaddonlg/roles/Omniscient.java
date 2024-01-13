package fr.neyuux.privatesaddonlg.roles;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.annotations.Role;
import fr.ph1lou.werewolfapi.enums.Aura;
import fr.ph1lou.werewolfapi.enums.Category;
import fr.ph1lou.werewolfapi.enums.RoleAttribute;
import fr.ph1lou.werewolfapi.events.UpdateNameTagEvent;
import fr.ph1lou.werewolfapi.events.UpdatePlayerNameTagEvent;
import fr.ph1lou.werewolfapi.events.game.permissions.UpdateModeratorNameTagEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.role.impl.RoleNeutral;
import fr.ph1lou.werewolfapi.role.utils.DescriptionBuilder;
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
}