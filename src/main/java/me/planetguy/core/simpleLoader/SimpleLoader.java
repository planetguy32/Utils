package me.planetguy.core.simpleLoader;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.text.html.parser.Entity;

import me.planetguy.util.Debug;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

/**Class containing logic to allow easily-created content modules with dependency management.
 *  
 * @author planetguy
 *
 */


public class SimpleLoader {

	private final HashMap<Class, Integer> IDMap = new HashMap<Class, Integer>();
	public Class[] moduleClasses; //unfiltered, unsorted classes
	public Class[] filteredSortedClasses;
	public Class[] entities,custom;
	public String modClassName;
	
	private Class modClass;
	
	public static File mcdir;

	private List<String> moduleList=new ArrayList<String>();
	
	public List<Item> itemsMade=new ArrayList<Item>();
	public List<Block>blocksMade=new ArrayList<Block>();

	/**
	 * IDMap contains all the config data loaded. 
	 */

	private int passLimit;
	private boolean generateCode;

	public SimpleLoader(Configuration cfg){
		String modClassName=Thread.currentThread().getStackTrace()[0].getClassName();
		try{
			moduleClasses=discoverSLModules(modClassName);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		SLSort.sort(moduleClasses);
		this.modClassName=modClassName;
		Property prop=cfg.get("[SL] Framework","SL loader",1);
		initDynamically(cfg);
		try{
			modClass=Class.forName(modClassName);
		}catch(Exception e){//
			throw new RuntimeException("Could not resolve mod class "+modClass);
		}
	}

	public void initDynamically(Configuration cfg){
		entities=filterClassesBySuper(Entity.class);
		custom=filterClassesBySuper(CustomModuleLoader.class);
		try {
			setupAndReadConfig(cfg);
		} catch (Exception e) {
			Debug.dbg(e);
		}
		filterAndSortClasses();
	}

	/**
	 * Nicely formats class names for printing.
	 */
	private String formatClasses(Class[] classes){ 
		String s="[";
		for(Class c:classes){
			s+=c.getName()+",";
		}s+="]";
		return s;
	}

	/**
	 * 
	 * @return the names of modules to be loaded
	 */

	public String[] getModuleNames(){
		LinkedList<String> moduleNames=new LinkedList<String>();
		for(Class c:moduleClasses){
			moduleNames.add(SLLoadUtils.getModuleName(c));
		}
		return moduleNames.toArray(new String[0]);
	}

	/**
	 * Gets Forge config options for SL framework stuff, properties marked to fill with @SLProp and any IDs needed by modules
	 * @param config passed from the Forge
	 * @throws Exception if anything goes wrong
	 */

	private void setupAndReadConfig(Configuration config) throws Exception{
		for(int i=0; i<moduleClasses.length; i++){
			boolean show=!(SLLoadUtils.getSLL(moduleClasses[i]).isTechnical());
			if(!show||config.get("[SL] Item-restrict","allow '"+moduleList.get(i)+"'",true).getBoolean(true))
				moduleList.add(SLLoadUtils.getModuleName(moduleClasses[i]));
		}
		
		Debug.dbg("[SL] Modules: "+moduleList);
		
		//moduleList=Arrays.asList(config.get("[SL] Framework","List of allowed modules", moduleList.toArray(new String[0])).getStringList());
		passLimit=config.get("[SL] Framework", "Maximum dependency passes", 10).getInt(10);

		//and get config values for the game content...

		int currentID=201;
		for(Class c:entities){//and entities
			int id=config.get("Entities", SLLoadUtils.getModuleName(c), currentID).getInt(currentID);
			++currentID;
			IDMap.put(c,id);
		}

		for(Class c:custom){
			//Debug.dbg(c.getName());
			CustomModuleLoader cml=(CustomModuleLoader) c.newInstance();
			cml.load();
		}
		for(Class c:moduleClasses){
			for(Field f:c.getDeclaredFields()){
				SLProp prop=f.getAnnotation(SLProp.class);
				if(prop!=null){
					String category=prop.category();
					String key=prop.name();
					Object defaultVal=f.get(null); //do field operations with null, assuming static field.
					//Get config props for items that request them.
					if(f.getGenericType().equals(Integer.TYPE)){
						f.setInt(null, config.get(category, key, (Integer) defaultVal).getInt());
					}else if(f.getGenericType().equals(Double.TYPE)){
						f.setDouble(null, config.get(category, key, (Double) defaultVal).getDouble((Double) defaultVal));
					}else if(f.getGenericType().toString()=="class java.lang.String"){
						f.set(null, config.get(category, key, (String) defaultVal).getString());
					}else if(f.getGenericType().toString()=="class [Ljava.lang.String;"){
						f.set(null, config.get(category, key, (String[]) defaultVal).getStringList());
					}else if(f.getGenericType().toString()=="class [I"){
						f.set(null, config.get(category, key, (int[]) defaultVal).getIntList());
					}else if(f.getGenericType().toString()=="class [D"){
						f.set(null, config.get(category, key, (double[]) defaultVal).getDoubleList());
					}

				}
			}
		}
	}

	public void filterAndSortClasses(){
		int pass=0; //How many passes the dependency manager has gone over the class list.
		ArrayList<Class> sortedClasses=new ArrayList<Class>();
		HashSet<String> loadedClasses=new HashSet<String>();
		ArrayDeque<Class> classes=new ArrayDeque<Class>();
		for(Class c:moduleClasses){
			SLLoad sll=(SLLoad) c.getAnnotation(SLLoad.class);
			if(moduleList.contains(sll.name())){
				classes.add(c);
			}
		}
		classes.addLast(SimpleLoader.class);
		int seq=0;
		while(!classes.isEmpty()&&pass<passLimit&&seq<1000){
			++seq;
			Class c=classes.pop();
			if(c==SimpleLoader.class){
				++pass; 
				classes.addLast(SimpleLoader.class);
				continue;//Don't get SLLoad from SimpleLoader, it would explode!
			}
			SLLoad slload=(SLLoad) c.getAnnotation(SLLoad.class);
			boolean allowSoFar=true;
			for(String s:slload.dependencies()){
				if(!loadedClasses.contains(s)){
					allowSoFar=false;
					break;
				}
			}
			if(allowSoFar){
				loadedClasses.add(slload.name());
				sortedClasses.add(c);
			}else{
				classes.addLast(c);
			}
		}
		filteredSortedClasses=sortedClasses.toArray(new Class[0]);
		Debug.dbg("Classes not loaded: "+classes.toString());
	}

	/**A way to filter classes by superclass (get anything extending, for example, Block)
	 * 
	 * @param superclass the class to filter by
	 * @return classes that inherit the superclass
	 */

	private Class[] filterClassesBySuper(Class superclass){
		ArrayList<Class> classes=new ArrayList<Class>();
		for(Class c:moduleClasses){
			if(superclass.isAssignableFrom(c)){
				classes.add(c);
			}
		}
		return classes.toArray(new Class[0]);
	}

	/**A way to load a single class with @SLLoad. DANGER: BYPASSES DEPENDENCY MANAGEMENT!
	 * 
	 * @param c
	 * @throws Exception
	 */

	public void loadClass(Class c) throws Exception{
		if(Block.class.isAssignableFrom(c)){
			loadBlock(c);
		}else if(Item.class.isAssignableFrom(c)){
			loadItem(c);
		}else if(Entity.class.isAssignableFrom(c)){
			loadEntity(c);
		}else if(CustomModuleLoader.class.isAssignableFrom(c)){
			loadCustomModule(c);
		}
	}

	/**
	 * Loads all the classes in SL's arrays of classes to load
	 * @throws Exception
	 */

	public void finishLoading() throws Exception{
		Debug.dbg("[SL] Loading classes...");
		for(Class c:filteredSortedClasses){
			loadClass(c);
		}
		
	}

	private void loadCustomModule(Class c)throws Exception{
		Debug.dbg("[SL] Loading "+c.getName());
		CustomModuleLoader cml=(CustomModuleLoader) c.newInstance();
		Field f=modClass.getClass().getDeclaredField(SLLoadUtils.getModuleName(c));
		f.set(modClass,cml);
		cml.load();
	}

	private void loadEntity(Class c){
		Debug.dbg("[SL] Loading "+c.getName());
		try {
			EntityRegistry.registerModEntity(
					c, 
					SLLoadUtils.getModuleName(c), 
					(Integer) IDMap.get(SLLoadUtils.getModuleName(c)), 
					Class.forName(this.modClassName).getDeclaredField("instance").get(null), 
					80, 
					3, 
					true);
		
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void loadItem(Class c) throws Exception{
		Debug.dbg("[SL] Loading "+c.getName());
		Object item = null;
		Constructor[] cons=c.getConstructors();
		for(Constructor con : cons){
			if(con.isAnnotationPresent(SLLoad.class)){
				item=con.newInstance();
				GameRegistry.registerItem((Item)item, modClassName+"."+SLLoadUtils.getModuleName(c));
				Field f=modClass.getClass().getDeclaredField(SLLoadUtils.getModuleName(c));
				f.set(modClass,item);
				itemsMade.add((Item)item);
			}
		}
		for(Method m:c.getMethods()){
			if(m.isAnnotationPresent(SLLoad.class)){
				m.invoke(item);
			}
		}
	}

	private void loadBlock(Class c) throws Exception{
		Debug.dbg("[SL] Loading "+c.getName());
		SLLoad slload=(SLLoad) c.getAnnotation(SLLoad.class);
		Object block = null;
		Constructor[] cons=c.getConstructors();
		for(Constructor con : cons){
			if(con.isAnnotationPresent(SLLoad.class)){
				try{
					block=con.newInstance();
					Field f=modClass.getClass().getDeclaredField(SLLoadUtils.getModuleName(c));
					f.set(modClass,block);
					blocksMade.add((Block) block);
				}catch(Exception e){
					e.printStackTrace();
					Debug.dbg(c.getName());
					throw(e);
				}

				GameRegistry.registerBlock((Block) block, 
						(Class<? extends ItemBlock>)
						(slload.itemClass()!="net.minecraft.item.ItemBlockWithMetadata"? 
								Class.forName(slload.itemClass()) 
								: ItemBlock.class),
								modClassName+"."+SLLoadUtils.getModuleName(c));
			}
		}
		for(Method m:c.getMethods()){
			if(m.isAnnotationPresent(SLLoad.class)){
				m.invoke(block);
			}
		}
	}

	/**
	 * 
	 * @return the classes loaded by the ASM transformer, 
	 */
	private Class[] discoverSLModules(String modname) throws Exception{
		List<Class> cnames=new ArrayList<Class>();
		String pkg=modname.replaceAll(".[a-zA-Z]*", "");
		for(String s:SLClassDiscoverer.classes){
			if(s.startsWith(pkg)){
				cnames.add(Class.forName(s));
			}
		}
		return cnames.toArray(new Class[0]);
	}

	public void setCreativeTab(CreativeTabs tab, String[] blacklist) {
		for(Block i:this.blocksMade){
			i.setCreativeTab(tab);
		}
		for(Item i:this.itemsMade){
			i.setCreativeTab(tab);
		}
	}
}
