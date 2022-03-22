package gg.xp.xivsupport.timelines;

import gg.xp.xivsupport.events.triggers.jobs.gui.LabelOverride;
import gg.xp.xivsupport.models.CurrentMaxPair;

@SuppressWarnings("NumericCastThatLosesPrecision")
public record VisualTimelineEntry(
		TimelineEntry originalTimelineEntry,
		boolean isCurrentSync,
		double timeUntil
) implements LabelOverride, CurrentMaxPair {
	@Override
	public String getLabel() {
		return String.format("%s%s", originalTimelineEntry.name(), isCurrentSync ? "*" : "");
	}

	public boolean shouldDisplay() {
		return originalTimelineEntry.name() != null;
	}

	@Override
	public long getCurrent() {
		if (remainingActiveTime() > 0) {
			return (long) (remainingActiveTime() * 1000.0);
		}
		return (long) Math.min(60_000.0d - (1000.0 * timeUntil), 60_000.0d);
	}

	@Override
	public long getMax() {
		if (remainingActiveTime() > 0) {
			//noinspection ConstantConditions - known to be non-null if remaining active time is > 0
			return (long) (originalTimelineEntry.duration() * 1000);
		}
		return 60_000L;
	}

	public double remainingActiveTime() {
		Double timelineDuration = originalTimelineEntry.duration();
		if (timelineDuration == null) {
			return 0;
		}
		if (timeUntil > 0) {
			// Not active yet
			return 0;
		}
		// timeUntil will be negative, so we want to add.
		return Math.min(timelineDuration, Math.max(0, timelineDuration + timeUntil));
	}
}
