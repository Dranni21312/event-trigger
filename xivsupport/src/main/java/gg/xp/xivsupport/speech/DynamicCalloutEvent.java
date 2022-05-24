package gg.xp.xivsupport.speech;

import gg.xp.reevent.events.BaseEvent;
import gg.xp.reevent.events.Event;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.function.Supplier;

public class DynamicCalloutEvent extends BaseEvent implements CalloutEvent {

	private final String callText;
	private final Supplier<String> visualText;
	private final Instant expiresAt;
	private final long hangTime;

	public DynamicCalloutEvent(String callText, Supplier<String> visualText, long hangTime) {
		this.callText = callText;
		this.visualText = visualText;
		this.hangTime = hangTime;
		expiresAt = Instant.now().plusMillis(hangTime);
	}

	@Override
	public @Nullable String getVisualText() {
		return visualText.get();
	}

	@Override
	public @Nullable String getCallText() {
		// TTS text does not need to be dynamic since it only happens once
		return callText;
	}

	@Override
	public boolean isExpired() {
		Event parent = getParent();
		if (parent instanceof BaseEvent be) {
			return be.getEffectiveTimeSince().toMillis() > hangTime;
		}
		return expiresAt.isBefore(Instant.now());
	}

	private @Nullable CalloutEvent replaces;

	@Override
	public @Nullable CalloutEvent replaces() {
		return replaces;
	}

	@Override
	public void setReplaces(CalloutEvent replaces) {
		this.replaces = replaces;
	}
}
