
package com.bitfire.uracer.events;

public abstract class Event<T> {
	public T source;

	public Event (T source) {
		this.source = source;
	}
}