package gg.xp.xivsupport.events.actlines.events;

import gg.xp.reevent.events.BaseEvent;
import gg.xp.reevent.events.SystemEvent;
import gg.xp.xivsupport.events.models.XivCombatant;

/**
 * Equivalent to an ACT 03 line. This event is intentionally left pretty bare, because
 * you should get all of the info from XivState.
 */
@SystemEvent
public class RawAddCombatantEvent extends BaseEvent {
	private static final long serialVersionUID = 3615674340829313314L;
	private final XivCombatant entity;

	public RawAddCombatantEvent(XivCombatant entity) {
		this.entity = entity;
	}

	public XivCombatant getEntity() {
		return entity;
	}

}
