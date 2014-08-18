package me.planetguy.core.sl;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

final class CreativeTab extends CreativeTabs {
	private final ItemStack stack;

	CreativeTab(String label, ItemStack... stacks) {
		super(label);
		for(ItemStack gb:stacks){
			if(gb!=null&&gb.getItem()!=null){
				this.stack = gb;
				return;
			}
		}
		throw new NullPointerException("Received only null and/or ItemStacks with null items");
		
	}

	public ItemStack func_151244_d(){
		return stack;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Item getTabIconItem() {
		return null;
	}
}