package fr.neyuux.privatesaddonlg.roles;

import fr.ph1lou.werewolfapi.annotations.Role;
import fr.ph1lou.werewolfapi.enums.*;
import fr.ph1lou.werewolfapi.events.game.life_cycle.FinalDeathEvent;
import fr.ph1lou.werewolfapi.events.roles.mystical_werewolf.MysticalWerewolfRevelationEvent;
import fr.ph1lou.werewolfapi.events.werewolf.WereWolfCanSpeakInChatEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.player.utils.Formatter;
import fr.ph1lou.werewolfapi.role.impl.RoleWereWolf;
import fr.ph1lou.werewolfapi.role.utils.DescriptionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Role(key = "privatesaddon.roles.mystical_werewolf.display", category = Category.WEREWOLF, attributes = {RoleAttribute.WEREWOLF}, defaultAura = Aura.LIGHT)
public class MysticalWerewolf extends RoleWereWolf {

    private final List<IPlayerWW> list = new ArrayList<>();

    public MysticalWerewolf(WereWolfAPI game, IPlayerWW playerWW) {
        super(game, playerWW);

        this.list.addAll(game.getPlayersWW());
        Collections.shuffle(list);
    }

    @Override
    @NotNull
    public String getDescription() {
        return (new DescriptionBuilder(this.game, this))
                .setDescription(this.game.translate("werewolf.roles.mystical_werewolf.description", new Formatter[0]))
                .setEffects(this.game.translate("werewolf.description.werewolf", new Formatter[0]))
                .build();
    }

    @Override
    public void recoverPower() {}

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMessage(WereWolfCanSpeakInChatEvent event) {
        if (!event.getPlayerWW().equals(getPlayerWW()))
            return;
        getPlayerWW().sendMessageWithKey("werewolf.prefix.red", "werewolf.roles.mystical_werewolf.no_message", new Formatter[0]);
        event.setCanSpeak(false);
    }

    @EventHandler
    public void onWereWolfDeath(FinalDeathEvent event) {
        if (!getPlayerWW().isState(StatePlayer.ALIVE))
            return;
        if (!event.getPlayerWW().getRole().isWereWolf())
            return;
        if (!isAbilityEnabled()) {
            getPlayerWW().sendMessageWithKey("werewolf.prefix.red", "werewolf.roles.mystical_werewolf.ability_disabled", new Formatter[0]);
            return;
        }
        int max = 5;
        IPlayerWW selected = list.remove(0);
        while (!selected.isState(StatePlayer.ALIVE) || !(!selected.getRole().isDisplayCamp(Camp.WEREWOLF.getKey()) || selected.getRole().getDisplayCamp().equals(selected.getRole().getCamp().getKey()) && !selected.getRole().isWereWolf())) {
            if (max == 0)
                return;
            
            if (list.isEmpty()) {
                list.addAll(game.getPlayersWW());
                Collections.shuffle(list);
                max--;
            }
            selected = list.remove(0);
        }
        Bukkit.getPluginManager().callEvent(new MysticalWerewolfRevelationEvent(getPlayerWW(), selected));
        getPlayerWW().sendMessageWithKey("werewolf.prefix.red", "werewolf.roles.mystical_werewolf.werewolf_death", Formatter.player(selected.getName()),
                Formatter.role(this.game.translate(selected.getRole().getKey())));
    }
}