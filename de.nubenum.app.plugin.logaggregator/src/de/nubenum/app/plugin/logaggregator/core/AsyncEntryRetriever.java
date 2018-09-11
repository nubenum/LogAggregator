package de.nubenum.app.plugin.logaggregator.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.nubenum.app.plugin.logaggregator.core.model.IEntry;

public class AsyncEntryRetriever {
	private ExecutorService batch;
	private List<Callable<IEntry>> tasks = new ArrayList<>();

	public AsyncEntryRetriever(int threadNum) {
		batch = Executors.newFixedThreadPool(threadNum);
	}

	public void add(Callable<IEntry> task) {
		tasks.add(task);
	}

	public void clear() {
		tasks.clear();
	}

	public List<IEntry> get() throws IOException {
		try {
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
		} catch (InterruptedException e) {
			clear();

			return new ArrayList<IEntry>();
		}

	}

	public List<IEntry> getSynchroneously() throws IOException {
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

	private void handleException(Throwable e) throws IOException {
		if (e instanceof IOException)
			throw (IOException) e;
		else {
			SystemLog.log(e);
		}
	}
}
