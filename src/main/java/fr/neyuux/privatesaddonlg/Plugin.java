package fr.neyuux.privatesaddonlg;

import fr.ph1lou.werewolfapi.GetWereWolfAPI;
import fr.ph1lou.werewolfapi.annotations.Author;
import fr.ph1lou.werewolfapi.annotations.ModuleWerewolf;
import fr.ph1lou.werewolfapi.enums.UniversalMaterial;
import fr.ph1lou.werewolfapi.events.game.game_cycle.StartEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.utils.Formatter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DecimalFormat;


@ModuleWerewolf(key = "privatesaddon.name",
        loreKeys = {"privatesaddon.description"},
        item = UniversalMaterial.COBBLESTONE,
        defaultLanguage = "fr_FR",
        authors = @Author(uuid = "0234db8c-e6e5-45e5-8709-ea079fa575bb", name = "Neyuux_"))
public class Plugin extends JavaPlugin implements Listener, CommandExecutor {

    private static final String PREFIX = "§bPrivatesAddon §8» §r";

    private int chunksLoaded = 0;

    private WereWolfAPI currentGame;


    public static String getPrefix() {
        return PREFIX;
    }

    @Override
    public void onEnable() {

        this.getServer().getPluginManager().registerEvents(this, this);

        this.getCommand("khqbib").setExecutor(this);

        GetWereWolfAPI ww = getServer().getServicesManager().load(GetWereWolfAPI.class);

        this.currentGame = ww.getWereWolfAPI();

        super.onEnable();
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onInitWerewolfWorld(WorldInitEvent ev) {
        World w = ev.getWorld();
        if (w.getName().equals("werewolf_map")) {
            w.loadChunk(-125, -125);
            this.chunksLoaded = 0;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void chunkLoadReplaceRoofedForest(ChunkLoadEvent event) {

        final World w = event.getWorld();

        if (event.isNewChunk() && w.getName().equals("werewolf_map")){
            
            final Chunk c = event.getChunk();
            int X = c.getX();
            int Z = c.getZ();
            if (X <= -110 && X >= -140 && Z <= -110 && Z >= -140 ){

                this.addChunk();

                for (int x=0; x<16; x++){
                    for (int z=0; z<16; z++){
                        final Block block = c.getBlock(x, w.getHighestBlockYAt(x,z) , z);
                        block.setBiome(Biome.ROOFED_FOREST);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onGameStart(StartEvent ev) {
        this.currentGame.setGameName("@Neyuux_");
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        sender.sendMessage(getPrefix() + "§a§lFuck Khqbib");
        return true;
    }

    private void addChunk() {
        this.chunksLoaded++;

        double percentage = this.chunksLoaded / 900.0D * 100.0D;

        Bukkit.getLogger().info("[LG UHC > PrivatesAddon] Add Roofed : " + this.chunksLoaded + " / 900 terminé.  (~" + (new DecimalFormat("0.0")).format(percentage) + "%)");
        if (this.chunksLoaded == 900)
            Bukkit.broadcastMessage(getPrefix() + "§2La génération de la Roofed Forest est terminée !");
    }
}
