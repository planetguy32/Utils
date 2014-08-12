package me.planetguy.core.simpleLoader;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.launchwrapper.IClassTransformer;

public class SLClassDiscoverer implements IClassTransformer{
	
	public static List<String> classes=new ArrayList<String>();
	
	@Override
	public byte[] transform(String name, String newName, byte[] clazz) {
		classes.add(newName);
		return clazz;
	}

}
