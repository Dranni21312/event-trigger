package gg.xp.xivsupport.events.actlines.events;

import gg.xp.reevent.events.BaseEvent;
import gg.xp.reevent.events.SystemEvent;

import java.io.Serial;

// Going to mark this as a system event. Debug commands already get their own class, the rest
// of it seems to be mostly combat log spam.
@SystemEvent
public class ChatLineEvent extends BaseEvent {
	@Serial
	private static final long serialVersionUID = 4852530944045709474L;
	private final long code;
	private final String name;
	private final String line;

	public ChatLineEvent(long code, String name, String line) {
		this.code = code;
		this.name = name;
		this.line = line;
	}

	public long getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getLine() {
		return line;
	}
}
