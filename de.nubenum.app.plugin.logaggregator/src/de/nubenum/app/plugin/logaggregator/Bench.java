package de.nubenum.app.plugin.logaggregator;

public class Bench {
	private long start;
	private String tag;
	private StringBuilder str;
	private static int c;
	public Bench(String tag) {
		c++;
		this.tag = tag;

		str = new StringBuilder();
		start = System.nanoTime();
	}
	public void stop(String tag) {
		str.append((System.nanoTime()-start)+" "+this.tag+c+tag+"\n");
		start = System.nanoTime();
	}
	public void stop() {
		stop("");
	}
	public void print(boolean force) {
		if (!force) return;
		stop();
		System.out.println(str.toString());
		str = new StringBuilder();
	}

	public void print() {
		print(true);
	}
}
