package gg.xp.xivsupport.timelines;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gg.xp.xivdata.data.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.Objects;
import java.util.regex.Pattern;

public interface TimelineEntry extends Comparable<TimelineEntry> {

	@JsonIgnore
	default double getMinTime() {
		return time() - timelineWindow().start();
	}

	@JsonIgnore
	default double getMaxTime() {
		return time() + timelineWindow().end();
	}


	default boolean shouldSync(double currentTime, String line) {
		Pattern sync = sync();
		if (sync == null) {
			return false;
		}
		return currentTime >= getMinTime() && currentTime <= getMaxTime() && sync.matcher(line).find();
	}

	@JsonIgnore
	default double getSyncToTime() {
		Double jump = jump();
		if (jump == null) {
			return time();
		}
		else {
			return jump;
		}

	}

	@Override
	@Nullable String toString();

	double time();

	@Nullable String name();

	@Nullable Pattern sync();

	@Nullable Double duration();

	@NotNull TimelineWindow timelineWindow();

	@Nullable Double jump();

	boolean enabled();

	@Override
	default int compareTo(@NotNull TimelineEntry o) {
		return Double.compare(this.time(), o.time());
	}

	default @Nullable URL icon() {
		return null;
	}

	default @Nullable TimelineReference replaces() {
		return null;
	}

	default boolean shouldSupersede(TimelineEntry that) {
		TimelineReference overrides = this.replaces();
		if (overrides == null) {
			return false;
		}
		// To allow users to switch languages freely without losing their overrides,
		// compare to the untranslated versions.
		that = that.untranslated();
		// Rules:
		// 1. Time must match
		// 2. Name must match
		// 3. Sync must match
		// 4. For backwards compatibility, treat replacing an empty/null pattern as always matching for step 2
		String desiredName = overrides.name();
		//noinspection FloatingPointEquality
		if (overrides.time() == that.time()) {
			String thatPattern = that.sync() == null ? null : that.sync().pattern();
			if (!Objects.equals(desiredName, that.name())) {
				return false;
			}
			if (overrides.pattern() == null || overrides.pattern().isBlank()) {
				return true;
			}
			return (Objects.equals(overrides.pattern(), thatPattern));
		}
		return false;
	}

	boolean callout();

	double calloutPreTime();

	default double effectiveCalloutTime() {
		return time() - calloutPreTime();
	}

	default boolean enabledForJob(Job job) {
		return true;
	}

	default TimelineEntry untranslated() {
		return this;
	}
}
