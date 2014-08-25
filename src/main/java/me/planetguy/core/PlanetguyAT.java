package me.planetguy.core;

import java.io.IOException;

import cpw.mods.fml.common.asm.transformers.AccessTransformer;

public class PlanetguyAT extends AccessTransformer{

	public PlanetguyAT() throws IOException {
		super("planetguy_at.cfg");
	}

}
