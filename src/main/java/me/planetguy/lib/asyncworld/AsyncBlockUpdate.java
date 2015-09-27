package me.planetguy.lib.asyncworld;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

//TODO make it actually work
public class AsyncBlockUpdate {
	
	public static class TickEntry implements Comparable<TickEntry>, Callable{

		private final int timeInTicks;
		private final World w;
		private final int x;
		private final int y;
		private final int z;
		
		@Override
		public int compareTo(TickEntry a) {
			return timeInTicks-a.timeInTicks;
		}
		
		public TickEntry(World w, int x, int y, int z, int timeFromNow) {
			this.timeInTicks=timeFromNow+tick;
			this.w=w;
			this.x=x;
			this.y=y;
			this.z=z;
		}

		@Override
		public Object call() throws Exception {
			try {
				//we must lock our chunk here since we didn't sort by chunks in 
				synchronized(AsyncWorld.getChunk(w, x, z)) {
					((IAsyncTickable)AsyncWorld.getBlock(w, x, y, z)).onAsyncLocalUpdate(w, x, y, z);
				}
			}catch(ClassCastException ignored) {
				// try/catch is probably faster than checked cast; we assume that no one improperly posts updates
			}
			return null;
		}

	}
	
	private static ExecutorService theThreadPool=Executors.newCachedThreadPool();
	
	//monotonically increases forever, but not valid across games or sessions - persist ticks in relative formats
	private static int tick=0;
	
	private static Map<World, Queue<TickEntry>[]> globalScheduledTicks=new HashMap<World, Queue<TickEntry>[]>();
	
	public static void onTick(final TickEvent.WorldTickEvent e) {
		if(e.phase==Phase.START) {
			Queue<TickEntry>[] entries=getOrCreateQueue(e.world);
			for(Queue<TickEntry> scheduledTicks:entries) { 
				//go in serial over these
				//this provides the local locking guaranteed in IAsyncTickable
				
				List<Callable<Void>> tasks=new ArrayList<Callable<Void>>();
				TickEntry te=scheduledTicks.peek();
				while(te.timeInTicks<=tick) {
					tasks.add(scheduledTicks.remove());
					te=scheduledTicks.peek();
				}
				try {
					theThreadPool.invokeAll(tasks);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		} else { //not Phase.START -> Phase.END
			tick++;
		}
	}
	
	private static Queue<TickEntry>[] getOrCreateQueue(World w){
		Queue<TickEntry>[] entries=globalScheduledTicks.get(w);
		if(entries==null) { //first async tick scheduled
			entries=new Queue[9];
			for(int i=0; i<entries.length; i++) {
				entries[i]=new ArrayDeque<TickEntry>();
			}
			globalScheduledTicks.put(w, entries);
		}
		return entries;
	}
	
	/*
	 * When scheduled, ticks are placed in one of 9 buckets, such that ticks within the same bucket are either in the same 
	 * chunk as, or more than 16 blocks from, any other tick in that bucket.
	 */
	public static int getRegionFor(int x, int z) {
		return (x>>4) % 3 + ((z>>4) % 3)*3;
	}
	
	public static void scheduleAsyncTick(World w, int x, int y, int z, int delay) {
		int regionToScheduleIn=(x>>4) % 3 + 3*((z>>4) % 3);
		getOrCreateQueue(w)[regionToScheduleIn].add(new TickEntry(w,x,y,z,delay));
	}

}
