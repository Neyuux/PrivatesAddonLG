package fr.neyuux.privatesaddonlg.roles;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.annotations.IntValue;
import fr.ph1lou.werewolfapi.annotations.Role;
import fr.ph1lou.werewolfapi.enums.*;
import fr.ph1lou.werewolfapi.events.game.day_cycle.DayEvent;
import fr.ph1lou.werewolfapi.events.game.day_cycle.NightEvent;
import fr.ph1lou.werewolfapi.events.game.life_cycle.FinalDeathEvent;
import fr.ph1lou.werewolfapi.events.roles.innkeeper.*;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.role.impl.RoleVillage;
import fr.ph1lou.werewolfapi.role.interfaces.IPower;
import fr.ph1lou.werewolfapi.role.utils.DescriptionBuilder;
import lombok.Getter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Role(key="privatesaddon.roles.innkeeperbuffed.display", category=Category.VILLAGER, attributes={RoleAttribute.VILLAGER, RoleAttribute.MINOR_INFORMATION}, configValues={@IntValue(key="privatesaddon.roles.innkeeperbuffed.configurations.detection_radius", defaultValue=10, meetUpValue=10, step=1, item=UniversalMaterial.IRON_DOOR)})
public class InnkeeperBuffed
        extends RoleVillage
        implements IPower {

    @Getter
    private final List<ClientData> clientDatas = new ArrayList<>();
    private final List<ClientData> previousClientDatas = new ArrayList<>();
    private boolean power = false;

    @Getter
    private int availableRooms = 3;

    public InnkeeperBuffed(WereWolfAPI game, IPlayerWW playerWW) {
        super(game, playerWW);

        this.enableAbilities();
    }

    @Override
    @NotNull
    public String getDescription() {
        return new DescriptionBuilder(this.game, this).setDescription(this.game.translate("privatesaddon.roles.innkeeperbuffed.description")).setEffects(this.game.translate("privatesaddon.roles.innkeeperbuffed.effect")).build();
    }

    @Override
    public void recoverPower() {
    }

    @EventHandler
    public void onKill(FinalDeathEvent event) {
        if (!this.hasPower() || !this.getPlayerWW().isState(StatePlayer.ALIVE)) {
            return;
        }
        this.clientDatas.stream().filter(clientData -> clientData.playerWW.equals(event.getPlayerWW())).findFirst().ifPresent(clientData -> {
            String role = clientData.playerWW.getLastKiller().isPresent() ? this.game.translate(clientData.playerWW.getLastKiller().get().getRole().getKey()) : "pve";
            ClientDeathEvent clientDeathEvent = new ClientDeathEvent(this.getPlayerWW(), clientData.playerWW, role);
            Bukkit.getPluginManager().callEvent(clientDeathEvent);
            if (!clientDeathEvent.isCancelled()) {
                Player player = Bukkit.getPlayer(this.getPlayerUUID());
                if (player != null) {
                    player.sendMessage(Plugin.getPrefix() + "§fVotre client §e§l" + event.getPlayerWW().getName() + "§f est mort ! Le rôle de son tueur est §c§l" + clientDeathEvent.getRole() + "§f.");
                }
                --this.availableRooms;
                clientData.watching = false;
                this.clientDatas.remove(clientData);
                this.checkAvaibleRooms(player);
            }
        });
        event.getPlayerWW().getLastKiller().ifPresent(killer -> {
            if (this.clientDatas.stream().anyMatch(clientData -> clientData.playerWW.equals(killer))) {
                ClientKillEvent clientKillEvent = new ClientKillEvent(this.getPlayerWW(), killer);
                Bukkit.getPluginManager().callEvent(clientKillEvent);
                if (!clientKillEvent.isCancelled()) {
                    this.getPlayerWW().sendMessage(new TextComponent(Plugin.getPrefix() + "§fVotre client §c§l" + killer.getName() + " §fvient de faire un kill ! Son rôle est §c§l" + Plugin.getRoleTranslated(killer.getRole().getDisplayRole()) +"§f."));
                    this.getPlayerWW().sendMessage(new TextComponent(Plugin.getPrefix() + "§fUne de vos chambre vient de fermer."));
                    this.availableRooms--;
                    this.clientDatas.forEach(clientData -> clientData.watching = false);
                    this.checkAvaibleRooms(Bukkit.getPlayer(this.getPlayerUUID()));
                }
            }
        });
    }

    private void checkAvaibleRooms(Player player) {
        if (this.availableRooms == 0 && player != null) {
            InnkeeperSpeedEvent innkeeperSpeedEvent = new InnkeeperSpeedEvent(this.getPlayerWW());
            Bukkit.getPluginManager().callEvent(innkeeperSpeedEvent);
            if (!innkeeperSpeedEvent.isCancelled()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
                player.sendMessage(Plugin.getPrefix() + "§fToutes vos chambres sont fermées ! Vous obtenez donc l'effet §b§lVitesse§f.");
            }
        }
    }

    @Override
    public void disableAbilitiesRole() {
        this.power = false;
    }

    @EventHandler
    public void onDay(DayEvent event) {
        if (!this.isAbilityEnabled() || !this.getPlayerWW().isState(StatePlayer.ALIVE)) {
            return;
        }
        this.power = true;
        this.getPlayerWW().sendMessage(new TextComponent(Plugin.getPrefix() + "§fVotre pouvoir est à nouveau disponible."));
        this.previousClientDatas.clear();
        this.previousClientDatas.addAll(this.clientDatas);
        this.clientDatas.clear();
    }

    @EventHandler
    public void onNight(NightEvent event) {
        this.power = false;
        if (!this.hasPower() || !this.getPlayerWW().isState(StatePlayer.ALIVE)) {
            return;
        }
        this.clientDatas.forEach(clientData -> (new BukkitRunnable() {
            public void run() {
                if (InnkeeperBuffed.this.game.isDay(Day.DAY) || !clientData.watching || InnkeeperBuffed.this.game.isState(StateGame.END)) {
                    cancel();
                } else {
                    Objects.requireNonNull(clientData.seenPlayers);
                    InnkeeperBuffed.this.game.getPlayersWW().stream()
                            .filter(iPlayerWW -> !iPlayerWW.equals(InnkeeperBuffed.this.getPlayerWW()))
                            .filter(iPlayerWW -> !iPlayerWW.equals(clientData.playerWW))
                            .filter(iPlayerWW -> iPlayerWW.isState(StatePlayer.ALIVE)).filter(iPlayerWW -> iPlayerWW.getLocation().getWorld() == clientData.playerWW.getLocation().getWorld() && ((iPlayerWW.getLocation().distance(clientData.playerWW.getLocation()) <= InnkeeperBuffed.this.game.getConfig().getValue("privatesaddon.roles.innkeeperbuffed.configurations.detection_radius"))))
                            .forEach(clientData.seenPlayers::add);
                }
            }
        }).runTaskTimerAsynchronously(Plugin.getINSTANCE(), 0L, 100L));
    }

    @EventHandler
    public void onRightClick(PlayerInteractAtEntityEvent event) {
        if (!this.hasPower()) {
            return;
        }
        if (event.getPlayer().getUniqueId() != this.getPlayerUUID() || !this.getPlayerWW().isState(StatePlayer.ALIVE)) {
            return;
        }
        if (this.game.isDay(Day.NIGHT)) {
            return;
        }
        IPlayerWW playerWW = this.game.getPlayerWW(event.getRightClicked().getUniqueId()).orElse(null);
        if (playerWW == null) {
            return;
        }
        Optional<ClientData> clientDataOptional = this.previousClientDatas.stream().filter(clientData -> clientData.playerWW.equals(playerWW)).findFirst();
        if (clientDataOptional.isPresent()) {
            ClientData clientData2 = clientDataOptional.get();
            if (!clientData2.seenPlayers.isEmpty()) {
                ArrayList<IPlayerWW> playerWWS = new ArrayList<>(clientData2.seenPlayers);
                Collections.shuffle(playerWWS, this.game.getRandom());
                InnkeeperInfoMeetEvent innkeeperInfoMeetEvent = new InnkeeperInfoMeetEvent(this.getPlayerWW(), playerWWS.get(0), playerWWS.size());
                Bukkit.getPluginManager().callEvent(innkeeperInfoMeetEvent);
                if (!innkeeperInfoMeetEvent.isCancelled()) {
                    this.getPlayerWW().sendMessage(new TextComponent(Plugin.getPrefix() + "§fCe client a vu §b§l" + playerWWS.size() + "§f personne" + (playerWWS.size() == 1 ? "" : "s") + " dont §b§l" + playerWWS.get(0).getName() + "§f."));
                }
            } else {
                this.getPlayerWW().sendMessage(new TextComponent(Plugin.getPrefix() + "§cCe client n'a vu personne cette nuit."));
            }
            this.previousClientDatas.remove(clientData2);
            return;
        }
        if (this.clientDatas.stream().anyMatch(clientData -> clientData.playerWW.equals(playerWW))) {
            this.getPlayerWW().sendMessage(new TextComponent(Plugin.getPrefix() + "§cVous hébergez déjà ce joueur."));
        } else if (this.clientDatas.size() < this.availableRooms) {
            InnkeeperHostEvent hostEvent = new InnkeeperHostEvent(this.getPlayerWW(), playerWW);
            Bukkit.getPluginManager().callEvent(hostEvent);
            if (!hostEvent.isCancelled()) {
                this.clientDatas.add(new ClientData(playerWW));
                this.getPlayerWW().sendMessage(new TextComponent(Plugin.getPrefix() + "§aVous avez bien donné une chambre à §b§l" + playerWW.getName() + "§a."));
            }
        } else {
            this.getPlayerWW().sendMessage(new TextComponent(Plugin.getPrefix() + "§cVous n'avez plus assez de chambres pour héberger ce joueur."));
        }
    }

    @Override
    public void setPower(boolean b) {
        this.power = b;
    }

    @Override
    public boolean hasPower() {
        return this.power;
    }

    private static class ClientData {
        private final IPlayerWW playerWW;
        private final Set<IPlayerWW> seenPlayers = new HashSet<>();
        private boolean watching = true;

        public ClientData(IPlayerWW playerWW) {
            this.playerWW = playerWW;
        }
    }
}