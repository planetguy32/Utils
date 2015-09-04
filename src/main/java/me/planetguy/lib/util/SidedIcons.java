package me.planetguy.lib.util;

import net.minecraft.client.renderer.IconFlipped;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

public class SidedIcons {
	
	public SidedIcons(IIcon front, IIcon sideUp, IIcon sideDown, IIcon sideFacingLeft, IIcon back ) {
		IIcon sideFacingRight=new IconFlipped(sideFacingLeft, true, false);
		
		Debug.mark();
		
		iconMatrix=new IIcon[][] {
				{back, front, sideUp, sideUp, sideUp, sideUp},
				{front, back, sideDown, sideDown, sideDown, sideDown},
				{sideDown, sideDown, back, front, sideFacingRight, sideFacingLeft},
				{sideUp, sideUp, front, back, sideFacingLeft, sideFacingRight},
				{sideFacingRight, sideFacingRight, sideFacingLeft, sideFacingRight, back, front},
				{sideFacingLeft, sideFacingLeft, sideFacingRight, sideFacingLeft, front, back}
		};
	}
	
	public SidedIcons(String mod, String blockName, IIconRegister ir) {
		this(
				ir.registerIcon(mod+":"+blockName+"Front"), 
				ir.registerIcon(mod+":"+blockName+"SideUp"), 
				ir.registerIcon(mod+":"+blockName+"SideDown"),
				ir.registerIcon(mod+":"+blockName+"SideL"),
				ir.registerIcon(mod+":"+blockName+"Back")
				);
	}
	
	private IIcon[][] iconMatrix;
	
	public IIcon getIcon(ForgeDirection facing, int side) {
		return getIcon(facing.ordinal(), side);
	}
	
	public IIcon getIcon(int facing, int side) {
		return iconMatrix[facing][side];
	}

}
