package gg.xp.xivsupport.events.state;

import gg.xp.reevent.events.EventContext;
import gg.xp.reevent.scan.HandleEvents;
import gg.xp.xivsupport.events.actlines.events.MapChangeEvent;
import gg.xp.xivsupport.events.actlines.events.RawAddCombatantEvent;
import gg.xp.xivsupport.events.actlines.events.RawPlayerChangeEvent;
import gg.xp.xivsupport.events.actlines.events.RawRemoveCombatantEvent;
import gg.xp.xivsupport.events.actlines.events.ZoneChangeEvent;

import java.util.Collections;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
public final class TrackStateChanges {

	@HandleEvents(order = Integer.MIN_VALUE)
	public static void zoneChange(EventContext context, ZoneChangeEvent event) {
		context.getStateInfo().get(XivState.class).setZone(event.getZone());
		context.accept(new RefreshCombatantsRequest());
	}

	@HandleEvents(order = Integer.MIN_VALUE)
	public static void mapChange(EventContext context, MapChangeEvent event) {
		context.getStateInfo().get(XivState.class).setMap(event.getMap());
		context.accept(new RefreshCombatantsRequest());
	}

	@HandleEvents(order = Integer.MIN_VALUE)
	public static void playerChange(EventContext context, RawPlayerChangeEvent event) {
		context.getStateInfo().get(XivState.class).setPlayer(event.getPlayer());
		// After learning about the player, make sure we request combatant data
		context.accept(new RefreshCombatantsRequest());
	}

	@HandleEvents(order = Integer.MIN_VALUE)
	public static void combatantAdded(EventContext context, RawAddCombatantEvent event) {
		context.accept(new RefreshSpecificCombatantsRequest(Collections.singletonList(event.getEntity().getId())));
	}

	@HandleEvents(order = Integer.MIN_VALUE)
	public static void combatantRemoved(EventContext context, RawRemoveCombatantEvent event) {
		context.accept(new RefreshCombatantsRequest());
	}

	@HandleEvents(order = Integer.MIN_VALUE)
	public static void partyChange(EventContext context, PartyChangeEvent event) {
		context.getStateInfo().get(XivState.class).setPartyList(event.getMembers());
	}

	@HandleEvents(order = Integer.MIN_VALUE)
	public static void combatants(EventContext context, CombatantsUpdateRaw event) {
		if (event.isFullRefresh()) {
			context.getStateInfo().get(XivState.class).setCombatants(event.getCombatantMaps());
		}
		else {
			context.getStateInfo().get(XivState.class).setSpecificCombatants(event.getCombatantMaps());
		}
	}

}
