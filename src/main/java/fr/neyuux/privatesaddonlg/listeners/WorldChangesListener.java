package fr.neyuux.privatesaddonlg.listeners;

import fr.neyuux.privatesaddonlg.Plugin;
import lombok.Getter;
import lombok.Setter;
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

    public WorldChangesListener() {
        Plugin.getINSTANCE().addServiceLoadTask(game -> game.getMapManager().loadMap());
    }

    private int chunksLoaded = 0;

    @Getter
    @Setter
    private RoofedSize size = RoofedSize.MEDIUM;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInitWerewolfWorld(WorldInitEvent ev) {
        World w = ev.getWorld();
        if (w.getName().equals("werewolf_map")) {
            this.chunksLoaded = 0;
            w.loadChunk(-125, -125);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void chunkLoadReplaceRoofedForest(ChunkLoadEvent event) {

        final World w = event.getWorld();

        if (event.isNewChunk() && w.getName().equals("werewolf_map")){

            int maximum = this.size.getPlusChunkCoord();
            int minimum = this.size.getMinusChunkCoord();
            final Chunk c = event.getChunk();
            int X = c.getX();
            int Z = c.getZ();
            if (X <= maximum && X >= minimum && Z <= maximum && Z >= minimum ){

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

        double percentage = this.chunksLoaded / (double)this.getSize().getChunks() * 100.0D;

        Bukkit.getLogger().info("[LG UHC > PrivatesAddon] Add Roofed : " + this.chunksLoaded + " / "+this.getSize().getChunks()+" terminé.  (~" + (new DecimalFormat("0.0")).format(percentage) + "%)");
        if (this.chunksLoaded == this.getSize().getChunks())
            Bukkit.broadcastMessage(Plugin.getPrefix() + "§2La génération de la Roofed Forest est terminée !");
    }


    public enum RoofedSize {
        LARGE(15),
        MEDIUM(11),
        SMALL(7);

        @Getter
        private final int size;

        @Getter
        private final int chunks;

        RoofedSize(int size) {
            this.size = size;

            int i = 0;
            int maximum = -125 + size;
            int minimum = -125 - size;

            for (int x = -400; x <= 400; x++) {
                for (int z = -400; z <= 400; z++) {
                    if (x <= maximum && x >= minimum && z <= maximum && z >= minimum) {
                        i++;
                    }
                }
            }

            this.chunks = i;
        }

        public int getPlusChunkCoord() {
            return -125 + size;
        }

        public int getMinusChunkCoord() {
            return -125 - size;
        }
    }
}
