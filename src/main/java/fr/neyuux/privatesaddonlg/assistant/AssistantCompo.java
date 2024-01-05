package fr.neyuux.privatesaddonlg.assistant;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.annotations.Lover;
import fr.ph1lou.werewolfapi.annotations.Role;
import fr.ph1lou.werewolfapi.enums.Category;
import fr.ph1lou.werewolfapi.enums.RoleAttribute;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.lovers.ILover;
import fr.ph1lou.werewolfapi.role.interfaces.IRole;
import fr.ph1lou.werewolfapi.utils.Wrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class AssistantCompo {

    private final Plugin main;

    private final HashMap<String, Integer> informationsPoints = new HashMap<>();

    public AssistantCompo(Plugin main) {
        this.main = main;

        informationsPoints.put("analyst", 1);
        informationsPoints.put("seer", 9);
        informationsPoints.put("chatty_seer", 8);
        informationsPoints.put("bear_trainer", 7);
        informationsPoints.put("fox", 7);
        informationsPoints.put("detective", 7);
        informationsPoints.put("citizen", 3);
        informationsPoints.put("priestess", 7);
        informationsPoints.put("shaman", 1);
        informationsPoints.put("oracle", 5);
        informationsPoints.put("twin", 7);
        informationsPoints.put("druid", 4);
        informationsPoints.put("fruit_merchant", 5);
        informationsPoints.put("wise_elder", 5);
        informationsPoints.put("occultist", 4);
        informationsPoints.put("gravedigger", 15);
        informationsPoints.put("story_teller", 6);
        informationsPoints.put("spy", 3);
        informationsPoints.put("interpreter", 4);
        informationsPoints.put("innkeeper", 4);
        informationsPoints.put("croupier", 5);
        informationsPoints.put("librarian", 2);
        informationsPoints.put("sister", 1);
        informationsPoints.put("little_girl", 3);
        informationsPoints.put("witness", 2);
    }


    public int checkBaseWerewolves(int count) {
        if (!main.isLoaded())
            return 0;

        WereWolfAPI game = main.getGame();

        AtomicInteger whiteWerewolf = new AtomicInteger(0);
        int currentWW = (int) main.getRegisterManager().getRolesRegister()
                .stream()
                .filter(roleRegister -> {
                    if (roleRegister.getMetaDatas().key().equals("werewolf.roles.white_werewolf.display")) {
                        whiteWerewolf.addAndGet(game.getConfig().getRoleCount("werewolf.roles.white_werewolf.display"));
                        return true;
                    }
                    return roleRegister.getMetaDatas().category() == Category.WEREWOLF;
                })
                .flatMapToInt(roleRegisterx -> IntStream.range(0, game.getConfig().getRoleCount(roleRegisterx.getMetaDatas().key())))
                .count();

        if (game.getConfig().isConfigActive("privatesaddon.configurations.lone_wolf.name") || game.getConfig().isConfigActive("werewolf.configurations.lone_wolf.name"))
            whiteWerewolf.incrementAndGet();

        count += whiteWerewolf.get();
        float ww = count / 3f;
        double floorWW = Math.floor(ww);


        if (floorWW > currentWW) {
            if (floorWW - currentWW != 1) {
                return (int) floorWW - currentWW;

            } else {
                if (ww - floorWW > 0.5)
                    return 1;
                else
                    return 0;
            }

        } else if (currentWW > floorWW) {
            if (currentWW - floorWW != 1) {
                return (int) floorWW - currentWW;

            } else {
                if (ww - floorWW < 0.5)
                    return -1;
                else
                    return 0;
            }
        } else {
            return 0;
        }
    }

    public int checkPotentialWerewolves(int count) {
        if (!main.isLoaded())
            return 0;

        WereWolfAPI game = main.getGame();

        AtomicInteger whiteWerewolf = new AtomicInteger(0);
        AtomicInteger maxWW = new AtomicInteger(0);
        AtomicBoolean romulusRemus = new AtomicBoolean(false);
        int currentWW = (int) main.getRegisterManager().getRolesRegister()
                .stream()
                .filter(roleRegister -> {
                    String key = roleRegister.getMetaDatas().key();
                    if (key.contains("white_werewolf")) {
                        whiteWerewolf.addAndGet(game.getConfig().getRoleCount(key));
                        return true;

                    }

                    if (Arrays.stream(roleRegister.getMetaDatas().attributes()).anyMatch(roleAttribute -> roleAttribute == RoleAttribute.HYBRID)) {
                        if (AssistantCompo.isPotentialWerewolf(roleRegister.getMetaDatas().key())) {
                            maxWW.incrementAndGet();
                            return false;
                        }
                    }

                    if (key.contains("romulus_remus"))
                        romulusRemus.set(true);

                    if (roleRegister.getMetaDatas().weight() == 1.5F)
                        maxWW.incrementAndGet();

                    return roleRegister.getMetaDatas().category() == Category.WEREWOLF;
                })
                .flatMapToInt(roleRegisterx -> IntStream.range(0, game.getConfig().getRoleCount(roleRegisterx.getMetaDatas().key())))
                .count();

        if (game.getConfig().isConfigActive("privatesaddon.configurations.lone_wolf.name") || game.getConfig().isConfigActive("werewolf.configurations.lone_wolf.name"))
            whiteWerewolf.incrementAndGet();

        if (romulusRemus.get())
            maxWW.decrementAndGet();

        maxWW.addAndGet(currentWW);
        count += whiteWerewolf.get();
        float ww = count / 3f + count / 15f;
        double floorWW = Math.round(ww);

        if (floorWW > maxWW.get()) {
            if (floorWW - maxWW.get() != 1 || count > 18) {
                return (int) floorWW - maxWW.get();

            } else {
                if (ww - floorWW > 0.5)
                    return 1;
                else
                    return 0;
            }

        } else if (maxWW.get() > floorWW) {
            if (maxWW.get() - floorWW != 1 || count > 18) {
                return (int) floorWW - maxWW.get();

            } else {
                if (ww - floorWW < 0.5)
                    return -1;
                else
                    return 0;
            }
        } else {
            return 0;
        }
    }


    public int checkInformationRoles(int count) {
        if (!main.isLoaded())
            return 0;

        WereWolfAPI game = main.getGame();

        int recommandedIP = Math.round(count * 1.25f + 0.8f * -this.checkBaseWerewolves(count));
        int currentIP = 0;

        for (Wrapper<IRole, Role> roleRegister : main.getRegisterManager().getRolesRegister())
            for (String s : this.informationsPoints.keySet())
                if (roleRegister.getMetaDatas().key().contains(s))
                    if (roleRegister.getMetaDatas().requireDouble())
                        currentIP = informationsPoints.get(s);
                    else
                        currentIP += informationsPoints.get(s) * game.getConfig().getRoleCount(roleRegister.getMetaDatas().key());

        //TODO Points GUI
        return recommandedIP - currentIP;
    }

    public int checkSoloRoles(int count) {
        if (!main.isLoaded())
            return 0;

        int recommandedSolos = 0;

        for (int i = count; i > 9; i -= 9)
            recommandedSolos++;

        return recommandedSolos - this.getSolosCount();
    }

    public int checkNeutralRoles(int count) {
        if (!main.isLoaded())
            return 0;

        int currentSolos = this.getSolosCount();
        int currentNeutral = (int) main.getRegisterManager().getRolesRegister()
                .stream()
                .filter(roleRegister -> AssistantCompo.isNeutral(roleRegister.getMetaDatas().key()))
                .flatMapToInt(roleRegisterx -> IntStream.range(0, main.getGame().getConfig().getRoleCount(roleRegisterx.getMetaDatas().key())))
                .count();

        int recommandedNeutral = Math.round(count / 16.5f - (currentSolos / (count < 18 ? 2f : 2.30f)));

        return recommandedNeutral - currentNeutral;
    }

    public boolean checkDoubleCouples() {
        if (!main.isLoaded())
            return false;

        WereWolfAPI game = main.getGame();

        if (game.getConfig().getRoleCount("werewolf.roles.cupid.display") > 0) {
            for (Wrapper<ILover, Lover> loverRegister : main.getRegisterManager().getLoversRegister()) {
                if (game.getConfig().getLoverCount(loverRegister.getMetaDatas().key()) > 0)
                    return true;
            }
        }
        return false;
    }

    public boolean checkCouple() {
        if (!main.isLoaded())
            return false;

        WereWolfAPI game = main.getGame();

        if (game.getConfig().getRoleCount("werewolf.roles.cupid.display") > 0 || game.getConfig().getRoleCount("werewolf.roles.stud.display") > 0)
            return false;

        for (Wrapper<ILover, Lover> loverRegister : main.getRegisterManager().getLoversRegister())
            if (game.getConfig().getLoverCount(loverRegister.getMetaDatas().key()) > 0)
                return false;

        return true;
    }

    public int checkResurrectionRoles(int count) {
        if (!main.isLoaded())
            return 0;

        int recommandedRez;

        if (count <= 19)
            recommandedRez = 1;
        else if (count <= 24)
            recommandedRez = 2;
        else
            recommandedRez = Math.round(count / 10f);

        int currentRez = (int) main.getRegisterManager().getRolesRegister()
                .stream()
                .filter(roleRegister -> AssistantCompo.isRessurectionRole(roleRegister.getMetaDatas().key()))
                .flatMapToInt(roleRegisterx -> IntStream.range(0, main.getGame().getConfig().getRoleCount(roleRegisterx.getMetaDatas().key())))
                .count();

        if (recommandedRez > currentRez)
            return 0;

        return recommandedRez - currentRez;
    }


    public List<String> getSummary(int count) {
        List<String> list = new ArrayList<>();

        int baseLG = this.checkBaseWerewolves(count);

        if (baseLG != 0)
            list.add(" §0§l■ §f" + getRemoveOrAdd(baseLG) + " §c§l" + baseLG + " §cLG");

        int potentialLG = this.checkPotentialWerewolves(count);

        if (potentialLG != 0)
            list.add(" §0§l■ §f" + getRemoveOrAdd(potentialLG) + " §c§l" + potentialLG + " §cLG Potentiel");

        int info = this.checkInformationRoles(count);

        if (info != 0)
            list.add(" §0§l■ §f" + getRemoveOrAdd(info) + " §d§l" + info + " §dRôles à infos");

        int solo = this.checkSoloRoles(count);

        if (solo != 0)
            list.add(" §0§l■ §f" + getRemoveOrAdd(solo) + " §6§l" + solo + " §6Rôles Solitaires");

        int neutral = this.checkNeutralRoles(count);

        if (neutral != 0)
            list.add(" §0§l■ §f" + getRemoveOrAdd(neutral) + " §e§l" + solo + " §eRôles Neutres");

        int rez = this.checkResurrectionRoles(count);

        if (rez != 0)
            list.add(" §0§l■ §f" + getRemoveOrAdd(rez) + " §e§l" + solo + " §aRôles qui réssucitent");

        if (this.checkCouple())
            list.add(" §0§l■ §fAjouter un §dcouple");

        if (this.checkDoubleCouples())
            list.add(" §0§l■ §cDésactivez §fle Cupidon ou un Couple Aléatoire pour ne pas avoir 2 couples !");

        return list;
    }


    private static boolean isPotentialWerewolf(String key) {
        return key.contains("amnesiac_werewolf") ||
                key.contains("romulus_remus") ||
                key.contains("scammer") ||
                key.contains("wild_child") ||
                key.contains("wolf_dog");
    }

    private static boolean isNeutral(String key) {
        return key.contains("thief") ||
                key.contains("thug") ||
                key.contains("scammer") ||
                key.contains("romulus_remus") ||
                key.contains("auramancer");
    }

    private static boolean isRessurectionRole(String key) {
        return key.contains("elder") ||
                key.contains("witch") ||
                key.contains("guard") ||
                key.contains("stud") ||
                key.contains("servitor");

    }

    private static String getRemoveOrAdd(int i) {
        if (i > 0)
            return "Ajouter";
        else return "Retirer";
    }

    private int getSolosCount() {
        return (int) main.getRegisterManager().getRolesRegister()
                .stream()
                .filter(roleRegister -> Arrays.stream(roleRegister.getMetaDatas().attributes()).anyMatch(attribute -> attribute == RoleAttribute.NEUTRAL) && !roleRegister.getMetaDatas().key().contains("white_werewolf"))
                .flatMapToInt(roleRegisterx -> IntStream.range(0, main.getGame().getConfig().getRoleCount(roleRegisterx.getMetaDatas().key())))
                .count();
    }
}
