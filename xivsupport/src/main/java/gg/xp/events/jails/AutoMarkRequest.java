package gg.xp.events.jails;

import gg.xp.events.BaseEvent;
import gg.xp.events.models.XivPlayerCharacter;

public class AutoMarkRequest extends BaseEvent {

	private static final long serialVersionUID = -915091489094353125L;
	private final XivPlayerCharacter playerToMark;
	@SuppressWarnings({"unused", "FieldCanBeLocal"}) // for debugging
	private int resolvedPartySlot;

	public AutoMarkRequest(XivPlayerCharacter playerToMark) {
		this.playerToMark = playerToMark;
	}

	public XivPlayerCharacter getPlayerToMark() {
		return playerToMark;
	}

	public void setResolvedPartySlot(int resolvedPartySlot) {
		this.resolvedPartySlot = resolvedPartySlot;
	}
}
