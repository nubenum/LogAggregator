package de.nubenum.app.plugin.logaggregator.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;

/**
 * Executes multiple long-running tasks for obtaining IEntries simultaneously
 * with a fixed number of threads.
 *
 */
public class AsyncEntryRetriever {
	private ExecutorService batch;
	private List<Callable<IEntry>> tasks = new ArrayList<>();

	/**
	 * A new instance with a fixed number of threads. The class is not thread-safe,
	 * i.e. should be used by only one client sequentially.
	 *
	 * @param threadNum
	 *            The number of threads to create. This should ideally coincide with
	 *            the number of Tasks that will be submitted in a single run. If 0,
	 *            Tasks will be executed in the same thread using
	 *            {@link #getSynchroneously()}
	 */
	public AsyncEntryRetriever(int threadNum) {
		if (threadNum > 0)
			batch = Executors.newFixedThreadPool(threadNum);
	}

	/**
	 * Add a new Callable to obtain an IEntry. This will never execute unless
	 * {@link #get() or #getSynchroneously()} are called
	 *
	 * @param task
	 *            The Callable containing a long-running logic to obtain an IEntry
	 */
	public void add(Callable<IEntry> task) {
		tasks.add(task);
	}

	/**
	 * Clear the list of tasks before adding a new batch of Tasks
	 */
	public void clear() {
		tasks.clear();
	}

	/**
	 * Stop all threads
	 */
	public void stop() {
		if (batch != null)
			batch.shutdownNow();
	}

	/**
	 * Run the Callables that were added before with all available threads and
	 * collect the returned {@link IEntry}s .
	 *
	 * @return The collected IEntries from the Callables upon completion of all
	 *         Callables.
	 * @throws IOException
	 *             If a Callable throws an IOException. All other exceptions will be
	 *             caught, logged and otherwise ignored.
	 * @throws InterruptedException
	 */
	public List<IEntry> get() throws IOException, InterruptedException {
		if (batch == null)
			return getSynchroneously();
		List<Future<IEntry>> futures = batch.invokeAll(tasks);
		List<IEntry> entries = new ArrayList<>();
		for (Future<IEntry> future : futures) {
			try {
				entries.add(future.get());
			} catch (ExecutionException e) {
				handleException(e.getCause());
			}
		}
		return entries;
	}

	/**
	 * Run the added Callables in the same thread.
	 *
	 * @return The collected IEntries.
	 * @throws IOException
	 *             If a Callable throws an IOException. All other exceptions will be
	 *             caught, logged and otherwise ignored.
	 * @throws InterruptedException
	 */
	public List<IEntry> getSynchroneously() throws IOException, InterruptedException {
		List<IEntry> results = new ArrayList<>();

		for (Callable<IEntry> task : tasks) {
			try {
				results.add(task.call());
			} catch (Exception e) {
				handleException(e);
			}
		}
		return results;
	}

	private void handleException(Throwable e) throws IOException, InterruptedException {
		if (e instanceof IOException)
			throw (IOException) e;
		if (e instanceof InterruptedException)
			throw (InterruptedException) e;
		else
			SystemLog.log(e);
	}
}
