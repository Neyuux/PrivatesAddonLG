package fr.neyuux.privatesaddonlg;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldInitEvent;

import java.text.DecimalFormat;

public class WorldChangesListener implements Listener {

    private int chunksLoaded = 0;

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


    private void addChunk() {
        this.chunksLoaded++;

        double percentage = this.chunksLoaded / 900.0D * 100.0D;

        Bukkit.getLogger().info("[LG UHC > PrivatesAddon] Add Roofed : " + this.chunksLoaded + " / 900 terminé.  (~" + (new DecimalFormat("0.0")).format(percentage) + "%)");
        if (this.chunksLoaded == 900)
            Bukkit.broadcastMessage(Plugin.getPrefix() + "§2La génération de la Roofed Forest est terminée !");
    }
}
