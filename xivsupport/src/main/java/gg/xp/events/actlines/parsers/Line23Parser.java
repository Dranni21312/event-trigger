package gg.xp.events.actlines.parsers;

import gg.xp.events.Event;
import gg.xp.events.actlines.events.AbilityCastCancel;
import gg.xp.events.models.XivAbility;
import gg.xp.events.models.XivEntity;

import java.time.ZonedDateTime;

@SuppressWarnings("unused")
public class Line23Parser extends AbstractACTLineParser<Line23Parser.Fields> {

	public Line23Parser() {
		super(23, Fields.class);
	}

	enum Fields {
		casterId, casterName, abilityId, abilityName, reason
	}

	@Override
	protected Event convert(FieldMapper<Fields> fields, int lineNumber, ZonedDateTime time) {
		return new AbilityCastCancel(
				fields.getEntity(Fields.casterId, Fields.casterName),
				fields.getAbility(Fields.abilityId, Fields.abilityName),
				fields.getString(Fields.reason)
		);
	}
}
