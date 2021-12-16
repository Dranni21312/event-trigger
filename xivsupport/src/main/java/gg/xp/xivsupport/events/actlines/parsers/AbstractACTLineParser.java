package gg.xp.xivsupport.events.actlines.parsers;

import gg.xp.reevent.events.Event;
import gg.xp.reevent.events.EventContext;
import gg.xp.reevent.scan.HandleEvents;
import gg.xp.xivsupport.events.ACTLogLineEvent;
import gg.xp.xivsupport.events.state.RefreshSpecificCombatantsRequest;
import gg.xp.xivsupport.events.state.XivState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractACTLineParser<F extends Enum<F>> {

	private static final Logger log = LoggerFactory.getLogger(AbstractACTLineParser.class);

	private final Class<? extends Enum<F>> enumCls;
	private final int lineNumber;
	private final String lineStart;
	private final List<@Nullable F> groups;
	private final XivState state;
	private final boolean splitAll;

	AbstractACTLineParser(XivState state, int logLineNumber, Class<F> enumCls) {
		this(state, logLineNumber, enumCls, false);
	}
	AbstractACTLineParser(XivState state, int logLineNumber, Class<F> enumCls, boolean splitAll) {
		this(state, logLineNumber, Arrays.asList(enumCls.getEnumConstants()), splitAll);
	}

	@SuppressWarnings({"ConstantConditions", "unchecked"})
	AbstractACTLineParser(XivState state, int logLineNumber, List<@Nullable F> groups, boolean splitAll) {
		this.state = state;
		this.splitAll = splitAll;
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
	public void handle(EventContext context, ACTLogLineEvent event) {
		try {
			String line = event.getLogLine();
			if (line.startsWith(lineStart)) {
				String[] splits;
				if (splitAll) {
					splits = line.split("\\|");
				}
				else {
					int numSplits = groups.size() + 3;
					splits = line.split("\\|", numSplits);
				}
				Map<F, String> out = new EnumMap<>((Class<F>) enumCls);
				// TODO: validate number of fields
				for (int i = 0; i < groups.size(); i++) {
					// i + 2 is because the first two are the line number and timestamp.
					out.put(groups.get(i), splits[i + 2]);
				}
				ZonedDateTime zdt = ZonedDateTime.parse(splits[1]);
				FieldMapper<F> mapper = new FieldMapper<>(out, state, context, entityLookupMissBehavior(), splits);
				Event outgoingEvent;
				try {
					outgoingEvent = convert(mapper, lineNumber, zdt);
				}
				catch (Throwable t) {
					throw new IllegalArgumentException("Error parsing ACT line: " + line, t);
				}
				mapper.getCombatantsToUpdate().forEach(id -> {
					context.accept(new RefreshSpecificCombatantsRequest(List.of(id)));
				});
				if (mapper.isRecalcNeeded()) {
					state.flushProvidedValues();
				}
				if (outgoingEvent != null) {
					outgoingEvent.setHappenedAt(zdt.toInstant());
					context.accept(outgoingEvent);
				}
			}
		} catch (Throwable t) {
			throw new ActLineParseException(event.getLogLine(), t);
		}
	}

	// TODO: consider other ways of handling line number + timestamp
	protected abstract @Nullable Event convert(FieldMapper<F> fields, int lineNumber, ZonedDateTime time);

	protected EntityLookupMissBehavior entityLookupMissBehavior() {
		return EntityLookupMissBehavior.GET_AND_WARN;
	}
}
