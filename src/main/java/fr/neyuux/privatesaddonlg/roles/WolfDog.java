package fr.neyuux.privatesaddonlg.roles;

import fr.ph1lou.werewolfapi.annotations.Role;
import fr.ph1lou.werewolfapi.enums.*;
import fr.ph1lou.werewolfapi.events.game.day_cycle.NightEvent;
import fr.ph1lou.werewolfapi.events.game.timers.WereWolfListEvent;
import fr.ph1lou.werewolfapi.events.werewolf.AppearInWereWolfListEvent;
import fr.ph1lou.werewolfapi.events.werewolf.RequestSeeWereWolfListEvent;
import fr.ph1lou.werewolfapi.events.werewolf.WereWolfCanSpeakInChatEvent;
import fr.ph1lou.werewolfapi.events.werewolf.WereWolfChatEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.impl.AuraModifier;
import fr.ph1lou.werewolfapi.player.impl.PotionModifier;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.player.utils.Formatter;
import fr.ph1lou.werewolfapi.role.impl.RoleImpl;
import fr.ph1lou.werewolfapi.role.interfaces.IPower;
import fr.ph1lou.werewolfapi.role.interfaces.IRole;
import fr.ph1lou.werewolfapi.role.interfaces.ITransformed;
import fr.ph1lou.werewolfapi.role.utils.DescriptionBuilder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

@Role(key = "privatesaddon.roles.wolf_dog.display", category = Category.VILLAGER, attribute = RoleAttribute.HYBRID, defaultAura = Aura.DARK)
public class WolfDog extends RoleImpl implements ITransformed, IPower {
    private boolean transformed = false;

    private boolean power = true;

    public WolfDog(WereWolfAPI api, IPlayerWW playerWW) {
        super(api, playerWW);
    }

    @Override
    @NotNull
    public String getDescription() {
        return (new DescriptionBuilder(this.game, this))
                .setDescription(this.power ? (

                        this.game.translate("privatesaddon.roles.wolf_dog.description", new Formatter[0]) + '\n' + this.game.translate("privatesaddon.roles.wolf_dog.description_2")) :

                        this.game.translate(this.transformed ? "privatesaddon.roles.wolf_dog.description_2" :

                                "privatesaddon.roles.wolf_dog.description"))
                .build();
    }

    @Override
    public void recoverPower() {
        int timer = this.game.getConfig().getTimerValue("werewolf.timers.werewolf_list.name");
        if (timer > 0)
            getPlayerWW().sendMessageWithKey("werewolf.prefix.green", "werewolf.roles.wolf_dog.transform", Formatter.timer(this.game, "werewolf.timers.werewolf_list.name"));
    }

    @Override
    public boolean isWereWolf() {
        return (super.isWereWolf() || this.transformed);
    }

    @EventHandler
    public void onWereWolfList(WereWolfListEvent event) {
        if (this.power)
            getPlayerWW().sendMessageWithKey("werewolf.prefix.red", "werewolf.roles.wolf_dog.time_over");
        this.power = false;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onWerewolfChat(WereWolfChatEvent event){

        if(!event.getTargetWW().equals(this.getPlayerWW())){
            return;
        }

        event.setCancelled(this.transformed && !super.isWereWolf());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onNightForWereWolf(NightEvent event) {
        if (!isAbilityEnabled())
            return;
        if (this.isWereWolf())
            getPlayerWW().addPotionModifier(PotionModifier.add(PotionEffectType.INCREASE_DAMAGE, "werewolf.roles.werewolf.display"));
        else
            getPlayerWW().addPotionModifier(PotionModifier.remove(PotionEffectType.INCREASE_DAMAGE, "werewolf.roles.werewolf.display", 0));
    }

    @EventHandler
    public void onChatSpeak(WereWolfCanSpeakInChatEvent event) {

        if (!event.getPlayerWW().equals(getPlayerWW())) return;

        event.setCanSpeak(!this.transformed || super.isWereWolf());
    }

    @EventHandler
    public void onAppearInWereWolfList(AppearInWereWolfListEvent event) {

        if (!getPlayerWW().equals(event.getTargetWW())) return;

        if (this.getPlayerWW().isState(StatePlayer.DEATH)) return;

        event.setAppear(!this.transformed || super.isWereWolf());
    }

    @EventHandler
    public void onRequestSeeWereWolfListEvent(RequestSeeWereWolfListEvent event) {

        if (!getPlayerWW().equals(event.getPlayerWW())) return;

        if (this.getPlayerWW().isState(StatePlayer.DEATH)) return;

        event.setAccept(this.transformed || super.isWereWolf());
    }

    @Override
    public String getDisplayCamp() {
        if (this.transformed)
            return Camp.VILLAGER.getKey();
        return Camp.WEREWOLF.getKey();
    }

    @Override
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

    @Override
    public boolean isTransformed() {
        return this.transformed;
    }

    @Override
    public void setTransformed(boolean transformed) {
        if (transformed) {
            this.addAuraModifier(new AuraModifier(this.getKey(), Aura.LIGHT, 1, false));
        }
        this.transformed = transformed;
    }

    @Override
    public void setPower(boolean power) {
        this.power = power;
    }

    @Override
    public boolean hasPower() {
        return this.power;
    }
}
