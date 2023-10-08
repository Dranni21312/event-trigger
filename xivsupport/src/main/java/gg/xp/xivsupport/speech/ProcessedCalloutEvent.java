package gg.xp.xivsupport.speech;

import gg.xp.xivsupport.callouts.CalloutTrackingKey;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.Serial;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class ProcessedCalloutEvent extends BaseCalloutEvent {

	@Serial
	private static final long serialVersionUID = 9186465196672495653L;
	private final String ttsText;
	private final Supplier<String> visualText;
	private final BooleanSupplier expired;
	private final Supplier<? extends @Nullable Component> guiProvider;
	private final @Nullable String soundFile;

	public ProcessedCalloutEvent(CalloutTrackingKey key, String ttsText, Supplier<String> visualText, BooleanSupplier expired, Supplier<? extends @Nullable Component> guiProvider, @Nullable Color colorOverride, @Nullable String soundFile) {
		super(key);
		this.ttsText = ttsText;
		this.visualText = visualText;
		this.expired = expired;
		this.guiProvider = guiProvider;
		this.soundFile = soundFile;
		super.setColorOverride(colorOverride);
	}

	@Override
	public @Nullable String getVisualText() {
		return visualText == null ? null : visualText.get();
	}

	@Override
	public @Nullable String getCallText() {
		return ttsText;
	}

	@Override
	public boolean isNaturallyExpired() {
		return expired.getAsBoolean();
	}

	@Override
	public @Nullable Component graphicalComponent() {
		return guiProvider.get();
	}

	@Override
	public @Nullable String getSound() {
		return soundFile;
	}

}
