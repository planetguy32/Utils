package me.planetguy.core.sl;

import java.util.ArrayList;
import java.util.List;

import me.planetguy.util.Debug;
import net.minecraft.launchwrapper.IClassTransformer;

public class SLClassDiscoverer implements IClassTransformer{
	
	public static List<String> classes=new ArrayList<String>();
	
	@Override
	public byte[] transform(String name, String newName, byte[] clazz) {
		classes.add(newName);
		Debug.dbg("CLASS\t\t\t n1="+name+", n2="+newName);
		return clazz;
	}

}
