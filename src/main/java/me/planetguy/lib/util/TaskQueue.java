package me.planetguy.lib.util;

public class TaskQueue<T> {
	
	private T[] tasks=(T[]) new Object[10];
	
	private int partitionStart=0;
	
	private int tasksStart=0;
	
	private int tasksEnd=0;
	
	public void insert(T t, int delay) {
		
	}
	
	//doubles the size of the queue
	private void resize() {
		T[] oldTasks=tasks;
		tasks=(T[]) new Object[tasks.length*2];
		for(int i=tasksStart; i<tasksEnd+oldTasks.length; i++) {
			tasks[index(i)]=oldTasks[i%oldTasks.length];
		}
	}
	
	private int index(int i) {
		return i%tasks.length;
	}
	
	private T get(int i) {
		return tasks[index(i)];
	}
	
}
