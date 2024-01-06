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

public class AssistantScenarios {

    private final Plugin main;

    public AssistantScenarios(Plugin main) {
        this.main = main;
    }


    public List<String> checkToDesactivate() {
        if (!main.isLoaded())
            return Collections.emptyList();

        return main.getRegisterManager().getScenariosRegister()
                .stream()
                .map(scenarioRegister -> scenarioRegister.getMetaDatas().key())
                .filter(key -> AssistantScenarios.isToDesactivate(key) && main.getGame().getConfig().isScenarioActive(key))
                .collect(Collectors.toList());
    }

    public List<String> checkToActivate() {
        if (!main.isLoaded())
            return Collections.emptyList();

        return main.getRegisterManager().getScenariosRegister()
                .stream()
                .map(scenarioRegister -> scenarioRegister.getMetaDatas().key())
                .filter(key -> AssistantScenarios.isToActivate(key) && !main.getGame().getConfig().isScenarioActive(key))
                .collect(Collectors.toList());
    }

    public List<TextComponent> checkScenariosConfigs() {
        if (!main.isLoaded())
            return Collections.emptyList();

        List<TextComponent> list = new ArrayList<>();
        IConfiguration config = main.getGame().getConfig();
        VersionUtils utils = VersionUtils.getVersionUtils();

        main.getRegisterManager().getScenariosRegister()
                .forEach(scenariosRegister -> {
                    String key = scenariosRegister.getMetaDatas().key();

                    if (config.isScenarioActive(key)) {

                        if (key.equals("werewolf.scenarios.xp_boost.name") && config.getValue("werewolf.scenarios.xp_boost.configurations.xp") != 200) {
                            list.add(utils.createClickableText(" §0§l■ §fMettre le boost d'XP à §a§l200%", "/assistant clickablemessage set werewolf.scenarios.xp_boost.configurations.xp 200", ClickEvent.Action.RUN_COMMAND, "§fCliquez ici pour mettre le boost d'XP à §a§l200%"));
                        }

                        if (key.equals("werewolf.scenarios.vanilla+.name")) {

                            if (config.getValue("werewolf.scenarios.vanilla+.configurations.flint_rate") < 50) {
                                list.add(utils.createClickableText(" §0§l■ §fAugmenter le taux de drop du Silex", "/assistant clickablemessage set werewolf.scenarios.vanilla+.configurations.flint_rate 50", ClickEvent.Action.RUN_COMMAND, "§fCliquez ici pour mettre le taux de drop du silex à §a§l50%"));

                            } else if (config.getValue("werewolf.scenarios.vanilla+.configurations.ender_pearl_rate") != 10) {
                                list.add(utils.createClickableText(" §0§l■ §fMettre le taux de drop des Ender Pearls à §a§l10%", "/assistant clickablemessage set werewolf.scenarios.vanilla+.configurations.ender_pearl_rate 10", ClickEvent.Action.RUN_COMMAND, "§fCliquez ici pour mettre le taux de drop des Ender Pearls à §a§l10%"));

                            } else if (config.getValue("werewolf.scenarios.vanilla+.configurations.apple_rate") < 8) {
                                list.add(utils.createClickableText(" §0§l■ §fAugmenter le taux de drop des Pommes", "/assistant clickablemessage set werewolf.scenarios.vanilla+.configurations.apple_rate 8", ClickEvent.Action.RUN_COMMAND, "§fCliquez ici pour mettre le taux de drop des pommes à §a§l8%"));

                            } else if (config.getValue("werewolf.scenarios.vanilla+.configurations.apple_rate") > 13) {
                                list.add(utils.createClickableText(" §0§l■ §fDiminuer le taux de drop des Pommes", "/assistant clickablemessage set werewolf.scenarios.vanilla+.configurations.apple_rate 10", ClickEvent.Action.RUN_COMMAND, "§fCliquez ici pour mettre le taux de drop des pommes à §a§l10%"));
                            }
                        }
                    }
                });

        return list;
    }


    public List<TextComponent> getSummary() {
        VersionUtils utils = VersionUtils.getVersionUtils();
        List<TextComponent> list = new ArrayList<>();

        WereWolfAPI game = main.getGame();

        this.checkToDesactivate()
                .forEach(key -> list.add(utils.createClickableText(" §0§l■ §fDésactiver le Scénario \"§b" + game.translate(key) + "§f\"", "/assistant clickablemessage desactivatescenario " + key, ClickEvent.Action.RUN_COMMAND, "§fCliquez ici pour §cdésactiver §fle Scénario \"§b" + game.translate(key) + "§f\"")));

        this.checkToActivate()
                .forEach(key -> list.add(utils.createClickableText(" §0§l■ §fActiver le Scénario \"§b" + game.translate(key) + "§f\"", "/assistant clickablemessage activatescenario " + key, ClickEvent.Action.RUN_COMMAND, "§fCliquez ici pour §aactiver §fle Scénario \"§b" + game.translate(key) + "§f\"")));

        list.addAll(this.checkScenariosConfigs());

        return list;
    }


    private static boolean isToDesactivate(String key) {
        return key.equals("werewolf.scenarios.diamond_limit.name") ||
                key.equals("werewolf.scenarios.no_clean_up.name") ||
                key.equals("werewolf.scenarios.no_fall.name") ||
                key.equals("werewolf.scenarios.slow_bow.name");
    }

    private static boolean isToActivate(String key) {
        return key.equals("werewolf.scenarios.beta_zombies.name") ||
                key.equals("werewolf.scenarios.cat_eyes.name") ||
                key.equals("werewolf.scenarios.cut_clean.name") ||
                key.equals("werewolf.scenarios.horse_less.name") ||
                key.equals("werewolf.scenarios.compass_middle.name") ||
                key.equals("werewolf.scenarios.no_extra_stones.name") ||
                key.equals("werewolf.scenarios.no_end.name") ||
                key.equals("werewolf.scenarios.no_fire_weapons.name") ||
                key.equals("werewolf.scenarios.no_nether.name") ||
                key.equals("werewolf.scenarios.no_poison.name") ||
                key.equals("werewolf.scenarios.no_egg_snowball.name") ||
                key.equals("werewolf.scenarios.rod_less.name") ||
                key.equals("werewolf.scenarios.safe_miner.name") ||
                key.equals("werewolf.scenarios.timber.name") ||
                key.equals("werewolf.scenarios.vanilla+.name") ||
                key.equals("werewolf.scenarios.xp_boost.name");
    }
}
