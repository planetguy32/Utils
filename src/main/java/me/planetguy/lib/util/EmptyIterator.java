package me.planetguy.lib.util;

import java.util.Iterator;

public class EmptyIterator<T> implements Iterator<T>, Iterable<T> {

	public static EmptyIterator	instance	= new EmptyIterator();

	private EmptyIterator() {};

	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public T next() {
		throw new RuntimeException("Tried to access element of empty iterator!");
	}

	@Override
	public void remove() {}

	@Override
	public Iterator<T> iterator() {
		return this;
	}

}
