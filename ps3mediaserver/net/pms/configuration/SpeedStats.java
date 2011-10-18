package net.pms.configuration;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapperImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Platform;

public class SpeedStats {
	private static SpeedStats instance = new SpeedStats();
	private static ExecutorService executor = Executors.newCachedThreadPool();
	public static SpeedStats getInstance() {
		return instance;
	}

	private static final Logger logger = LoggerFactory.getLogger(RendererConfiguration.class);

	private final Map<String, Future<Integer>> speedStats = new HashMap<String, Future<Integer>>();

	public synchronized Future<Integer> getSpeedInMBits(InetAddress addr) {
		Future<Integer> value = speedStats.get(addr.getHostAddress());
		if (value != null) {
			return value;
		}
		value = speedStats.get(addr.getCanonicalHostName());
		if (value != null) {
			return value;
		}
		value = executor.submit(new MeasureSpeed(addr));
		speedStats.put(addr.getHostAddress(), value);
		speedStats.put(addr.getCanonicalHostName(), value);

		return value;
	}

	class MeasureSpeed implements Callable<Integer> {
		InetAddress addr;

		public MeasureSpeed(InetAddress addr) {
			this.addr = addr;
		}

		@Override
		public Integer call() throws Exception {
			// let's get that speed
			OutputParams op = new OutputParams(null);
			op.log = true;
			op.maxBufferSize = 1;
			String count = Platform.isWindows() ? "-n" : "-c";
			String size = Platform.isWindows() ? "-l" : "-s";
			final ProcessWrapperImpl pw = new ProcessWrapperImpl(new String[] { "ping", count, "3", size, "64000", addr.getHostAddress() }, op,
					true, false);
			Runnable r = new Runnable() {
				public void run() {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
					}
					pw.stopProcess();
				}
			};
			Thread failsafe = new Thread(r);
			failsafe.start();
			pw.run();
			List<String> ls = pw.getOtherResults();
			int time = 0;
			int c = 0;
			for (String line : ls) {
				int msPos = line.indexOf("ms");
				try {
					if (msPos > -1) {
						String timeString = line.substring(line.lastIndexOf("=", msPos) + 1, msPos).trim();
						time += Double.parseDouble(timeString);
						c++;
					}
				} catch (Exception e) {
					// no big deal
				}
			}
			if (c > 0) {
				time = (int) (time / c);
			}
			if (time > 0) {
				int speedInMbits = (int) (1024 / time);
				logger.info("Address " + addr + " has an estimated network speed of: " + speedInMbits + " Mb/s");
				return speedInMbits;
			}
			return -1;
		}
	}

}
