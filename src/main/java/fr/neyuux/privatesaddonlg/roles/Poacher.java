package fr.neyuux.privatesaddonlg.roles;

import fr.ph1lou.werewolfapi.annotations.IntValue;
import fr.ph1lou.werewolfapi.annotations.Role;
import fr.ph1lou.werewolfapi.annotations.Timer;
import fr.ph1lou.werewolfapi.enums.Aura;
import fr.ph1lou.werewolfapi.enums.Category;
import fr.ph1lou.werewolfapi.enums.RoleAttribute;
import fr.ph1lou.werewolfapi.enums.UniversalMaterial;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.role.impl.RoleImpl;
import fr.ph1lou.werewolfapi.role.utils.DescriptionBuilder;
import org.jetbrains.annotations.NotNull;

@Role(
        key = "privatesaddon.roles.poacher.display",
        defaultAura = Aura.NEUTRAL,
        attribute = RoleAttribute.VILLAGER,
        category = Category.VILLAGER,
        timers = {@Timer(key = "privatesaddon.roles.poacher.timers.progress", defaultValue = 60, meetUpValue = 60)},
        configValues = {@IntValue(key = "privatesaddon.roles.poacher.configurations.distance", defaultValue = 10, meetUpValue = 10, step = 1, item = UniversalMaterial.ARROW)}
)
public class Poacher extends RoleImpl {

    private int furNumbers = 0;

    public Poacher(@NotNull WereWolfAPI game, @NotNull IPlayerWW playerWW) {
        super(game, playerWW);
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
    }

    @Override
    public String getDisplayCamp() {
        if (this.furNumbers > 0 || isWereWolf())
            return Category.WEREWOLF.getKey();
        return Category.VILLAGER.getKey();
    }
}
