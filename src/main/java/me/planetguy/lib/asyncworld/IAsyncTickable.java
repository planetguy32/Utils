package me.planetguy.lib.asyncworld;

import net.minecraft.world.World;

/**
 * Can receive asynchronous ticks.
 * 
 * TODO make it actually get called
 */
public interface IAsyncTickable {

	/**
	 * Called asynchronously at start of world's tick. Can do synchronization-free access to blocks and TileEntities within 16 blocks in any direction.
	 * 
	 * To schedule this, call AsyncBlockUpdate.scheduleAsyncTick(world, x, y, z, ticksFromNowToTickIn).
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 */
	void onAsyncLocalUpdate(World world, int x, int y, int z);

}
