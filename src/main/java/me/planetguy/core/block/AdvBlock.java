package me.planetguy.core.block;

import java.util.HashMap;
import java.util.List;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public abstract class AdvBlock extends BlockContainer{

	private Class<? extends TileEntity>[] teClasses;
	
	IIcon[][] IIconMatrix;
	
	public AdvBlock( Material mat) {
		super(mat);
	}
	
	@Override
	public TileEntity createNewTileEntity(World w, int meta){
		try {
			return (TileEntity) getTEClasses()[meta].newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public void getSubBlocks(int p_149666_1_, CreativeTabs p_149666_2_, List items){
		for(int i=0; i<getTEClasses().length; i++){
			items.add(new ItemStack(this, 1, i));
		}
	}
	
	public IIcon getIIcon(int side, int meta){
		return IIconMatrix[meta][side];
	}
	
	public void registerIIcons(IIconRegister ir){
		String[][] imatrix=getIconNameMatrix();
		HashMap<String, IIcon> imap=new HashMap<String, IIcon>();
		IIconMatrix=new IIcon[imatrix.length][];
		for(int x=0; x<imatrix.length; x++){
			IIconMatrix[x]=new IIcon[imatrix[x].length];
			for(int y=0; y<IIconMatrix[x].length; y++){
				if(imap.containsKey(imatrix[x][y])){
					IIconMatrix[x][y]=imap.get(imatrix[x][y]);
				}else{
					IIcon i=ir.registerIcon(getIconNamespace()+":"+imatrix[x][y]);
					imap.put(imatrix[x][y], i);
					IIconMatrix[x][y]=i;
				}
			}
		}
	}

	Class<? extends TileEntity>[] getTEClasses() {
		return teClasses;
	}
	
	public void setTEClasses(Class[] classes){
		this.teClasses=classes;
		for(Class c:teClasses){
			GameRegistry.registerTileEntity(c, c.getCanonicalName());
		}
	}

	public abstract String[][] getIconNameMatrix();
	
	public void setNames(String lang, String[] names){
		for(int i=0; i<names.length; i++){
			LanguageRegistry.instance().addStringLocalization(getClass().getCanonicalName()+"."+i+".name", lang, names[i]);
		}
	}

	public abstract String getIconNamespace();


	
}