package de.nubenum.app.plugin.logaggregator.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.nubenum.app.plugin.logaggregator.core.UpdateEvent.Event;

/**
 * A stoppable queue that will execute the queued tasks sequentially in a single
 * separate thread. Race Conditions are impossible. Listeners will be notified
 * if all tasks in the queue are completed, i.e. the queue is empty.
 *
 */
public class AsyncCompletableQueue implements IUpdateInitiator {
	private ExecutorService singleQueue = Executors.newSingleThreadExecutor();
	private List<Future<Void>> queueTasks = new ArrayList<>();
	private List<IUpdateListener> listeners = new ArrayList<>();

	/**
	 * Queue a new Runnable
	 *
	 * @param command
	 *            The Runnable to be executed
	 */
	public void addToQueue(Runnable command) {
		CompletableFuture<Void> task = CompletableFuture.runAsync(command, singleQueue);
		task.thenAccept(v -> onFinish());
		queueTasks.add(task);
	}

	private synchronized void onFinish() {
		boolean finished = true;
		for (Future<Void> t : queueTasks) {
			if (!t.isDone())
				finished = false;
		}
		if (finished) {
			listeners.forEach(l -> l.onUpdate(new UpdateEvent(Event.STOP)));
		}
	}

	/**
	 * Remove all pending tasks and try to stop the execution of the running task
	 */
	public void stop() {
		for (Future<Void> t : queueTasks) {
			if (!t.isDone())
				t.cancel(true);
		}
		queueTasks.clear();
	}

	@Override
	public void addListener(IUpdateListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(IUpdateListener listener) {
		listeners.remove(listener);
	}
}
