package me.planetguy.lib.asyncworld.impl;

import net.minecraft.launchwrapper.IClassTransformer;

public class AsyncWorldTransformer implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] classIn) {
		
		return classIn;
	}

}
