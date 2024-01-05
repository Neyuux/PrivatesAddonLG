package fr.neyuux.privatesaddonlg.assistant;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.annotations.Role;
import fr.ph1lou.werewolfapi.enums.Category;
import fr.ph1lou.werewolfapi.enums.RoleAttribute;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.role.interfaces.IRole;
import fr.ph1lou.werewolfapi.utils.Wrapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class AssistantCompo {

    private final Plugin main;
    private final CommandAssistant assistant;

    private final HashMap<String, Integer> informationsPoints = new HashMap<>();

    public AssistantCompo(Plugin main, CommandAssistant assistant) {
        this.main = main;
        this.assistant = assistant;

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


    public int checkBaseWerewolves() {
        if (!main.isLoaded())
            return 0;

        WereWolfAPI game = main.getGame();

        int count = this.assistant.getPlayerCount();
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

    public int checkPotentialWerewolves() {
        if (!main.isLoaded())
            return 0;

        WereWolfAPI game = main.getGame();

        int count = this.assistant.getPlayerCount();
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


    public int checkInformationRoles() {
        if (!main.isLoaded())
            return 0;

        WereWolfAPI game = main.getGame();

        int count = this.assistant.getPlayerCount();
        int recommandedIP = Math.round(count * 1.25f + 0.8f * -this.checkBaseWerewolves());
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

    public int checkSoloRoles() {
        if (!main.isLoaded())
            return 0;

        int count = this.assistant.getPlayerCount();
        int recommandedSolos = 0;

        for (int i = count; i > 9; i -= 9)
            recommandedSolos++;

        return recommandedSolos - this.getSolosCount();
    }

    public int checkNeutralRoles() {
        if (!main.isLoaded())
            return 0;

        int count = this.assistant.getPlayerCount();
        int currentSolos = this.getSolosCount();
        int currentNeutral = (int) main.getRegisterManager().getRolesRegister()
                .stream()
                .filter(roleRegister -> AssistantCompo.isNeutral(roleRegister.getMetaDatas().key()))
                .flatMapToInt(roleRegisterx -> IntStream.range(0, main.getGame().getConfig().getRoleCount(roleRegisterx.getMetaDatas().key())))
                .count();

        return 0;
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

    private int getSolosCount() {
        return (int) main.getRegisterManager().getRolesRegister()
                .stream()
                .filter(roleRegister -> Arrays.stream(roleRegister.getMetaDatas().attributes()).anyMatch(attribute -> attribute == RoleAttribute.NEUTRAL) && !roleRegister.getMetaDatas().key().contains("white_werewolf"))
                .flatMapToInt(roleRegisterx -> IntStream.range(0, main.getGame().getConfig().getRoleCount(roleRegisterx.getMetaDatas().key())))
                .count();
    }
}
