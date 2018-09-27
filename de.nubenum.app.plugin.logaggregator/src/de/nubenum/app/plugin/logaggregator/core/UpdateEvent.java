package de.nubenum.app.plugin.logaggregator.core;

import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;

/**
 * An event created by IUpdateInitiator carrying a variety of possible event
 * types and payload.
 *
 */
public class UpdateEvent {
	public static enum Event {
		/**
		 * Used when processing starts.
		 */
		START,
		/**
		 * Used when processing is completed and/or files are loaded.
		 */
		STOP,
		/**
		 * Used to update an Entry (as payload).
		 */
		ENTRY,
		/**
		 * Used to update the number of lines read.
		 */
		COUNT,
		/**
		 * Used to update the byte count of opened files.
		 */
		SIZE,
		/**
		 * Used to hand over user-faced exceptions.
		 */
		EXCEPTION,
		/**
		 * Used to trigger a refresh of all data.
		 */
		REFRESH,
		/**
		 * Used to trigger a refresh of the entries when new ones were appended.
		 */
		APPEND
	}

	private Event type;
	private Exception exception = null;
	private IEntry entry = null;
	private Integer num = null;

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

	public UpdateEvent(Event type, Integer num) {
		this.type = type;
		this.num = num;
	}

	public Exception getException() {
		return exception;
	}

	public IEntry getEntry() {
		return entry;
	}

	public Integer getNum() {
		return num;
	}

	public Event getType() {
		return type;
	}
}
