package fr.neyuux.privatesaddonlg.assistant;

import fr.neyuux.privatesaddonlg.Plugin;
import fr.ph1lou.werewolfapi.game.IConfiguration;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.versions.VersionUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AssistantConfig {

    private final Plugin main;

    public AssistantConfig(Plugin main) {
        this.main = main;
    }

    public List<String> checkNeyuuxConfigs() {
        if (!main.isLoaded())
            return Collections.emptyList();

        WereWolfAPI game = main.getGame();

        return main.getRegisterManager().getConfigsRegister()
                .stream()
                .filter(configRegister -> AssistantConfig.isImportantNeyuuxConfig(configRegister.getMetaDatas().config().key()) && !game.getConfig().isConfigActive(configRegister.getMetaDatas().config().key()))
                .map(configRegister -> configRegister.getMetaDatas().config().key())
                .collect(Collectors.toList());
    }

    public List<String> checkOudatedConfigs() {
        if (!main.isLoaded())
            return Collections.emptyList();

        WereWolfAPI game = main.getGame();

        return main.getRegisterManager().getConfigsRegister()
                .stream()
                .filter(configRegister -> AssistantConfig.isOutdatedConfig(configRegister.getMetaDatas().config().key()) && game.getConfig().isConfigActive(configRegister.getMetaDatas().config().key()))
                .map(configRegister -> configRegister.getMetaDatas().config().key())
                .collect(Collectors.toList());
    }

    public List<TextComponent> checkEnchants() {
        if (!main.isLoaded())
            return Collections.emptyList();

        List<TextComponent> list = new ArrayList<>();
        IConfiguration config = main.getGame().getConfig();
        VersionUtils utils = VersionUtils.getVersionUtils();

        if (config.getLimitProtectionDiamond() != 2)
            list.add(utils.createClickableText(" §0§l■ §fMettre la limite de protection en §bDiamant §fà §a§l2", "/assistant clickablemessage setprotectiondiamond 2", ClickEvent.Action.RUN_COMMAND, "§fCliquez ici pour mettre la limite de Protection en §bDiamant §fà §a§l2"));

        if (config.getLimitProtectionIron() != 3)
            list.add(utils.createClickableText(" §0§l■ §fMettre la limite de protection en §7Fer §fà §a§l3", "/assistant clickablemessage setprotectioniron 3", ClickEvent.Action.RUN_COMMAND, "§fCliquez ici pour mettre la limite de Protection en §7Fer §fà §a§l3"));

        if (config.getLimitSharpnessDiamond() != 3)
            list.add(utils.createClickableText(" §0§l■ §fMettre la limite de tranchant en §bDiamant §fà §a§l3", "/assistant clickablemessage setsharpnessdiamond 3", ClickEvent.Action.RUN_COMMAND, "§fCliquez ici pour mettre la limite de Tranchant en §dDiamant §fà §a§l3"));

        if (config.getLimitSharpnessIron() != 3)
            list.add(utils.createClickableText(" §0§l■ §fMettre la limite de tranchant en §7Fer §fà §a§l3", "/assistant clickablemessage setsharpnessiron 3", ClickEvent.Action.RUN_COMMAND, "§fCliquez ici pour mettre la limite de Tranchant en §7Fer §fà §a§l3"));

        if (config.getLimitPowerBow() != 3)
            list.add(utils.createClickableText(" §0§l■ §fMettre la limite de puissance à §a§l3", "/assistant clickablemessage setpower 3", ClickEvent.Action.RUN_COMMAND, "§fCliquez ici pour mettre la limite de Puissance à §a§l3"));

        return list;
    }

    public List<TextComponent> checkRolesConfigs() {
        if (!main.isLoaded())
            return Collections.emptyList();

        List<TextComponent> list = new ArrayList<>();
        IConfiguration config = main.getGame().getConfig();
        VersionUtils utils = VersionUtils.getVersionUtils();

        main.getRegisterManager().getRolesRegister()
                .forEach(roleRegister -> {
                    String key = roleRegister.getMetaDatas().key();
                    String addon = key.split("\\.")[0];

                    if (config.getRoleCount(key) > 0) {

                        if (key.contains("cupid")) {
                            if (!config.isConfigActive(addon + ".roles.cupid.configurations.random_cupid"))
                                list.add(utils.createClickableText(" §0§l■ §fActiver le §dCouple Aléatoire §f§o(avec Cupidon)", "/assistant clickablemessage setrandomcouplecupid " + addon, ClickEvent.Action.RUN_COMMAND, "§fCliquez ici pour §aactiver §fle §dCouple Aléatoire §fdu Cupidon"));

                        } else if (key.contains("witch")) {
                            if (config.isConfigActive(addon + ".roles.witch.configurations.auto_rez_witch"))
                                list.add(utils.createClickableText(" §0§l■ §fDésactiver l'§dAuto-Résurrection §fde la Sorcière", "/assistant clickablemessage removeautorezwitch " + addon, ClickEvent.Action.RUN_COMMAND, "§fCliquez ici pour §cdésactiver §fl'§dAuto-Résurrection §fde la Sorcière"));

                        } else if (key.contains("croupier")) {
                            if (config.isConfigActive(addon + ".roles.croupier.configurations.croupier_every_other_day"))
                                list.add(utils.createClickableText(" §0§l■ §fActiver §aCroupier §fun jour sur deux", "/assistant clickablemessage setcroupier " + addon, ClickEvent.Action.RUN_COMMAND, "§fCliquez ici pour §cdésactiver §fl'§dAuto-Résurrection §fde l'IPDL"));
                        }
                    }
                });

        return list;
    }

    public List<TextComponent> getSummary() {
        if (!main.isLoaded())
            return Collections.emptyList();

        WereWolfAPI game = main.getGame();
        List<TextComponent> list = new ArrayList<>();
        VersionUtils utils = VersionUtils.getVersionUtils();


        this.checkNeyuuxConfigs()
                .forEach(key -> list.add(utils.createClickableText(" §0§l■ §fActiver la configuration \"§e" + game.translate(key) + "§f\"", "/assistant clickablemessage activate " + key, ClickEvent.Action.RUN_COMMAND, "§fCliquez ici pour §aactiver la configuration §f\"§e" + game.translate(key) + "§f\"")));

        this.checkOudatedConfigs()
                .forEach(key -> list.add(utils.createClickableText(" §0§l■ §fDésactiver la configuration \"§e" + game.translate(key) + "§f\"", "/assistant clickablemessage desactivate " + key, ClickEvent.Action.RUN_COMMAND, "§fCliquez ici pour §cdésactiver la configuration §f\"§e" + game.translate(key) + "§f\"")));

        list.addAll(this.checkRolesConfigs());
        list.addAll(this.checkEnchants());

        return list;
    }



    private static boolean isOutdatedConfig(String key) {
        return key.equals("werewolf.configurations.lone_wolf.name");
    }


    private static boolean isImportantNeyuuxConfig(String key) {
        return key.equals("privatesaddon.configurations.enhanced_diamond_limit.name") ||
                key.equals("privatesaddon.configurations.old_votes.name");
    }
}
