package gg.xp.events.actlines.parsers;

import gg.xp.events.Event;
import gg.xp.events.EventContext;
import gg.xp.events.models.XivAbility;
import gg.xp.events.models.XivCombatant;
import gg.xp.events.models.XivStatusEffect;
import gg.xp.events.state.XivState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;

public class FieldMapper<K extends Enum<K>> {

	private static final Logger log = LoggerFactory.getLogger(FieldMapper.class);

	private final Map<K, String> raw;
	private final EventContext<Event> context;
	private final boolean ignoreEntityLookupMiss;

	public FieldMapper(Map<K, String> raw, EventContext<Event> context, boolean ignoreEntityLookupMiss) {
		this.raw = new EnumMap<>(raw);
		this.context = context;
		this.ignoreEntityLookupMiss = ignoreEntityLookupMiss;
	}

	public String getString(K key) {
		return raw.get(key);
	}

	public long getLong(K key) {
		return Long.parseLong(raw.get(key), 10);
	}

	public long getHex(K key) {
		return Long.parseLong(raw.get(key), 16);
	}

	public double getDouble(K key) {
		return Double.parseDouble(raw.get(key));
	}

	public XivAbility getAbility(K idKey, K nameKey) {
		long id = getHex(idKey);
		String name = getString(nameKey);
		return new XivAbility(id, name);
	}

	public XivStatusEffect getStatus(K idKey, K nameKey) {
		long id = getHex(idKey);
		String name = getString(nameKey);
		return new XivStatusEffect(id, name);
	}
	public XivCombatant getEntity(K idKey, K nameKey) {
		long id = getHex(idKey);
		String name = getString(nameKey);
		XivState xivState = context.getStateInfo().get(XivState.class);
		XivCombatant xivCombatant = xivState.getCombatants().get(id);
		if (xivCombatant != null) {
			return xivCombatant;
		}
		else {
			if (!ignoreEntityLookupMiss) {
				log.warn("Did not find combatant info for id {} name '{}', guessing", Long.toString(id, 16), name);
			}
			return new XivCombatant(id, name);
		}
	}

}
