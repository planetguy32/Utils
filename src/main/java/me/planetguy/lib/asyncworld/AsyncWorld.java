package me.planetguy.lib.asyncworld;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import net.minecraft.block.Block;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.ForgeDirection;

public class AsyncWorld {

	/*
	 * Returns the block in the world at the specified position, or null if the block is unavailable or not generated.
	 * 
	 * REQUIRES THE WORLD TO NOT CHANGE WHILE RUNNING! This can be accomplished by blocking the main thread while async-only tasks
	 * run in the background.
	 * 
	 * Can only be called on a server.
	 */
	public static Block getBlock(World w, int x, int y, int z) {
		if (y >= 0 && y < 256) //bounds check only in Y direction
		{
			Chunk chunk = null;

			try
			{
				chunk=getChunk(w,x,z);
				if(chunk != null) {
					return chunk.getBlock(x & 15, y, z & 15);
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
		return null;
	}

	public static int getBlockMetadata(World w, int x, int y, int z)
	{
		if (y < 0 || y >= 256)
			return 0;
		Chunk chunk = getChunk(w, x, z);
		if(chunk==null)
			return 0;
		x &= 15;
		z &= 15;
		return chunk.getBlockMetadata(x, y, z);
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

	public static enum AsyncResult{
		GOOD,
		OOB,
		NOT_GEN,
	}

	//Good
	public static AsyncResult setBlock(World w, int x, int y, int z, Block block, int meta, int notify)
	{
		//Don't bounds-check in XZ
		if (y < 0)
			return AsyncResult.OOB;
		if (y >= 256)
			return AsyncResult.OOB;
		Chunk chunk = getChunk(w, x, z);
		if(chunk==null)
			return AsyncResult.NOT_GEN;
		Block oldBlock = null;
		BlockSnapshot blockSnapshot = null;

		if ((notify & 1) != 0) {
			//chunk.getBlock() is pure WRT the rest of the world
			oldBlock = chunk.getBlock(x & 15, y, z & 15);
		}

		//We have to capture the snapshot before setting the block
		if (w.captureBlockSnapshots && !w.isRemote)
		{
			blockSnapshot = new BlockSnapshot(w, x, y, z, getBlock(w, x, y, z), getBlockMetadata(w, x, y, z), notify);
		}

		boolean couldSetBlock = chunkSetBlock(chunk, x & 15, y, z & 15, block, meta);

		if (!couldSetBlock && blockSnapshot != null)
			blockSnapshot = null;
		else
			//Synchronize on the world's list of captured snapshots
			synchronized(w.capturedBlockSnapshots){
				w.capturedBlockSnapshots.add(blockSnapshot);
			}

		worldUpdateLight(w, x, y, z);

		if (couldSetBlock && blockSnapshot == null)
			worldNotifyBlock(w, x, y, z, chunk, oldBlock, block, notify);

		return AsyncResult.GOOD;
	}


	//It looks like World.lightUpdateBlockList is only a field to avoid allocating it new every call
	//Allocating 2^15 ints every block set would be a Bad Thing (TM) so we have to pool them
	//TODO this could probably be tuned for better perf, depending on what happens
	private static BlockingQueue<int[]> lightUpdateLists=new ArrayBlockingQueue<int[]>(10);
	private static int[] getLightUpdateList(){
		int[] a;
		while(true){
			try {
				a = lightUpdateLists.poll(1, TimeUnit.MILLISECONDS);
				return a==null
						? new int[32*32*32]
								: a;
			} catch (InterruptedException ignored) {}
		}
	}
	private static void releaseLightUpdateList(int[] list){
		lightUpdateLists.add(list);
	}
	
	//From public boolean updateLightByType(EnumSkyBlock p_147463_1_, int p_147463_2_, int p_147463_3_, int p_147463_4_)
	//Looks mostly good
	//TODO can this leak outside the region?
	public static boolean worldUpdateLight(World w, EnumSkyBlock lightType, int x, int y, int z)
	{
		//Safe
		if (!w.doChunksNearChunkExist(x, y, z, 17))
			return false;
		int[] lightUpdateList=getLightUpdateList();

		int lightUpdateIndex = 0;
		int lightUpdateIndex2 = 0;
		int savedLightValue = worldGetSavedLightValue(w, lightType, x, y, z);
		int computedLightValue = worldComputeLightValue(w, x, y, z, lightType);
		int l1;
		int i2;
		int j2;
		int k2;
		int l2;
		int i3;
		int j3;
		int k3;
		int l3;

		if (computedLightValue > savedLightValue)
		{
			lightUpdateList[lightUpdateIndex2++] = 133152;
		}
		else if (computedLightValue < savedLightValue)
		{
			lightUpdateList[lightUpdateIndex2++] = 133152 | savedLightValue << 18;

			while (lightUpdateIndex < lightUpdateIndex2)
			{
				l1 = lightUpdateList[lightUpdateIndex++];
				i2 = (l1 & 63) - 32 + x;
				j2 = (l1 >> 6 & 63) - 32 + y;
				k2 = (l1 >> 12 & 63) - 32 + z;
				l2 = l1 >> 18 & 15;
				i3 = worldGetSavedLightValue(w, lightType, i2, j2, k2);

				if (i3 == l2)
				{
					worldSetLightValue(w, lightType, i2, j2, k2, 0);

					if (l2 > 0)
					{
						j3 = MathHelper.abs_int(i2 - x);
						k3 = MathHelper.abs_int(j2 - y);
						l3 = MathHelper.abs_int(k2 - z);

						if (j3 + k3 + l3 < 17)
						{
							for (int i4 = 0; i4 < 6; ++i4)
							{
								int j4 = i2 + Facing.offsetsXForSide[i4];
								int k4 = j2 + Facing.offsetsYForSide[i4];
								int l4 = k2 + Facing.offsetsZForSide[i4];
								//TODO location-sensitive
								int i5 = Math.max(1, getBlock(w, j4, k4, l4).getLightOpacity());
								i3 = worldGetSavedLightValue(w, lightType, j4, k4, l4);

								if (i3 == l2 - i5 && lightUpdateIndex2 < lightUpdateList.length)
								{
									lightUpdateList[lightUpdateIndex2++] = j4 - x + 32 | k4 - y + 32 << 6 | l4 - z + 32 << 12 | l2 - i5 << 18;
								}
							}
						}
					}
				}
			}

			lightUpdateIndex = 0;
		}

		while (lightUpdateIndex < lightUpdateIndex2)
		{
			l1 = lightUpdateList[lightUpdateIndex++];
			i2 = (l1 & 63) - 32 + x;
			j2 = (l1 >> 6 & 63) - 32 + y;
			k2 = (l1 >> 12 & 63) - 32 + z;
			l2 = worldGetSavedLightValue(w, lightType, i2, j2, k2);
			i3 = worldComputeLightValue(w, i2, j2, k2, lightType);

			if (i3 != l2)
			{
				worldSetLightValue(w, lightType, i2, j2, k2, i3);

				if (i3 > l2)
				{
					j3 = Math.abs(i2 - x);
					k3 = Math.abs(j2 - y);
					l3 = Math.abs(k2 - z);
					boolean flag = lightUpdateIndex2 < lightUpdateList.length - 6;

					if (j3 + k3 + l3 < 17 && flag)
					{
						if (worldGetSavedLightValue(w, lightType, i2 - 1, j2, k2) < i3)
						{
							lightUpdateList[lightUpdateIndex2++] = i2 - 1 - x + 32 + (j2 - y + 32 << 6) + (k2 - z + 32 << 12);
						}

						if (worldGetSavedLightValue(w, lightType, i2 + 1, j2, k2) < i3)
						{
							lightUpdateList[lightUpdateIndex2++] = i2 + 1 - x + 32 + (j2 - y + 32 << 6) + (k2 - z + 32 << 12);
						}

						if (worldGetSavedLightValue(w, lightType, i2, j2 - 1, k2) < i3)
						{
							lightUpdateList[lightUpdateIndex2++] = i2 - x + 32 + (j2 - 1 - y + 32 << 6) + (k2 - z + 32 << 12);
						}

						if (worldGetSavedLightValue(w, lightType, i2, j2 + 1, k2) < i3)
						{
							lightUpdateList[lightUpdateIndex2++] = i2 - x + 32 + (j2 + 1 - y + 32 << 6) + (k2 - z + 32 << 12);
						}

						if (worldGetSavedLightValue(w, lightType, i2, j2, k2 - 1) < i3)
						{
							lightUpdateList[lightUpdateIndex2++] = i2 - x + 32 + (j2 - y + 32 << 6) + (k2 - 1 - z + 32 << 12);
						}

						if (worldGetSavedLightValue(w, lightType, i2, j2, k2 + 1) < i3)
						{
							lightUpdateList[lightUpdateIndex2++] = i2 - x + 32 + (j2 - y + 32 << 6) + (k2 + 1 - z + 32 << 12);
						}
					}
				}
			}
		}
		releaseLightUpdateList(lightUpdateList);
		return true;
	}

	//good
    public static void worldSetLightValue(World w, EnumSkyBlock type, int x, int y, int z, int lightLevel)
    {
    	Chunk chunk = getChunk(w, x, z);
    	if(chunk!=null)
    		chunk.setLightValue(type, x & 15, y, z & 15, lightLevel);
    }

	//good
	private static int worldComputeLightValue(World w, int x, int y, int z, EnumSkyBlock lightType)
	{
		if (lightType == EnumSkyBlock.Sky && w.canBlockSeeTheSky(x, y, z))
		{
			return 15;
		}
		else
		{
			Block block = getBlock(w, x, y, z);
			//TODO location-sensitive?
			int blockLight = block.getLightValue();
			int lightFromBlock = lightType == EnumSkyBlock.Sky ? 0 : blockLight;
			int blocksOpacity = block.getLightOpacity();

			if (blocksOpacity >= 15 && blockLight > 0)
				blocksOpacity = 1;

			if (blocksOpacity < 1)
				blocksOpacity = 1;

			if (blocksOpacity >= 15)
				return 0;
			if (lightFromBlock >= 14)
				return lightFromBlock;
			for (int i = 0; i < 6; ++i)
			{
				int adjX = x + Facing.offsetsXForSide[i];
				int adjY = y + Facing.offsetsYForSide[i];
				int adjZ = z + Facing.offsetsZForSide[i];
				int oldLight = worldGetSavedLightValue(w, lightType, adjX, adjY, adjZ) - blocksOpacity;

				if (oldLight > lightFromBlock)
				{
					lightFromBlock = oldLight;
				}

				if (lightFromBlock >= 14)
				{
					return lightFromBlock;
				}
			}

			return lightFromBlock;
		}
	}

	//Good
	public static int worldGetSavedLightValue(World w, EnumSkyBlock type, int x, int y, int z)
	{
		Chunk chunk=getChunk(w,x,z);
		if(chunk!=null)
			return chunk.getSavedLightValue(type, x & 15, y, z & 15);
		return type.defaultLightValue;
	}

	//Good
	//From public boolean func_147451_t(int p_147451_1_, int p_147451_2_, int p_147451_3_)
	private static boolean worldUpdateLight(World w, int x, int y, int z)
	{
		boolean flag = false;

		if (!w.provider.hasNoSky)
		{
			flag |= worldUpdateLight(w, EnumSkyBlock.Sky, x, y, z);
		}

		flag |= worldUpdateLight(w, EnumSkyBlock.Block, x, y, z);
		return flag;
	}

	//TODO
	//From public void markAndNotifyBlock(int x, int y, int z, Chunk chunk, Block oldBlock, Block newBlock, int flag)
	public static void worldNotifyBlock(World w, int x, int y, int z, Chunk chunk, Block oldBlock, Block newBlock, int flag)
	{
		if ((flag & 1) != 0)
		{
			//TODO
			w.notifyBlockChange(x, y, z, oldBlock);

			if (newBlock.hasComparatorInputOverride())
			{
				worldNotifyNeighbours(w, x, y, z, newBlock);
			}
		}
	}
	
	//TODO 
	//From public void func_147453_f(int p_147453_1_, int p_147453_2_, int p_147453_3_, Block p_147453_4_)
    public static void worldNotifyNeighbours(World w, int x, int y, int z, Block newBlock)
    {
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
        {
            int newX = x + dir.offsetX;
            int newY  = y + dir.offsetY;
            int newZ = z + dir.offsetZ;
            Block block1 = getBlock(w, newX, newY, newZ);

            //TODO take block calls to main thread (somehow)
            	//They should be OK because the state's different
            block1.onNeighborChange(w, newX, newY, newZ, x, y, z);
            if (block1.isNormalCube(w, newX, newY, newZ))
            {
                newX += dir.offsetX;
                newY  += dir.offsetY;
                newZ += dir.offsetZ;
                Block block2 = getBlock(w, newX, newY, newZ);

                if (block2.getWeakChanges(w, newX, newY, newZ))
                {
                    block2.onNeighborChange(w, newX, newY, newZ, x, y, z);
                }
            }
        }
    }

	//TODO
	//From chunk.func_150807_a()
	private static boolean chunkSetBlock(Chunk c, int cx, int y, int cz, Block newBlock, int meta)
	{
		int columnIndex = cz << 4 | cx;

		if (y >= c.precipitationHeightMap[columnIndex] - 1)
		{
			c.precipitationHeightMap[columnIndex] = -999;
		}

		int oldHeight = c.heightMap[columnIndex];
		Block oldBlock = c.getBlock(cx, y, cz);
		int oldMeta = c.getBlockMetadata(cx, y, cz);

		if (oldBlock == newBlock && oldMeta == meta)
		{
			return false;
		}
		else
		{
			ExtendedBlockStorage xbs = c.storageArrays[y >> 4];
			boolean flag = false;

			if (xbs == null)
			{
				if (newBlock == Blocks.air)
				{
					return false;
				}

				xbs = c.storageArrays[y >> 4] = new ExtendedBlockStorage(y >> 4 << 4, !c.worldObj.provider.hasNoSky);
				flag = y >= oldHeight;
			}

			int worldX = c.xPosition * 16 + cx;
			int worldZ = c.zPosition * 16 + cz;

			int oldOpacity = oldBlock.getLightOpacity(c.worldObj, worldX, y, worldZ);

			if (!c.worldObj.isRemote)
			{
				oldBlock.onBlockPreDestroy(c.worldObj, worldX, y, worldZ, oldMeta);
			}

			xbs.func_150818_a(cx, y & 15, cz, newBlock);
			xbs.setExtBlockMetadata(cx, y & 15, cz, meta); // c line duplicates the one below, so breakBlock fires with valid worldstate

			if (!c.worldObj.isRemote)
			{
				oldBlock.breakBlock(c.worldObj, worldX, y, worldZ, oldBlock, oldMeta);
				// After breakBlock a phantom TE might have been created with incorrect meta. c attempts to kill that phantom TE so the normal one can be create properly later
				TileEntity te = c.getTileEntityUnsafe(cx & 0x0F, y, cz & 0x0F);
				if (te != null && te.shouldRefresh(oldBlock, c.getBlock(cx & 0x0F, y, cz & 0x0F), oldMeta, c.getBlockMetadata(cx & 0x0F, y, cz & 0x0F), c.worldObj, worldX, y, worldZ))
				{
					c.removeTileEntity(cx & 0x0F, y, cz & 0x0F);
				}
			}
			else if (oldBlock.hasTileEntity(oldMeta))
			{
				TileEntity te = c.getTileEntityUnsafe(cx & 0x0F, y, cz & 0x0F);
				if (te != null && te.shouldRefresh(oldBlock, newBlock, oldMeta, meta, c.worldObj, worldX, y, worldZ))
				{
					c.worldObj.removeTileEntity(worldX, y, worldZ);
				}
			}

			if (xbs.getBlockByExtId(cx, y & 15, cz) != newBlock)
			{
				return false;
			}
			else
			{
				xbs.setExtBlockMetadata(cx, y & 15, cz, meta);

				if (flag)
				{
					c.generateSkylightMap();
				}
				else
				{
					int j2 = newBlock.getLightOpacity(c.worldObj, worldX, y, worldZ);

					if (j2 > 0)
					{
						if (y >= oldHeight)
						{
							c.relightBlock(cx, y + 1, cz);
						}
					}
					else if (y == oldHeight - 1)
					{
						c.relightBlock(cx, y, cz);
					}

					if (j2 != oldOpacity && (j2 < oldOpacity || c.getSavedLightValue(EnumSkyBlock.Sky, cx, y, cz) > 0 || c.getSavedLightValue(EnumSkyBlock.Block, cx, y, cz) > 0))
					{
						c.propagateSkylightOcclusion(cx, cz);
					}
				}

				TileEntity tileentity;

				if (!c.worldObj.isRemote)
				{
					newBlock.onBlockAdded(c.worldObj, worldX, y, worldZ);
				}

				if (newBlock.hasTileEntity(meta))
				{
					tileentity = c.func_150806_e(cx, y, cz);

					if (tileentity != null)
					{
						tileentity.updateContainingBlockInfo();
						tileentity.blockMetadata = meta;
					}
				}

				c.isModified = true;
				return true;
			}
		}
	}
}
