package fr.neyuux.privatesaddonlg.commands.roles;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.neyuux.privatesaddonlg.roles.Omniscient;
import fr.ph1lou.werewolfapi.annotations.RoleCommand;
import fr.ph1lou.werewolfapi.basekeys.RoleBase;
import fr.ph1lou.werewolfapi.commands.ICommandRole;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.events.roles.fox.BeginSniffEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.lovers.ILover;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.role.interfaces.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@RoleCommand(key="privatesaddon.roles.omniscient.command", roleKeys={"privatesaddon.roles.omniscient.display"}, argNumbers={0, 1})
public class TranscenderCommand
        implements ICommandRole {
    @Override
    public void execute(WereWolfAPI game, IPlayerWW playerWW, String[] args) {
        UUID uuid = playerWW.getUUID();
        Omniscient omniscient = (Omniscient) playerWW.getRole();
        Player player = Bukkit.getPlayer(uuid);

        if (args.length == 0) {

            List<IPlayerWW> players = new ArrayList<>();

            player.sendMessage("§f§m                                                                           §r");
            player.sendMessage(Plugin.getPrefix() + "Liste des rôles de la partie : ");
            player.sendMessage("");

            players.addAll(game.getPlayersWW().stream().filter(this::isWerewolf).collect(Collectors.toList()));
            players.addAll(game.getPlayersWW().stream().filter(playerWW1 -> playerWW1.getRole().isNeutral() || playerWW1.getRole().isSolitary()).collect(Collectors.toList()));
            players.addAll(game.getPlayersWW().stream().filter(this::isVillager).collect(Collectors.toList()));

            players.forEach(playerWW1 -> player.sendMessage(this.getColor(playerWW1) + " §fest " + this.getColor(playerWW1) + Plugin.getRoleTranslated(playerWW1.getRole().getKey())));

            player.sendMessage("§f§m                                                                           §r");
            return;
        }

        AtomicReference<UUID> playerAtomicUUID = new AtomicReference<>();

        game.getPlayersWW()
                .stream()
                .filter(playerWW1 -> playerWW1.getName().equalsIgnoreCase(args[0]))
                .forEach(playerWW1 -> playerAtomicUUID.set(playerWW1.getUUID()));

        if (playerAtomicUUID.get() == null) {
            player.sendMessage(Plugin.getPrefixWithColor(ChatColor.RED) + "§cCe joueur n'est pas en ligne !");
            return;
        }

        UUID playerUUID = playerAtomicUUID.get();
        IPlayerWW targetWW = game.getPlayerWW(playerUUID).orElse(null);
        ChatColor c = this.getColor(targetWW);

        if (targetWW == null) {
            player.sendMessage(Plugin.getPrefixWithColor(ChatColor.RED) + "§cCe joueur n'est pas dans la partie !");
            return;
        }

        player.sendMessage("§f§m                                                                           §r");
        player.sendMessage(" Informations sur " + c + targetWW.getName() + " §f:");
        player.sendMessage("");

        IRole role = targetWW.getRole();
        player.sendMessage(" §0§l■ §fRôle : " + c + Plugin.getRoleTranslated(role.getKey()));

        if (role instanceof IPower)
            player.sendMessage(" §0§l■ §fPouvoir Disponible : " + this.getYesOrNo(((IPower) role).hasPower()));

        if (role instanceof ITransformed) {
            player.sendMessage(" §0§l■ §fTransformé : " + this.getYesOrNo(((ITransformed) role).isTransformed()));
        }

        for (ILover lover : targetWW.getLovers()) {

            StringBuilder sb = new StringBuilder();
            lover.getLovers().stream()
                    .filter(playerWW1 -> !targetWW.equals(playerWW1))
                    .forEach(playerWW1 -> sb.append(playerWW1.getName()).append(" "));

            if (sb.length() != 0) {
                if (!lover.getKey().contains("cursed_lover")) {

                    player.sendMessage(" §0§l■ §fEn couple avec : " + c + sb);

                } else {

                    player.sendMessage(" §0§l■ §fEn Couple Maudit avec : " + c + sb);
                }
            }
        }

        StringBuilder sb = new StringBuilder();

        if (targetWW.getRole() instanceof IAffectedPlayers) {
            IAffectedPlayers affectedPlayers = (IAffectedPlayers) targetWW.getRole();

            for (IPlayerWW playerWW1 : affectedPlayers.getAffectedPlayers()) {
                if (playerWW1 != null) {
                    sb.append(playerWW1.getName()).append(" ");
                }
            }
            if (sb.length() != 0) {
                player.sendMessage(" §0§l■ §fJoueurs affectés : " + c + sb);
            }
        }

        if (role.isKey(RoleBase.SISTER)) {
            sb = new StringBuilder();

            for (IPlayerWW playerWW1 : game.getPlayersWW()) {
                if (playerWW1.getRole().isKey(RoleBase.SISTER) && !playerWW1.equals(targetWW)) {
                    sb.append(playerWW1.getName()).append(" ");
                }
            }
            if (sb.length() != 0) {

                player.sendMessage(" §0§l■ §fSoeur(s) : " + c + sb);
            }
        }

        if (role.isKey(RoleBase.SIAMESE_TWIN)) {
            sb = new StringBuilder();

            for (IPlayerWW playerWW1 : game.getPlayersWW()) {
                if (playerWW1.getRole().isKey(RoleBase.SIAMESE_TWIN) && !playerWW1.equals(targetWW)) {
                    sb.append(playerWW1.getName()).append(" ");
                }
            }
            if (sb.length() != 0) {

                player.sendMessage(" §0§l■ §fFrère(s) Siamois : " + c + sb);
            }
        }

        sb = new StringBuilder();

        for (IPlayerWW playerWW1 : targetWW.getKillers()) {
            if (playerWW1 != null) {
                sb.append(playerWW1.getName()).append(" ");
            } else sb.append(game.translate("werewolf.utils.pve")).append(" ");
        }

        if (sb.length() != 0) {
            player.sendMessage(" §0§l■ §fTué par : " + c + sb);
        }

        player.sendMessage("§f§m                                                                           §r");
    }


    private boolean isWerewolf(IPlayerWW playerWW) {
        IRole role = playerWW.getRole();
        return role.isWereWolf() && !role.isNeutral() && !role.isSolitary();
    }

    private boolean isVillager(IPlayerWW playerWW) {
        IRole role = playerWW.getRole();
        return !role.isWereWolf() && !role.isNeutral() && !role.isSolitary();
    }

    private ChatColor getColor(IPlayerWW playerWW) {
        if (this.isWerewolf(playerWW))
            return ChatColor.DARK_RED;
        else if (this.isVillager(playerWW))
            return ChatColor.GREEN;
        else
            return ChatColor.GOLD;
    }

    private String getYesOrNo(boolean b) {
        return (b ? "§a§lOUI" : "§c§lNON");
    }
}