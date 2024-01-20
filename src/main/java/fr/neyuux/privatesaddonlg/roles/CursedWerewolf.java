package fr.neyuux.privatesaddonlg.roles;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.annotations.Role;
import fr.ph1lou.werewolfapi.enums.Aura;
import fr.ph1lou.werewolfapi.enums.Category;
import fr.ph1lou.werewolfapi.enums.RoleAttribute;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.events.game.day_cycle.DayEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.player.utils.Formatter;
import fr.ph1lou.werewolfapi.role.impl.RoleWereWolf;
import fr.ph1lou.werewolfapi.role.interfaces.IAffectedPlayers;
import fr.ph1lou.werewolfapi.role.interfaces.IPower;
import fr.ph1lou.werewolfapi.role.utils.DescriptionBuilder;
import fr.ph1lou.werewolfapi.utils.Utils;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Role(key = "privatesaddon.roles.cursed_werewolf.display",
        defaultAura = Aura.DARK,
        category = Category.WEREWOLF,
        attribute = RoleAttribute.WEREWOLF,
        auraDescriptionSpecialUseCase = "privatesaddon.roles.cursed_werewolf.aura")
public class CursedWerewolf extends RoleWereWolf implements IAffectedPlayers, IPower {
    private final List<IPlayerWW> affectedPlayer = new ArrayList<>();

    private boolean power = true;

    public CursedWerewolf(WereWolfAPI api, IPlayerWW playerWW) {
        super(api, playerWW);
    }

    public void setPower(boolean power) {
        this.power = power;
    }

    public boolean hasPower() {
        return this.power;
    }

    public void addAffectedPlayer(IPlayerWW playerWW) {
        this.affectedPlayer.add(playerWW);
    }

    public void removeAffectedPlayer(IPlayerWW playerWW) {
        this.affectedPlayer.remove(playerWW);
    }

    public void clearAffectedPlayer() {
        this.affectedPlayer.clear();
    }

    public List<IPlayerWW> getAffectedPlayers() {
        return this.affectedPlayer;
    }

    @NotNull
    public String getDescription() {
        DescriptionBuilder descBuilder = new DescriptionBuilder(this.game, this);

        descBuilder.setDescription("Vous pouvez à chaque maudire un joueur. La malédiction fera en sorte que ce joueur sera vu comme Loup-Garou par les rôles à informations du Village.");

        descBuilder.setEffects(this.game.translate("werewolf.description.werewolf"));

        descBuilder.setPower("Joueur maudit actuellement : §b" + (this.affectedPlayer.isEmpty() ? "Aucun" : this.affectedPlayer.get(0).getName()));

        descBuilder.setCommand("/ww curse");

        return descBuilder.build();
    }

    public void recoverPower() {}


    @EventHandler(priority = EventPriority.LOWEST)
    public void onDay(DayEvent ev) {
        if (!this.affectedPlayer.isEmpty()) {
            IPlayerWW last = this.affectedPlayer.remove(0);

            last.getRole().clearDisplay();
            last.getRole().removeAuraModifier(this.getKey());
        }
        if (!this.getPlayerWW().isState(StatePlayer.ALIVE))
            return;

        this.setPower(true);
        this.getPlayerWW().sendMessage(new TextComponent(Plugin.getPrefix() + "Vous avez §b" + Utils.conversion(this.game.getConfig().getTimerValue("werewolf.timers.power_duration.name")) +
                "§f pour Maudire un joueur. Ce joueur sera perçu comme §cLoup-Garou §fpar les rôles à informations du Village jusqu'au prochain jour."));
    }
}