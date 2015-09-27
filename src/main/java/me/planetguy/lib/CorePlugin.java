package me.planetguy.lib;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

//TODO hook this up, make it apply mixins to World and Chunk for async access
public class CorePlugin implements IFMLLoadingPlugin {

	@Override
	public String[] getASMTransformerClass() {
		return new String[] {
				"ne.planetguy.lib.asyncworld.impl.AsyncWorldTransformer"
		};
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}
