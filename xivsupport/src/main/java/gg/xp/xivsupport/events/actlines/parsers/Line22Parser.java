package gg.xp.xivsupport.events.actlines.parsers;

import gg.xp.reevent.events.Event;
import gg.xp.xivsupport.events.state.XivState;
import org.picocontainer.PicoContainer;

import java.time.ZonedDateTime;

@SuppressWarnings("unused")
public class Line22Parser extends AbstractACTLineParser<NetworkAbilityFields> {

	public Line22Parser(PicoContainer container) {
		super(container, 22, NetworkAbilityFields.class, true);
	}

	@Override
	protected Event convert(FieldMapper<NetworkAbilityFields> fields, int lineNumber, ZonedDateTime time) {
		return NetworkAbilityFields.convert(fields);
	}
}
