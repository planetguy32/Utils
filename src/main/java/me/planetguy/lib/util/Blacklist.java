package me.planetguy.lib.util;

import net.minecraft.block.Block;
import net.minecraft.world.World;

public class Blacklist {

	private java.util.HashSet<Block>		BlacklistedIds				= new java.util.HashSet<Block>();

	private java.util.HashSet<BlockInt>	BlacklistedIdAndMetaPairs	= new java.util.HashSet<BlockInt>();

	public void blacklist(Block Id) {
		BlacklistedIds.add(Id);
	}

	public void blacklist(Block Id, int Meta) {
		BlacklistedIdAndMetaPairs.add(new BlockInt(Id, Meta));
	}

	public boolean lookup(World w, int x, int y, int z) {

		int meta = w.getBlockMetadata(x, y, z);
		Block block = w.getBlock(x, y, z);
		if (BlacklistedIds.contains(block) || BlacklistedIdAndMetaPairs.contains(new BlockInt(block, meta))) {
			return true;
		} else {
			return false;
		}
	}

	private class BlockInt {

		public final Block	block;
		public final int	meta;

		public BlockInt(Block b, int i) {
			block = b;
			meta = i;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof BlockInt) {
				return ((BlockInt) o).block == block && ((BlockInt) o).meta == meta;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return block.hashCode() ^ meta;
		}
	}
}
