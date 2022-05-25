package gg.xp.xivsupport.speech;

import gg.xp.reevent.events.BaseEvent;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.Serial;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ParentedCalloutEvent<X> extends BaseEvent implements CalloutEvent {

	@Serial
	private static final long serialVersionUID = 6842512228516345067L;
	private final X event;
	private final String callText;
	private final Supplier<String> visualText;
	private final Predicate<X> expiryCheck;
	private final Function<X, Component> guiFunction;

	public ParentedCalloutEvent(X event, String callText, Supplier<String> visualText, Predicate<X> expiryCheck, Function<X, Component> guiFunction) {
		this.event = event;
		this.callText = callText;
		this.visualText = visualText;
		this.expiryCheck = expiryCheck;
		this.guiFunction = guiFunction;
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
		return expiryCheck.test(event);
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

	@Override
	public @Nullable Component graphicalComponent() {
		return guiFunction.apply(event);
	}
}
