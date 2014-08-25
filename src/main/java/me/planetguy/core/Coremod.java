package me.planetguy.core;

import java.util.Map;

import me.planetguy.core.sl.SLClassDiscoverer;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@MCVersion("1.7.10")
public class Coremod implements IFMLLoadingPlugin {
	
	@Override
	public String[] getASMTransformerClass() {
		return new String[]{SLClassDiscoverer.class.getCanonicalName(), PlanetguyAT.class.getCanonicalName()};
	}

	@Override
	public String getModContainerClass() {
		return PlanetguyCoreModContainer.class.getCanonicalName();
	}

	@Override
	public String getSetupClass() {
		return null;
	}

    @Override
    public void injectData(Map<String, Object> data)
    {
    }
    
	@Override
	public String getAccessTransformerClass() {
		return null;
	}


}
