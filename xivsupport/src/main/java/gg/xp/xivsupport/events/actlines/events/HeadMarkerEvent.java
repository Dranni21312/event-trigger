package gg.xp.xivsupport.events.actlines.events;

import gg.xp.reevent.events.BaseEvent;
import gg.xp.xivsupport.models.XivCombatant;

public class HeadMarkerEvent extends BaseEvent implements HasTargetEntity {

	private static final long serialVersionUID = -413687601479469145L;
	private final XivCombatant target;
	private final long markerId;

	public HeadMarkerEvent(XivCombatant target, long markerId) {
		this.target = target;
		this.markerId = markerId;
	}

	@Override
	public XivCombatant getTarget() {
		return target;
	}

	public long getMarkerId() {
		return markerId;
	}
}
