package fr.neyuux.privatesaddonlg.commands.roles;

import fr.neyuux.privatesaddonlg.roles.WolfDog;
import fr.ph1lou.werewolfapi.annotations.RoleCommand;
import fr.ph1lou.werewolfapi.commands.ICommandRole;
import fr.ph1lou.werewolfapi.enums.Sound;
import fr.ph1lou.werewolfapi.events.UpdateNameTagEvent;
import fr.ph1lou.werewolfapi.events.roles.wolf_dog.WolfDogChooseWereWolfForm;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import org.bukkit.Bukkit;

@RoleCommand(key="privatesaddon.roles.wolf_dog.command", roleKeys={"privatesaddon.roles.wolf_dog.display"}, argNumbers={0})
public class TransformerCommand
        implements ICommandRole {
    @Override
    public void execute(WereWolfAPI game, IPlayerWW playerWW, String[] args) {

        WolfDog wolfDog = (WolfDog) playerWW.getRole();
        wolfDog.setPower(false);
        WolfDogChooseWereWolfForm event = new WolfDogChooseWereWolfForm(playerWW);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            playerWW.sendMessageWithKey("werewolf.prefix.red", "werewolf.check.cancel");
            return;
        }
        wolfDog.setTransformed(true);
        playerWW.sendMessageWithKey("werewolf.prefix.red", "privatesaddon.roles.wolf_dog.perform");
        Bukkit.getPluginManager().callEvent(new UpdateNameTagEvent(playerWW));
        game.checkVictory();
        Sound.LEVEL_UP.play(Bukkit.getPlayer(playerWW.getUUID()), 8f, 1.8f);
    }
}