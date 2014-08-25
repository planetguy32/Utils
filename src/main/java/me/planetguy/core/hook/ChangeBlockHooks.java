package me.planetguy.core.hook;

import java.util.ArrayList;
import java.util.List;

import me.planetguy.lib.api.IChangeBlockHook;
import net.minecraft.block.Block;
import net.minecraft.world.World;

public class ChangeBlockHooks {
	
	public static List<IChangeBlockHook> hooks=new ArrayList<IChangeBlockHook>();
	
	public static void registerHook(IChangeBlockHook hook){
		hooks.add(hook);
	}
	
	public static boolean setBlock(World w, int x, int y, int z, Block block, int meta, int flag){
		boolean deny=false;
		for(IChangeBlockHook hook:hooks){
			EventResponse resp=hook.setBlock(w, x, y, z, block, meta, flag);
			if(resp==EventResponse.FORCE_ALLOW)
				return true;
			else if(resp==EventResponse.DENY)
				deny=true;
		}
		return !deny;
	}
	
	public static boolean setBlockMetadataWithNotify(int x, int y, int z, int meta, int flag){
		boolean deny=false;
		for(IChangeBlockHook hook:hooks){
			EventResponse resp=hook.setBlockMetadataWithNotify(x,y,z,meta,flag);
			if(resp==EventResponse.FORCE_ALLOW)
				return true;
			else if(resp==EventResponse.DENY)
				deny=true;
		}
		return !deny;
	}
	
}
