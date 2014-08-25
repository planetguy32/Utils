package me.planetguy.lib.api;

import me.planetguy.core.hook.EventResponse;
import net.minecraft.block.Block;
import net.minecraft.world.World;

public interface IChangeBlockHook {

	public EventResponse setBlock(World w, int x, int y, int z, Block block, int meta, int flag);
	
	public EventResponse setBlockMetadataWithNotify(int x, int y, int z, int meta, int flag);
	
}
