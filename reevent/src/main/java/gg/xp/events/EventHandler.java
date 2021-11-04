package gg.xp.events;

public interface EventHandler<X extends Event> {
	void handle(EventContext<Event> context, X event);
}
