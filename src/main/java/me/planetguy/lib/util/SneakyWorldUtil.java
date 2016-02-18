package me.planetguy.lib.util;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public abstract class SneakyWorldUtil {

    public static boolean setBlock(World world, int x, int y, int z, Block newBlock, int meta) {
        return setBlock(world, x, y, z, newBlock, meta, false);
    }

    public static boolean setBlock(World world, int x, int y, int z, Block newBlock, int meta, boolean skipUpdateClient) {
        world.restoringBlockSnapshots=true;
        int chunkX = x & 0xF;
        int chunkZ = z & 0xF;

        Chunk chunk = world.getChunkFromBlockCoords(x, z);
        
        if(chunk==null)
        	return false;

        int xzCombinedPosition = chunkZ << 4 | chunkX;

        if (y >= chunk.precipitationHeightMap[xzCombinedPosition] - 1)
        {
            chunk.precipitationHeightMap[xzCombinedPosition] = -999;
        }

        int heightMapAtTarget = chunk.heightMap[xzCombinedPosition];
        Block oldBlock = chunk.getBlock(chunkX, y, chunkZ);
        int metadata = chunk.getBlockMetadata(chunkX, y, chunkZ);

        if (oldBlock == newBlock && metadata == meta)
        {
            return false;
        }
        else
        {
            ExtendedBlockStorage xbs = chunk.storageArrays[y >> 4];
            boolean heightMapChanged = false;

            if (xbs == null)
            {
                if (newBlock == Blocks.air)
                {
                    return false;
                }

                xbs = chunk.storageArrays[y >> 4] = new ExtendedBlockStorage(y >> 4 << 4, !world.provider.hasNoSky);
                heightMapChanged = y >= heightMapAtTarget;
            }

            int oldOpacity = oldBlock.getLightOpacity(world, x, y, z);

            xbs.func_150818_a(chunkX, y & 15, chunkZ, newBlock);
            xbs.setExtBlockMetadata(chunkX, y & 15, chunkZ, meta); 

            if (!world.isRemote)
            {
                // After breakBlock a phantom TE might have been created with incorrect meta. This attempts to kill that phantom TE so the normal one can be create properly later
                TileEntity te = chunk.getTileEntityUnsafe(chunkX & 0x0F, y, chunkZ & 0x0F);
                if (te != null && te.shouldRefresh(oldBlock, chunk.getBlock(chunkX & 0x0F, y, chunkZ & 0x0F), metadata, chunk.getBlockMetadata(chunkX & 0x0F, y, chunkZ & 0x0F), world, x, y, z))
                {
                	 //suppress item drops from TileEntity.invalidate()
                    chunk.removeTileEntity(chunkX & 0x0F, y, chunkZ & 0x0F);
                }
            }
            else if (oldBlock.hasTileEntity(metadata))
            {
                TileEntity te = chunk.getTileEntityUnsafe(chunkX & 0x0F, y, chunkZ & 0x0F);
                if (te != null && te.shouldRefresh(oldBlock, newBlock, metadata, meta, world, x, y, z))
                {
                    world.removeTileEntity(x, y, z);
                }
            }

            if (xbs.getBlockByExtId(chunkX, y & 15, chunkZ) != newBlock)
            {
                world.restoringBlockSnapshots=false;
                return false;
            }
            else
            {
                xbs.setExtBlockMetadata(chunkX, y & 15, chunkZ, meta);

                if(!skipUpdateClient) {
                    if (heightMapChanged) {
                        chunk.generateSkylightMap();
                    } else {
                        int newOpacity = newBlock.getLightOpacity(world, x, y, z);

                        if (newOpacity > 0) {
                            if (y >= heightMapAtTarget) {
                                relightChunkBlock(chunk, chunkX, y, chunkZ);
                            }
                        } else if (y == heightMapAtTarget - 1) {
                            relightChunkBlock(chunk, chunkX, y, chunkZ);
                        }

                        if (newOpacity != oldOpacity && (newOpacity < oldOpacity || chunk.getSavedLightValue(EnumSkyBlock.Sky, chunkX, y, chunkZ) > 0 || chunk.getSavedLightValue(EnumSkyBlock.Block, chunkX, y, chunkZ) > 0)) {
                            chunk.propagateSkylightOcclusion(chunkX, chunkZ);
                        }
                    }
                }

                TileEntity tileentity;

                if (newBlock.hasTileEntity(meta))
                {
                    tileentity = chunk.func_150806_e(chunkX, y, chunkZ);

                    if (tileentity != null)
                    {
                        tileentity.updateContainingBlockInfo();
                        tileentity.blockMetadata = meta;
                    }
                }

                chunk.isModified = true;

                if(!skipUpdateClient) {
                    world.func_147451_t(x, y, z);

                    if (chunk.func_150802_k()) world.markBlockForUpdate(x, y, z);
                }

                world.restoringBlockSnapshots=false;
                return true;
            }
        }

    }

    public static void setTileEntity(World world, int X, int Y, int Z, TileEntity entity) {
        if (entity == null) { throw new NullPointerException(); }
        // This does exactly the same thing, except without reflection
        world.addTileEntity(entity);
        world.restoringBlockSnapshots=true;
        world.getChunkFromBlockCoords(X, Z).func_150812_a(X & 0xF, Y, Z & 0xF, entity);
        world.restoringBlockSnapshots=false;
    }

    public static void notifyBlocks(World world, int X, int Y, int Z, Block OldId, Block NewId) {
        world.notifyBlockChange(X,Y,Z,OldId);

        if (NewId == null) { return; }

        if ((world.getTileEntity(X, Y, Z) != null) || (NewId.hasComparatorInputOverride())) {
            world.func_147453_f(X, Y, Z, NewId);
        }
    }

    public static void refreshBlock(World world, int X, int Y, int Z, Block OldId, Block NewId) {
        notifyBlocks(world, X, Y, Z, OldId, NewId);
    }
    
    public static void relightBlock(World w, int x, int y, int z) {
    	w.getChunkFromBlockCoords(x, z).relightBlock(x&15, y, z&15);
    }
    
    public static void relightChunkBlock(Chunk c, int cx, int y, int cz) {
    	//c.relightBlock(cx, y, cz);
    }
    
    public static void fixLighting(Chunk c, int cx, int y, int cz) {
    	
    }

}
