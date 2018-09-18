package de.nubenum.app.plugin.logaggregator.core;

import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;

public class UpdateEvent {
	public static enum Event {
		START,
		STOP,
		ENTRY,
		COUNT,
		EXCEPTION,
		REFRESH,
		APPEND
	}
	private Event type;
	private Exception exception = null;
	private IEntry entry = null;
	private Integer count = null;

	public UpdateEvent(Event type) {
		this.type = type;
	}

	public UpdateEvent(Exception exception) {
		this.type = Event.EXCEPTION;
		this.exception = exception;
	}

	public UpdateEvent(IEntry entry) {
		this.type = Event.ENTRY;
		this.entry = entry;
	}

	public UpdateEvent(Integer count) {
		this.type = Event.COUNT;
		this.count = count;
	}

	public Exception getException() {
		return exception;
	}

	public IEntry getEntry() {
		return entry;
	}

	public Integer getCount() {
		return count;
	}

	public Event getType() {
		return type;
	}
}
