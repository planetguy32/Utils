package me.planetguy.lib.cc;

import me.planetguy.util.Debug;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.Optional;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;

@Optional.Interface(modid="ComputerCraft", iface = "dan200.computercraft.api.peripheral.IPeripheralProvider")
public class SimplePeripheralProvider implements IPeripheralProvider {
	
	public static final SimplePeripheralProvider instance;
	
	static{
		instance=new SimplePeripheralProvider();
		try {
			Class.forName("dan200.computercraft.api.ComputerCraftAPI")
			.getDeclaredMethod("registerPeripheralProvider", Class.forName("dan200.computercraft.api.peripheral.IPeripheralProvider"))
			.invoke(null, instance);
		} catch (Exception e) {
			Debug.dbg("Error enabling SimplePeripheral system! It probably won't work!");
			e.printStackTrace();
		}
	}
	
	private SimplePeripheralProvider(){}
	
	@Optional.Method(modid="ComputerCraft")
	@Override
	public IPeripheral getPeripheral(World world, int x, int y, int z,
			int side) {
		TileEntity te=world.getTileEntity(x, y,z);
		if(SimplePeripheral.valid(te))
			return new SimplePeripheral(te);
		else
			return null;
	}
	
}