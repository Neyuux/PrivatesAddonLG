package fr.neyuux.privatesaddonlg.roles;

import fr.ph1lou.werewolfapi.annotations.Role;
import fr.ph1lou.werewolfapi.enums.Aura;
import fr.ph1lou.werewolfapi.enums.Camp;
import fr.ph1lou.werewolfapi.enums.Category;
import fr.ph1lou.werewolfapi.enums.RoleAttribute;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.events.game.day_cycle.NightEvent;
import fr.ph1lou.werewolfapi.events.game.timers.WereWolfListEvent;
import fr.ph1lou.werewolfapi.events.werewolf.AppearInWereWolfListEvent;
import fr.ph1lou.werewolfapi.events.werewolf.WereWolfCanSpeakInChatEvent;
import fr.ph1lou.werewolfapi.events.werewolf.WereWolfChatEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.impl.PotionModifier;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.player.utils.Formatter;
import fr.ph1lou.werewolfapi.role.impl.RoleVillage;
import fr.ph1lou.werewolfapi.role.interfaces.IPower;
import fr.ph1lou.werewolfapi.role.interfaces.IRole;
import fr.ph1lou.werewolfapi.role.interfaces.ITransformed;
import fr.ph1lou.werewolfapi.role.utils.DescriptionBuilder;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

@Role(key = "privatesaddon.roles.wolf_dog.display", category = Category.VILLAGER, attributes = {RoleAttribute.HYBRID})
public class WolfDog extends RoleVillage implements ITransformed, IPower {
    private boolean transformed = false;

    private boolean power = true;

    public WolfDog(WereWolfAPI api, IPlayerWW playerWW) {
        super(api, playerWW);
    }

    @NotNull
    public String getDescription() {
        return (new DescriptionBuilder(this.game, this))
                .setDescription(this.power ? (

                        this.game.translate("privatesaddon.roles.wolf_dog.description", new Formatter[0]) + '\n' + this.game.translate("privatesaddon.roles.wolf_dog.description_2", new Formatter[0])) :

                        this.game.translate(this.transformed ? "privatesaddon.roles.wolf_dog.description_2" :

                                "privatesaddon.roles.wolf_dog.description", new Formatter[0]))
                .build();
    }

    public void recoverPower() {
        int timer = this.game.getConfig().getTimerValue("werewolf.timers.werewolf_list.name");
        if (timer > 0)
            getPlayerWW().sendMessageWithKey("werewolf.prefix.green", "werewolf.roles.wolf_dog.transform", new Formatter[] { Formatter.timer(this.game, "werewolf.timers.werewolf_list.name") });
    }

    public boolean isWereWolf() {
        return (super.isWereWolf() || this.transformed);
    }

    public Aura getDefaultAura() {
        return this.transformed ? Aura.LIGHT : Aura.DARK;
    }

    @EventHandler
    public void onWereWolfList(WereWolfListEvent event) {
        if (this.power)
            getPlayerWW().sendMessageWithKey("werewolf.prefix.red", "werewolf.roles.wolf_dog.time_over", new Formatter[0]);
        this.power = false;
    }

    @EventHandler
    public void onWWChat(WereWolfChatEvent event) {
        if (event.isCancelled())
            return;
        if (this.transformed && !super.isWereWolf()) {
            event.setCancelled(true);
            return;
        }
        if (!getPlayerWW().isState(StatePlayer.ALIVE))
            return;
        event.sendMessage(getPlayerWW());
    }

    @EventHandler
    public void onNightForWereWolf(NightEvent event) {
        if (!isAbilityEnabled())
            return;
        if (this.isWereWolf())
            getPlayerWW().addPotionModifier(PotionModifier.add(PotionEffectType.INCREASE_DAMAGE, "werewolf.roles.werewolf.display"));
        if (this.transformed && !super.isWereWolf())
            return;
        if (!this.game.getConfig().isConfigActive("werewolf.configurations.werewolf_chat.name"))
            return;
        openWereWolfChat();
    }

    @EventHandler
    public void onChatSpeak(WereWolfCanSpeakInChatEvent event) {
        if (!getPlayerWW().isState(StatePlayer.ALIVE))
            return;
        if (!event.getPlayerWW().equals(getPlayerWW()))
            return;
        if (this.transformed && !super.isWereWolf())
            return;
        event.setCanSpeak(true);
    }

    @EventHandler
    public void onAppearInWereWolfList(AppearInWereWolfListEvent event) {
        if (!getPlayerUUID().equals(event.getPlayerUUID()))
            return;
        if (getPlayerWW().isState(StatePlayer.DEATH))
            return;
        event.setAppear((!this.transformed || super.isWereWolf()));
    }

    public String getDisplayCamp() {
        if (this.transformed)
            return Camp.VILLAGER.getKey();
        return Camp.WEREWOLF.getKey();
    }

    public String getDisplayRole() {
        if (this.transformed)
            return this.game.getPlayersWW().stream()
                    .filter(playerWW -> playerWW.isState(StatePlayer.ALIVE))
                    .map(IPlayerWW::getRole)
                    .filter(roles -> roles.isCamp(Camp.VILLAGER))
                    .map(IRole::getKey)
                    .findFirst()
                    .orElse(getKey());
        return this.game.getPlayersWW().stream()
                .filter(playerWW -> playerWW.isState(StatePlayer.ALIVE))
                .map(IPlayerWW::getRole)
                .filter(role -> role.isDisplayCamp(Camp.WEREWOLF.getKey()))
                .map(IRole::getKey)
                .findFirst()
                .orElse(getKey());
    }

    public boolean isTransformed() {
        return this.transformed;
    }

    public void setTransformed(boolean transformed) {
        this.transformed = transformed;
    }

    public void setPower(boolean power) {
        this.power = power;
    }

    public boolean hasPower() {
        return this.power;
    }
}
