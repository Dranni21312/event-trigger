package gg.xp.xivsupport.events.actlines.events;

import gg.xp.reevent.events.BaseEvent;
import gg.xp.xivsupport.events.triggers.marks.adv.MarkerSign;
import gg.xp.xivsupport.models.XivCombatant;

import java.io.Serial;

public class PlayerMarkerRemovedEvent extends BaseEvent implements HasSourceEntity, HasTargetEntity, HasPrimaryValue {

	@Serial
	private static final long serialVersionUID = 1170627458816079017L;
	private final MarkerSign marker;
	private final XivCombatant source;
	private final XivCombatant target;

	public PlayerMarkerRemovedEvent(MarkerSign marker, XivCombatant source, XivCombatant target) {
		this.marker = marker;
		this.source = source;
		this.target = target;
	}

	public MarkerSign getMarker() {
		return marker;
	}

	@Override
	public XivCombatant getSource() {
		return source;
	}

	@Override
	public XivCombatant getTarget() {
		return target;
	}

	@Override
	public String getPrimaryValue() {
		return "Removed " + marker.getFriendlyName();
	}
}
