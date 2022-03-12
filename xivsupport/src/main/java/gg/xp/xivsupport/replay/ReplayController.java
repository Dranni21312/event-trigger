package gg.xp.xivsupport.replay;

import gg.xp.reevent.events.Event;
import gg.xp.reevent.events.EventMaster;
import gg.xp.xivsupport.persistence.Compressible;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class ReplayController {

	private static final Logger log = LoggerFactory.getLogger(ReplayController.class);

	private final ExecutorService exs = Executors.newSingleThreadExecutor(new BasicThreadFactory.Builder()
			.daemon(true)
			.namingPattern("ReplayThread-%d")
			.priority(Thread.MIN_PRIORITY)
			.build()
	);

	private final EventMaster master;
	private final List<? extends Event> events;
	private final List<Runnable> callbacks = new ArrayList<>();
	private final boolean decompress;
	private int currentIndex;


	public ReplayController(EventMaster master, List<? extends Event> events, boolean decompress) {
//		master.setUseLoopLock(true);
		this.master = master;
		this.events = events;
		this.decompress = decompress;
	}

	public int getCount() {
		return events.size();
	}

	public int getCurrentPosition() {
		return currentIndex;
	}

	public void addCallback(Runnable r) {
		callbacks.add(r);
	}

	private void notifyCallbacks() {
		try {
			callbacks.forEach(Runnable::run);
		}
		catch (Throwable t) {
			log.error("Error running callbacks", t);
		}
	}


	/**
	 * Advance by a certain number of events. While this is synchronous in the sense that it will not
	 * return until the events have been enqueued, it is not asynchronous in the sense that events are
	 * merely enqueued rather than processed immediately.
	 *
	 * @param count Count to advance by
	 * @return Actual count advanced by, which will be less than 'count' if there were less than 'count'
	 * events remaining in the replay.
	 */
	public int advanceBy(int count) {
		int advancedBy = 0;
		for (; count-- > 0 && currentIndex < events.size(); currentIndex++) {
			Event event = events.get(currentIndex);
			if (decompress && event instanceof Compressible compressedEvent) {
				compressedEvent.decompress();
			}
			master.pushEvent(event);
			advancedBy++;
		}
		notifyCallbacks();
		return advancedBy;
	}

	public void advanceByAsync(int count) {
		exs.submit(() -> {
			advanceBy(count);
			notifyCallbacks();
		});
	}

	public void advanceByAsyncWhile(Supplier<Boolean> advWhile) {
		exs.submit(() -> {
			for (; advWhile.get() && currentIndex < events.size(); currentIndex++) {
				Event event = events.get(currentIndex);
				if (decompress && event instanceof Compressible compressedEvent) {
					compressedEvent.decompress();
				}
				master.pushEventAndWait(event);
			}
			notifyCallbacks();
		});
	}
}
