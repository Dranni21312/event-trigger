package gg.xp.events.actlines;

import gg.xp.events.ACTLogLineEvent;
import gg.xp.events.Event;
import gg.xp.events.EventContext;
import gg.xp.scan.HandleEvents;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractACTLineParser<F extends Enum<F>> {

	private final Class<? extends Enum<F>> enumCls;
	private final int lineNumber;
	private final String lineStart;
	private final List<@Nullable F> groups;

	AbstractACTLineParser(int logLineNumber, Class<F> enumCls) {
		this(logLineNumber, Arrays.asList(enumCls.getEnumConstants()));
	}

	@SuppressWarnings({"ConstantConditions", "unchecked"})
	AbstractACTLineParser(int logLineNumber, List<@Nullable F> groups) {
		if (groups.isEmpty()) {
			// TODO: could some of them make sense as empty?
			throw new IllegalArgumentException("Capture groups cannot be empty");
		}
		this.groups = new ArrayList<>(groups);
		F anyCap = groups.stream()
				.filter(Objects::nonNull)
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Must have a non-null capture group"));
		enumCls = (Class<? extends Enum<F>>) anyCap.getClass();
		lineNumber = logLineNumber;
		lineStart = String.format("%02d", logLineNumber) + '|';
	}

	@SuppressWarnings("unchecked")
	@HandleEvents
	public void handle(EventContext<Event> context, ACTLogLineEvent event) {
		String line = event.getLogLine();
		if (line.startsWith(lineStart)) {
			int numSplits = groups.size() + 3;

			String[] splits = line.split("\\|", numSplits);
			Map<F, String> out = new EnumMap<>((Class<F>) enumCls);
			for (int i = 0; i < groups.size(); i++) {
				// i + 2 is because the first two are the line number and timestamp.
				out.put(groups.get(i), splits[i + 2]);
			}
			ZonedDateTime zdt = ZonedDateTime.parse(splits[1]);
			Event outgoingEvent = convert(out, lineNumber, zdt);
			context.accept(outgoingEvent);
		}
	}

	// TODO: consider other ways of handling line number + timestamp
	protected abstract Event convert(Map<F, String> fields, int lineNumber, ZonedDateTime time);
}
