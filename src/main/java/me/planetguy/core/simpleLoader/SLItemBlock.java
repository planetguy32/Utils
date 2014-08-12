package me.planetguy.core.simpleLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlockWithMetadata;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.registry.LanguageRegistry;

public class SLItemBlock extends ItemBlockWithMetadata{

	/**
	 * Maximum metadata SLItemBlock supports. Can be changed as necessary.
	 */
	public static final int maxMetadata=16;

	public static Map<Block, String[][]> tooltips=new HashMap<Block, String[][]>();
	public static List<SLItemBlock> slItemBlocks=new ArrayList<SLItemBlock>();

	public String[] name=new String[maxMetadata];
	public String[][] tooltip=new String[maxMetadata][0];

	public SLItemBlock( Block block) {
		super(block, block);
		setHasSubtypes(true);
		slItemBlocks.add(this);
	}
	
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List tooltipLines, boolean showMore){
        if(!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){
        	tooltipLines.add("Hold <shift> for more");
        	return;
        }
		try{
			int meta=itemStack.getItemDamage();
			for(String TTtext:tooltip[meta]){
				tooltipLines.add(TTtext);
			}
		}catch(Exception e){
			return;
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack stk){
		try{
			return name[0]+"("+name[stk.getItemDamage()]+")";
		}catch(Exception e){
			return super.getUnlocalizedName(stk);
		}
	}

	public static void registerString(Block block, int meta, String name, String... tooltip){
		
		block.setBlockName(block.getClass().getCanonicalName());
		LanguageRegistry.instance().addStringLocalization(block.getClass().getCanonicalName(), "en_US", name);

		String[][] ttips=tooltips.get(block);
		if(ttips==null)ttips=new String[maxMetadata][0];
		ttips[meta]=tooltip;
		tooltips.put(block, ttips);
	}
	
}
