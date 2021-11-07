package gg.xp.events.actlines;

import gg.xp.events.BaseEvent;
import gg.xp.events.models.XivZone;

public class ZoneChangeEvent extends BaseEvent {
	private final XivZone zone;

	public ZoneChangeEvent(XivZone zone) {
		this.zone = zone;
	}

	public XivZone getZone() {
		return zone;
	}
}
