package fr.neyuux.privatesaddonlg.listeners;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.neyuux.privatesaddonlg.roles.InnkeeperBuffed;
import fr.neyuux.privatesaddonlg.utils.Reflection;
import fr.ph1lou.werewolfapi.enums.*;
import fr.ph1lou.werewolfapi.events.ActionBarEvent;
import fr.ph1lou.werewolfapi.events.UpdatePlayerNameTagEvent;
import fr.ph1lou.werewolfapi.events.game.game_cycle.WinEvent;
import fr.ph1lou.werewolfapi.events.game.timers.RepartitionEvent;
import fr.ph1lou.werewolfapi.events.game.utils.EnchantmentEvent;
import fr.ph1lou.werewolfapi.events.lovers.RevealLoversEvent;
import fr.ph1lou.werewolfapi.events.roles.StealEvent;
import fr.ph1lou.werewolfapi.events.roles.angel.AngelTargetEvent;
import fr.ph1lou.werewolfapi.events.roles.charmer.CharmerGetEffectDeathEvent;
import fr.ph1lou.werewolfapi.events.roles.falsifier_werewolf.NewDisplayRole;
import fr.ph1lou.werewolfapi.events.roles.priestess.PriestessEvent;
import fr.ph1lou.werewolfapi.game.IConfiguration;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.impl.PotionModifier;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.role.interfaces.IAffectedPlayers;
import fr.ph1lou.werewolfapi.utils.Utils;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class RoleBuffListener implements Listener {

    private final HashMap<IPlayerWW, BigDecimal> statsSaved = new HashMap<>();
    private final HashMap<IPlayerWW, Camp> imitatorBuff = new HashMap<>();
    private final HashSet<IPlayerWW> charmerResistance = new HashSet<>();
    private final HashSet<IPlayerWW> imitators = new HashSet<>();

    @EventHandler
    public void onBuffSharpnessSolo(EnchantmentEvent ev) {
        final IPlayerWW playerWW = ev.getPlayerWW();

        if (!playerWW.isState(StatePlayer.ALIVE))
            return;

        final IConfiguration config = Plugin.getINSTANCE().getGame().getConfig();
        final Map<Enchantment, Integer> enchants = ev.getEnchants();

        if (Plugin.getINSTANCE().hasAttribute(RoleAttribute.NEUTRAL, playerWW.getRole()))
            if (enchants.containsKey(Enchantment.DAMAGE_ALL)) {
                if (ev.getItem().getType() == Material.DIAMOND_SWORD) {
                    ev.getFinalEnchants().put(Enchantment.DAMAGE_ALL, Math.min(enchants.get(Enchantment.DAMAGE_ALL), config.getLimitSharpnessDiamond() + 1));
                } else {
                    ev.getFinalEnchants().put(Enchantment.DAMAGE_ALL, Math.min(enchants.get(Enchantment.DAMAGE_ALL), config.getLimitSharpnessIron() + 1));
                }
            }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onModifyDamages(EntityDamageByEntityEvent ev) {
        Entity edamager = ev.getDamager();

        if (edamager.getType() != EntityType.PLAYER || ev.getEntityType() != EntityType.PLAYER || ev.isCancelled())
            return;

        Player damager = (Player)edamager;
        WereWolfAPI game = Plugin.getINSTANCE().getGame();
        Optional<IPlayerWW> optional = game.getPlayerWW(damager.getUniqueId());
        Optional<IPlayerWW> playerWW = game.getPlayerWW(ev.getEntity().getUniqueId());

        optional.ifPresent(damagerWW -> {

            if (damagerWW.getRole() == null)
                return;

            switch (damagerWW.getRole().getKey()) {
                case "werewolf.roles.barbarian.display":
                    double damageBonusPercentage = calculateStrengthByHealth(damagerWW);

                    ev.setDamage(ev.getDamage() * damageBonusPercentage);

                    damagerWW.addPlayerHealth(ev.getFinalDamage() * 0.20D);

                    statsSaved.put(damagerWW, statsSaved.get(damagerWW).add(BigDecimal.valueOf(ev.getFinalDamage() * 0.20D)));

                    break;
                case "werewolf.roles.imitator.display":
                    if (this.imitatorBuff.containsKey(damagerWW) && playerWW.isPresent() && playerWW.get().getRole().getCamp().equals(this.imitatorBuff.get(damagerWW))) {

                        double finalDamages = ev.getFinalDamage();

                        ev.setDamage(ev.getDamage() * 1.15D);

                        statsSaved.put(damagerWW, statsSaved.get(damagerWW).add(BigDecimal.valueOf(ev.getFinalDamage() - finalDamages)));
                    }

                case "werewolf.roles.rival.display":

                    if (playerWW.isPresent() && !playerWW.get().getLovers().isEmpty()) {
                        double finalDamages = ev.getFinalDamage();

                        ev.setDamage(ev.getDamage() * 1.20D);

                        statsSaved.put(damagerWW, statsSaved.get(damagerWW).add(BigDecimal.valueOf(ev.getFinalDamage() - finalDamages)));
                    }

                    break;
                default:
                    break;
            }
        });


        playerWW.ifPresent(playerWW1 -> {

            if (playerWW1.getRole() == null)
                return;

            switch (playerWW1.getRole().getKey()) {
                case "werewolf.roles.charmer.display":
                    if (!this.charmerResistance.contains(playerWW1))
                        return;

                    double finalDamages = ev.getFinalDamage();

                    ev.setDamage(ev.getDamage() * 0.90);

                    statsSaved.put(playerWW1, statsSaved.get(playerWW1).add(BigDecimal.valueOf(finalDamages - ev.getFinalDamage())));

                    break;

                default:
                    break;
            }
        });
    }

    @EventHandler
    public void onSelectionEnd(RepartitionEvent ev) {
        Plugin main = Plugin.getINSTANCE();

        Bukkit.getScheduler().runTaskLater(Plugin.getINSTANCE(), () -> {
            main.doToAllPlayersWithRole("werewolf.roles.wolf_dog.display", playerWW -> playerWW.sendMessage(new TextComponent(Plugin.getPrefix() + "§fVous possèdez bien §c§lForce §fla nuit si vous choisissez de vous transformer. (Il y une erreur dans la description)")));

            main.doToAllPlayersWithRole("werewolf.roles.mischievous_werewolf.display", playerWW -> playerWW.sendMessage(new TextComponent(Plugin.getPrefix() + "§fVous ne possèdez pas §c§lForce§f la nuit. (Il y une erreur dans la description)")));

            main.doToAllPlayersWithRole("werewolf.roles.barbarian.display", playerWW -> {
                playerWW.sendMessage(new TextComponent(Plugin.getPrefix() + "§fEffets bonus : Vous régénérez §d§l20% §fdes dégâts que vous infligez. Vous possèdez un §ceffet de force§f en fonction de votre barre de vie §c(5% -> 35%)§f."));
            });

            main.doToAllPlayersWithRole("werewolf.roles.imitator.display", this.imitators::add);

            main.getGame().getPlayersWW().forEach(playerWW -> {
                statsSaved.put(playerWW, BigDecimal.valueOf(0.0D));

                if (Plugin.getINSTANCE().hasAttribute(RoleAttribute.NEUTRAL, playerWW.getRole())) {
                    playerWW.sendMessage(new TextComponent(Plugin.getPrefix() + "§fVotre rôle vous permet de faire un niveau de §c§lTranchant§f de plus que les autres dans cette partie !"));
                }
            });
        }, 2L);
    }

    @EventHandler
    public void onActionBarEvent(ActionBarEvent event) {
        WereWolfAPI game = Plugin.getINSTANCE().getGame();

        if (!game.isState(StateGame.GAME))
            return;
        UUID uuid = event.getPlayerUUID();
        IPlayerWW playerWW = game.getPlayerWW(uuid).orElse(null);
        Player player = Bukkit.getPlayer(uuid);

        if (player == null)
            return;
        if (playerWW == null || playerWW.getRole() == null)
            return;
        if (!playerWW.isState(StatePlayer.ALIVE))
            return;

        if (playerWW.getRole().getKey().equals("werewolf.roles.barbarian.display")) {
            Plugin.addActionBar(event, "§cForce : §l" + new DecimalFormat("#").format(calculateStrengthByHealth(playerWW) * 100.0 - 100.0) + "%");
        }
    }
    @EventHandler
    public void onLoverDurationEnd(RevealLoversEvent event) {
        event.getLovers().forEach(iLover -> iLover.getLovers().stream().filter(iPlayerWW -> iPlayerWW.getRole().isKey("werewolf.roles.charmer.display")).forEach(iPlayerWW -> {
            Player p = Bukkit.getPlayer(iPlayerWW.getUUID());
            p.sendMessage(Plugin.getPrefix() + "§dVous pouvez utiliser la commande §e/ww charmstop §dpour révéler votre identité à votre charmé. (Vous ne pourrez plus vous faire de dons de vie et vous n'aurez plus la flèche permettant de vous traquer).");
            p.sendMessage(Plugin.getPrefix() + "§dVous pouvez également utiliser la commande §e/ww item §dpour obtenir les objets d'un autre rôle de la partie que vous pourrez choisir. Ces objets seront cependant inutilisables.");
        }));
    }

    @EventHandler
    public void onEndSendStats(WinEvent ev) {
        Plugin main = Plugin.getINSTANCE();

        main.doToAllPlayersWithRole("werewolf.roles.barbarian.display", playerWW -> Bukkit.broadcastMessage(Plugin.getPrefix() + "§d§lSoins du §c§lBarbare §c" + playerWW.getName() + " §d: §l" + statsSaved.get(playerWW).setScale(0, RoundingMode.HALF_UP).intValue() + " HP"));

        main.doToAllPlayersWithRole("werewolf.roles.imitator.display", playerWW -> Bukkit.broadcastMessage(Plugin.getPrefix() + "§c§lDégâts de l'§7§lImitateur §7" + playerWW.getName() + "§c: §l" + statsSaved.get(playerWW).setScale(0, RoundingMode.HALF_UP).intValue() + " HP"));

        main.doToAllPlayersWithRole("werewolf.roles.rival.display", playerWW -> Bukkit.broadcastMessage(Plugin.getPrefix() + "§c§lDégâts du §d§lRival §d" + playerWW.getName() + "§c: §l" + statsSaved.get(playerWW).setScale(0, RoundingMode.HALF_UP).intValue() + " HP"));

        main.doToAllPlayersWithRole("werewolf.roles.charmer.display", playerWW -> Bukkit.broadcastMessage(Plugin.getPrefix() + "§7§lRésistance de la §d§lCharmeuse §d" + playerWW.getName() + "§c: §l" + statsSaved.get(playerWW).setScale(0, RoundingMode.HALF_UP).intValue() + " HP"));
    }

    @EventHandler
    public void onStealImitator(StealEvent ev) {
        IPlayerWW player = ev.getPlayerWW();

        if (this.imitators.contains(player))
            this.imitatorBuff.put(player, ev.getPlayerWW().getRole().getCamp());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBarbarianTargetUpdateSuffix(UpdatePlayerNameTagEvent event) {
        if (!Plugin.getINSTANCE().isLoaded())
            return;

        WereWolfAPI game = Plugin.getINSTANCE().getGame();
        IPlayerWW playerWW = game.getPlayerWW(event.getPlayerUUID()).orElse(null);
        if (playerWW == null)
            return;
        if (!playerWW.isState(StatePlayer.DEATH))
            return;

        Plugin.getINSTANCE().doToAllPlayersWithRole("werewolf.roles.barbarian.display", barbarianWW -> {
            if (((IAffectedPlayers)barbarianWW.getRole()).getAffectedPlayers().contains(playerWW))
                event.setSuffix("");
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCharmerEffects(CharmerGetEffectDeathEvent ev) {
        ev.setCancelled(true);

        IPlayerWW playerWW = ev.getPlayerWW();
        Player player = Bukkit.getPlayer(playerWW.getUUID());

        playerWW.addPotionModifier(PotionModifier.add(PotionEffectType.SPEED, playerWW.getRole().getKey()));
        this.charmerResistance.add(playerWW);

        if (player == null)
            return;

        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 3600, 1, false, false));

        player.sendMessage(Plugin.getPrefix() + "§fTu as désormais plusieurs effets : §bRapidité 1§f, §eAborption 2 pendant 3 minutes§f et §7Résistance 10% jusqu'à la fin de la partie§f.");
    }

    @EventHandler(ignoreCancelled = true)
    public void onAngelTarget(AngelTargetEvent ev){

        ev.getTargetWW().sendMessage(new TextComponent(Plugin.getPrefixWithColor(ChatColor.RED) + "§cVous avez désigné pour être la cible de l'§f§lAnge§c."));

        Player target = Bukkit.getPlayer(ev.getTargetWW().getUUID());

        if (target != null)
            Sound.ENDERDRAGON_WINGS.play(target, 5f, 1.2f);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFalsifierDisplayRole(NewDisplayRole ev) {
        IPlayerWW target = Utils.autoSelect(Plugin.getINSTANCE().getGame(), ev.getPlayerWW());
        int max = 30;

        if (Plugin.getINSTANCE().getGame().getRandom().nextDouble() < 0.10D) {

            while (target.getRole().isCamp(Camp.VILLAGER) && max > 0) {
                target = Utils.autoSelect(Plugin.getINSTANCE().getGame(), ev.getPlayerWW());
                max--;
            }

        } else {
            while (!target.getRole().isCamp(Camp.VILLAGER) && max > 0) {
                target = Utils.autoSelect(Plugin.getINSTANCE().getGame(), ev.getPlayerWW());
                max--;
            }
        }

        try {
            Reflection.setValue(ev, "newDisplayRole", target.getRole().getKey());
            Reflection.setValue(ev, "newDisplayCamp", target.getRole().getCamp().getKey());
            System.out.println("[Reflection] set Falsifier Role " + ev.getPlayerWW().getName() + " to : " + Plugin.getRoleTranslated(target.getRole().getKey()) + " (" + Plugin.getRoleTranslated(target.getRole().getCamp().getKey()) + ")");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPriestessPower(PriestessEvent ev) {
        if (!ev.getCamp().equals(Camp.VILLAGER.getKey()))
            ev.getPlayerWW().addPlayerMaxHealth(4.0D);
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

        if (Bukkit.getPlayer(damagerWW.getUUID()).hasPotionEffect(PotionEffectType.INCREASE_DAMAGE))
            return 1.0D;

        return minDamageBonusPercentage +
                (maxDamageBonusPercentage - minDamageBonusPercentage) * healthPercentage;
    }
}
