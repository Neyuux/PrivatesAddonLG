package fr.neyuux.privatesaddonlg;

import fr.neyuux.privatesaddonlg.roles.InnkeeperBuffed;
import fr.ph1lou.werewolfapi.enums.Day;
import fr.ph1lou.werewolfapi.enums.StateGame;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.events.ActionBarEvent;
import fr.ph1lou.werewolfapi.events.game.game_cycle.WinEvent;
import fr.ph1lou.werewolfapi.events.game.utils.EnchantmentEvent;
import fr.ph1lou.werewolfapi.events.lovers.RevealLoversEvent;
import fr.ph1lou.werewolfapi.events.roles.SelectionEndEvent;
import fr.ph1lou.werewolfapi.game.IConfiguration;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class RoleBuffListener implements Listener {

    private BigDecimal barbarianHealed = BigDecimal.valueOf(0.0D);

    @EventHandler
    public void onBuffSharpnessCharmeuse(EnchantmentEvent ev) {
        final IPlayerWW playerWW = ev.getPlayerWW();

        if (!playerWW.isState(StatePlayer.ALIVE))
            return;

        final IConfiguration config = Plugin.getINSTANCE().getGame().getConfig();
        final Map<Enchantment, Integer> enchants = ev.getEnchants();

        if (playerWW.getRole().isKey("werewolf.roles.charmer.display"))
            if (enchants.containsKey(Enchantment.DAMAGE_ALL)) {
                if (ev.getItem().getType() == Material.DIAMOND_SWORD) {
                    ev.getFinalEnchants().put(Enchantment.DAMAGE_ALL, Math.min(enchants.get(Enchantment.DAMAGE_ALL), config.getLimitSharpnessDiamond() + 1));
                } else {
                    ev.getFinalEnchants().put(Enchantment.DAMAGE_ALL, Math.min(enchants.get(Enchantment.DAMAGE_ALL), config.getLimitSharpnessIron() + 1));
                }
            }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBarbarianHit(EntityDamageByEntityEvent ev) {
        Entity edamager = ev.getDamager();

        if (edamager.getType() != EntityType.PLAYER || ev.getEntityType() != EntityType.PLAYER || ev.isCancelled())
            return;

        Player damager = (Player)edamager;
        WereWolfAPI game = Plugin.getINSTANCE().getGame();
        Optional<IPlayerWW> optional = game.getPlayerWW(damager.getUniqueId());

        optional.ifPresent(damagerWW -> {
            if (damagerWW.getRole() != null && damagerWW.getRole().getKey().equals("werewolf.roles.barbarian.display")) {
                double damageBonusPercentage = calculateStrengthByHealth(damagerWW);

                ev.setDamage(ev.getDamage() * damageBonusPercentage);

                double finalDamage = ev.getFinalDamage();

                damagerWW.addPlayerHealth(ev.getFinalDamage() * 0.25D);

                barbarianHealed =  barbarianHealed.add(BigDecimal.valueOf(ev.getFinalDamage() - finalDamage));
            }
        });
    }

    @EventHandler
    public void onSelectionEnd(SelectionEndEvent ev) {
        Plugin.getINSTANCE().getGame().getPlayersWW().stream()
                .filter(playerWW -> playerWW.getRole() != null && playerWW.getRole().getKey().equals("werewolf.roles.barbarian.display"))
                .forEach(playerWW -> playerWW.sendMessage(new TextComponent(Plugin.getPrefix() + "§fEffets bonus : Vous régénérez §d§l225% §fdes dégâts que vous infligez. Vous possèdez un effet de force en fonction de votre barre de vie (5% -> 35%).")));
    }

    @EventHandler
    public void onActionBarEvent(ActionBarEvent event) {
        WereWolfAPI game = Plugin.getINSTANCE().getGame();

        if (!game.isState(StateGame.GAME))
            return;
        UUID uuid = event.getPlayerUUID();
        IPlayerWW playerWW = game.getPlayerWW(uuid).orElse(null);
        StringBuilder sb = new StringBuilder(event.getActionBar());
        Player player = Bukkit.getPlayer(uuid);

        if (player == null)
            return;
        if (playerWW == null || playerWW.getRole() == null)
            return;
        if (!playerWW.isState(StatePlayer.ALIVE))
            return;

        if (playerWW.getRole().getKey().equals("werewolf.roles.barbarian.display")) {
            sb.append(" §7| §cForce : §l").append(new DecimalFormat("#").format(calculateStrengthByHealth(playerWW) * 100.0 - 100.0)).append("%");
            event.setActionBar(sb.toString());

        } else if (playerWW.getRole().getKey().equals("privatesaddon.roles.innkeeperbuffed.display") && game.isDay(Day.DAY)) {
            InnkeeperBuffed innkeeper = (InnkeeperBuffed) playerWW.getRole();
            sb.append(" §7| §fChambres disponibles : §l").append(innkeeper.getAvailableRooms() - innkeeper.getClientDatas().size());
            event.setActionBar(sb.toString());
        }
    }
    @EventHandler
    public void onLoverDurationEnd(RevealLoversEvent event) {
        event.getLovers().forEach(iLover -> iLover.getLovers().stream().filter(iPlayerWW -> iPlayerWW.getRole().isKey("werewolf.roles.charmer.display")).forEach(iPlayerWW -> Bukkit.getPlayer(iPlayerWW.getUUID()).sendMessage(Plugin.getPrefix() + "§dVous pouvez utiliser la commande §e/ww charmstop §dpour révéler votre identité à votre charmé. (Vous ne pourrez plus vous faire de dons de vie et vous n'aurez plus la flèche permettant de vous traquer).")));
    }

    @EventHandler
    public void onEndSendStats(WinEvent ev) {
        Plugin.getINSTANCE().getGame().getPlayersWW().stream().filter(playerWW -> playerWW.getRole() != null && playerWW.getRole().isKey("werewolf.roles.barbarian.display")).findFirst().ifPresent(playerWW -> {
            Bukkit.broadcastMessage(Plugin.getPrefix() + "§d§lSoins du §c§lBarbare §c" + playerWW.getName() + " §d: §l" + barbarianHealed.setScale(1, RoundingMode.HALF_UP).doubleValue() + " HP");
        });
    }


    private double calculateHealthPercentage(double currentHealth, double maxHealth) {
        if (currentHealth <= 0.0 || maxHealth <= 0.0) {
            return 0.0;
        }

        double healthPercentage = currentHealth / maxHealth * 100.0;
        return Math.min(healthPercentage, 100.0);
    }

    private double calculateStrengthByHealth(IPlayerWW damagerWW) {
        double minDamageBonusPercentage = 1.35;
        double maxDamageBonusPercentage = 1.05;
        double healthPercentage = calculateHealthPercentage(damagerWW.getHealth(), damagerWW.getMaxHealth()) / 100.0D;

        return minDamageBonusPercentage +
                (maxDamageBonusPercentage - minDamageBonusPercentage) * healthPercentage;
    }
}
