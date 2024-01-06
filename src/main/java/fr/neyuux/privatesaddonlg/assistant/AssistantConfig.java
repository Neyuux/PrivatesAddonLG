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
            list.add(utils.createClickableText(" §0§l■ §fMettre la limite de protection en §bDiamant §fà §a§l2", "/assistant clickablemessage setprotectiondiamond 2", ClickEvent.Action.RUN_COMMAND, "§fCliquez ici pour mettre la limite de Protection en §bDiamant §aà §a§l2"));

        return list;
    }

    public List<TextComponent> getSummary() {
        List<TextComponent> list = new ArrayList<>();

        return list;
    }



    private static boolean isOutdatedConfig(String key) {
        return key.equals("werewolf.configurations.red_name_tag.name") ||
                key.equals("werewolf.configurations.lone_wolf.name");
    }


    private static boolean isImportantNeyuuxConfig(String key) {
        return key.equals("privatesaddon.configurations.enhanced_diamond_limit.name") ||
                key.equals("privatesaddon.configurations.old_votes.name");
    }
}
