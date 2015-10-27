package me.planetguy.lib.asyncworld;

import net.minecraft.block.Block;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ReportedException;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;

public class AsyncWorld {
	
	/*
	 * Returns the block in the world at the specified position, or null if the block is unavailable or not generated.
	 * 
	 * REQUIRES THE WORLD TO NOT CHANGE WHILE RUNNING! This can be accomplished by blocking the main thread while async-only tasks
	 * run in the background.
	 * 
	 * Can only be called on a server. TODO client port?
	 */
	public static Block getBlock(World w, int x, int y, int z) {
        if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000 && y >= 0 && y < 256) //bounds check
        {
            Chunk chunk = null;

            try
            {
            	chunk=getChunk(w,x,z);
            	if(chunk != null) {
            		return chunk.getBlock(x & 15, y, z & 15);
            	}
            	Block.getBlockById(0);
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
        return null;
	}
	
    public int getBlockMetadata(World w, int p_72805_1_, int p_72805_2_, int p_72805_3_)
    {
        if (p_72805_1_ >= -30000000 && p_72805_3_ >= -30000000 && p_72805_1_ < 30000000 && p_72805_3_ < 30000000)
        {
            if (p_72805_2_ < 0)
            {
                return 0;
            }
            else if (p_72805_2_ >= 256)
            {
                return 0;
            }
            else
            {
                Chunk chunk = getChunk(w, p_72805_1_, p_72805_3_);
                p_72805_1_ &= 15;
                p_72805_3_ &= 15;
                return chunk.getBlockMetadata(p_72805_1_, p_72805_2_, p_72805_3_);
            }
        }
        else
        {
            return 0;
        }
    }
	
	public static TileEntity getTileEntity(World w, int x, int y, int z) {
		Chunk c=getChunk(w, x, z);
		if(c != null) {
			return (TileEntity) c.chunkTileEntityMap.get(new ChunkPosition(x,y,z));
		} else {
			return null;
		}
	}
	
	public static Chunk getChunk(World w, int x, int z) {
        // default, if chunk is not immediately available - no loading it because that's not pure functional
        // TODO synchronize on the world and load the area?
    	ChunkProviderServer prov=(ChunkProviderServer)w.getChunkProvider();
    	if(prov != null) {
    		//hashmap get is pure
    		Chunk c = (Chunk)prov.loadedChunkHashMap.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(x, z));
    		
    		/* 
    		if(c!=null)
    			synchronized(w) {
    				return prov.provideChunk(x, z);
    			}
    		else
    		*/
    			return c;
    	} else {
    		return null;
    	}
	}
	
	public static void setBlock(World w, int x, int y, int z) {
		Chunk c=getChunk(w, x, z);
		//TODO find my sanity; I think I left it somewhere near here...
		
	}

}
