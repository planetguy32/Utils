package me.planetguy.core.cc;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.Optional.Interface;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;

@Optional.Interface(modid="ComputerCraft", iface = "dan200.computercraft.api.peripheral.IPeripheralProvider")
public class ODTPeripheral implements IPeripheralProvider {
	
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