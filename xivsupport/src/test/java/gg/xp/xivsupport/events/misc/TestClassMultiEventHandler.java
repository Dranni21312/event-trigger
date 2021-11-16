package gg.xp.xivsupport.events.misc;

import gg.xp.reevent.events.Event;
import gg.xp.reevent.events.EventContext;
import gg.xp.reevent.scan.HandleEvents;
import gg.xp.xivsupport.events.ACTLogLineEvent;
import gg.xp.xivsupport.events.DiagEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestClassMultiEventHandler {

	private static final Logger log = LoggerFactory.getLogger(TestClassMultiEventHandler.class);

	private final int source = System.identityHashCode(this);

	@HandleEvents
	public void firstHandler(EventContext<Event> context, ACTLogLineEvent logLineEvent) {
		if (logLineEvent.getLogLine().equals("Stuff")) {
			context.accept(new DiagEvent("Foo", source));
		}
	}

	@HandleEvents
	public void secondHandler(EventContext<Event> context, ACTLogLineEvent logLineEvent) {
		if (logLineEvent.getLogLine().equals("Stuff")) {
			context.accept(new DiagEvent("Bar", source));
		}
	}

	@HandleEvents
	public void diagHandler(EventContext<Event> context, DiagEvent event) {
		log.info("Received diag event from {}: {}", event.getSource(), event.getText());
	}
}
