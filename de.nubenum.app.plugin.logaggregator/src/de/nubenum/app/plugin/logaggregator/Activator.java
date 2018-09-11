package de.nubenum.app.plugin.logaggregator;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	public static void warn(String msg) {
		Platform.getLog(context.getBundle()).log(new Status(Status.WARNING, context.getBundle().getSymbolicName(), msg));
	}

	public static void log(String msg) {
		Platform.getLog(context.getBundle()).log(new Status(Status.INFO, context.getBundle().getSymbolicName(), msg));
	}

	public static void log(Throwable exc) {
		Platform.getLog(context.getBundle()).log(new Status(Status.ERROR, context.getBundle().getSymbolicName(), exc.getMessage(), exc));
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
