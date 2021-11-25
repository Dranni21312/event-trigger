package gg.xp.xivsupport.eventstorage;

import gg.xp.reevent.events.Event;

import java.io.EOFException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class EventReader {

	public static List<Event> readEventsFromResource(String resourcePath) {
		InputStream stream = EventReader.class.getResourceAsStream(resourcePath);
		List<Event> events;
		try (ObjectInputStream ois = new ObjectInputStream(stream)) {
			// TODO: security
			ObjectInputFilter filter = filterInfo -> {
				if (filterInfo.serialClass() == null) {
					return ObjectInputFilter.Status.ALLOWED;
				}
				if (filterInfo.depth() != 1 || Event.class.isAssignableFrom(filterInfo.serialClass())) {
					return ObjectInputFilter.Status.ALLOWED;
				}
				else {
					return ObjectInputFilter.Status.REJECTED;
				}
			};
			ois.setObjectInputFilter(filter);
			events = new ArrayList<>();
			try {
				while (true) {
					Event event = (Event) ois.readObject();
					event.setImported(true);
					events.add(event);
				}
			}
			catch (EOFException eof) {
				// done reading
			}
		}
		catch (Throwable e) {
			throw new RuntimeException("Error reading events", e);
		}
		return events;
	}
}
