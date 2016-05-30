package me.planetguy.lib.util;

import java.util.Iterator;

public class SingleIterator<T> implements Iterator<T>, Iterable<T> {

	private T	object;

	public SingleIterator(T o) {
		object = o;
	}

	@Override
	public boolean hasNext() {
		return object != null;
	}

	@Override
	public T next() {
		T o = object;
		object = null;
		return o;
	}

	@Override
	public void remove() {
		object = null;
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}

}
