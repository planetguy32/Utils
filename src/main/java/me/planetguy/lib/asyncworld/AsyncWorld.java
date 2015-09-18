package me.planetguy.lib.asyncworld;

import net.minecraft.block.Block;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.init.Blocks;
import net.minecraft.util.ReportedException;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;

public class AsyncWorld {
	
	/*
	 * Returns the block in the world at the specified position, or null if the block is unavailable or not generated.
	 */
	public Block getBlock(World w, int x, int y, int z) {
        if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000 && y >= 0 && y < 256) //bounds check
        {
            Chunk chunk = null;

            try
            {
            	ChunkProviderServer prov=(ChunkProviderServer)w.getChunkProvider();
            	if(prov != null) {
            		//hashmap get -> pure
            		chunk=(Chunk)prov.loadedChunkHashMap.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(x, z));
            		if(chunk != null) {
            			return chunk.getBlock(x & 15, y, z & 15);
            		}
            	}
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception getting block type in world");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Requested block coordinates");
                crashreportcategory.addCrashSection("Found chunk", Boolean.valueOf(chunk == null));
                crashreportcategory.addCrashSection("Location", CrashReportCategory.getLocationInfo(x, y, z));
                throw new ReportedException(crashreport);
            }
        }
        // default, if chunk is not immediately available - no loading it because that's not pure functional
        // TODO synchronize on the world and load the area?
        return null;
	}

}
