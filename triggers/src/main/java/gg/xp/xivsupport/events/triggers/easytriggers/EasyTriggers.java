package gg.xp.xivsupport.events.triggers.easytriggers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.reevent.events.Event;
import gg.xp.reevent.events.EventContext;
import gg.xp.reevent.scan.HandleEvents;
import gg.xp.reevent.scan.ScanMe;
import gg.xp.xivsupport.events.ACTLogLineEvent;
import gg.xp.xivsupport.events.actlines.events.AbilityCastCancel;
import gg.xp.xivsupport.events.actlines.events.AbilityCastStart;
import gg.xp.xivsupport.events.actlines.events.AbilityResolvedEvent;
import gg.xp.xivsupport.events.actlines.events.AbilityUsedEvent;
import gg.xp.xivsupport.events.actlines.events.ActorControlEvent;
import gg.xp.xivsupport.events.actlines.events.BuffApplied;
import gg.xp.xivsupport.events.actlines.events.BuffRemoved;
import gg.xp.xivsupport.events.actlines.events.ChatLineEvent;
import gg.xp.xivsupport.events.actlines.events.EntityKilledEvent;
import gg.xp.xivsupport.events.actlines.events.HasAbility;
import gg.xp.xivsupport.events.actlines.events.HasDuration;
import gg.xp.xivsupport.events.actlines.events.HasSourceEntity;
import gg.xp.xivsupport.events.actlines.events.HasStatusEffect;
import gg.xp.xivsupport.events.actlines.events.HasTargetEntity;
import gg.xp.xivsupport.events.actlines.events.HasTargetIndex;
import gg.xp.xivsupport.events.triggers.easytriggers.conditions.AbilityIdFilter;
import gg.xp.xivsupport.events.triggers.easytriggers.conditions.ChatLineRegexFilter;
import gg.xp.xivsupport.events.triggers.easytriggers.conditions.ChatLineTypeFilter;
import gg.xp.xivsupport.events.triggers.easytriggers.conditions.DurationFilter;
import gg.xp.xivsupport.events.triggers.easytriggers.conditions.EntityType;
import gg.xp.xivsupport.events.triggers.easytriggers.conditions.GroovyEventFilter;
import gg.xp.xivsupport.events.triggers.easytriggers.conditions.LogLineNumberFilter;
import gg.xp.xivsupport.events.triggers.easytriggers.conditions.LogLineRegexFilter;
import gg.xp.xivsupport.events.triggers.easytriggers.conditions.SourceEntityNpcIdFilter;
import gg.xp.xivsupport.events.triggers.easytriggers.conditions.SourceEntityTypeFilter;
import gg.xp.xivsupport.events.triggers.easytriggers.conditions.StatusIdFilter;
import gg.xp.xivsupport.events.triggers.easytriggers.conditions.StatusStacksFilter;
import gg.xp.xivsupport.events.triggers.easytriggers.conditions.TargetCountFilter;
import gg.xp.xivsupport.events.triggers.easytriggers.conditions.TargetEntityNpcIdFilter;
import gg.xp.xivsupport.events.triggers.easytriggers.conditions.TargetEntityTypeFilter;
import gg.xp.xivsupport.events.triggers.easytriggers.conditions.TargetIndexFilter;
import gg.xp.xivsupport.events.triggers.easytriggers.conditions.gui.GenericFieldEditor;
import gg.xp.xivsupport.events.triggers.easytriggers.conditions.gui.GroovyFilterEditor;
import gg.xp.xivsupport.events.triggers.easytriggers.model.Condition;
import gg.xp.xivsupport.events.triggers.easytriggers.model.ConditionDescription;
import gg.xp.xivsupport.events.triggers.easytriggers.model.EasyTrigger;
import gg.xp.xivsupport.events.triggers.easytriggers.model.EventDescription;
import gg.xp.xivsupport.events.triggers.easytriggers.model.EventDescriptionImpl;
import gg.xp.xivsupport.events.triggers.easytriggers.model.HasMutableConditions;
import gg.xp.xivsupport.gui.tables.filters.ValidationError;
import gg.xp.xivsupport.models.CombatantType;
import gg.xp.xivsupport.models.XivCombatant;
import gg.xp.xivsupport.persistence.PersistenceProvider;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ScanMe
public class EasyTriggers {
	private static final Logger log = LoggerFactory.getLogger(EasyTriggers.class);
	private static final String settingKey = "easy-triggers.my-triggers";
	private static final String failedTriggersSettingKey = "easy-triggers.failed-triggers";
	private static final ObjectMapper mapper = new ObjectMapper();

	private final PersistenceProvider pers;

	private List<EasyTrigger<?>> triggers;

	public EasyTriggers(PersistenceProvider pers) {
		this.pers = pers;
		String strVal = pers.get(settingKey, String.class, null);
		triggers = new ArrayList<>();
		if (strVal != null) {
			try {
				// First, convert to List<JsonNode> so that errors can be reported for individual triggers
				List<JsonNode> jsonNodes = mapper.readValue(strVal, new TypeReference<>() {
				});
				List<JsonNode> failed = new ArrayList<>();
				for (JsonNode jsonNode : jsonNodes) {
					try {
						EasyTrigger easyTrigger = mapper.convertValue(jsonNode, EasyTrigger.class);
						triggers.add(easyTrigger);
					}
					catch (Throwable jpe) {
						log.error("Trigger failed to load: \n{}\n", jsonNode, jpe);
						failed.add(jsonNode);
					}
				}
				if (!failed.isEmpty()) {
					String failedSetting = pers.get(failedTriggersSettingKey, String.class, "[]");
					List<String> otherFailues = mapper.readValue(failedSetting, new TypeReference<>() {
					});
					List<String> failures = new ArrayList<>(otherFailues);
					failures.addAll(jsonNodes.stream().map(Object::toString).toList());
					pers.save(failedTriggersSettingKey, mapper.writeValueAsString(failures));
					log.info("One or more easy triggers failed to load - they have been saved to the setting '{}'", failedTriggersSettingKey);
				}
			}
			catch (Throwable e) {
				log.error("Error loading Easy Triggers", e);
				log.error("Dump of trigger data:\n{}", strVal);
				throw new RuntimeException("There was an error loading Easy Triggers. Check the log.", e);
			}
		}
	}

	public static String exportToString(List<EasyTrigger<?>> toExport) {
		try {
			return mapper.writeValueAsString(toExport);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException("Error exporting trigger", e);
		}
	}

	public static List<EasyTrigger<?>> importFromString(String string) {
		try {
			return mapper.readValue(string, new TypeReference<>() {
			});
		}
		catch (JsonProcessingException e) {
			throw new ValidationError("Error importing trigger: " + e.getMessage(), e);
		}
	}

	private void save() {
		try {
			String triggersSerialized = mapper.writeValueAsString(triggers);
//			log.info("Saving triggers: {}", triggersSerialized);
			pers.save(settingKey, triggersSerialized);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public void commit() {
		save();
	}

	@HandleEvents
	public void runEasyTriggers(EventContext context, Event event) {
		triggers.forEach(trig -> {
			try {
				trig.handleEvent(context, event);
			}
			catch (Throwable t) {
				log.error("Error running easy trigger '{}'", trig.getName(), t);
			}
		});
	}

	public List<EasyTrigger<?>> getTriggers() {
		return Collections.unmodifiableList(triggers);
	}

	public void addTrigger(EasyTrigger<?> trigger) {
		makeListWritable();
		triggers.add(trigger);
		save();
	}

	public void removeTrigger(EasyTrigger<?> trigger) {
		makeListWritable();
		triggers.remove(trigger);
		save();
	}

	private void makeListWritable() {
		if (!(triggers instanceof ArrayList<EasyTrigger<?>>)) {
			triggers = new ArrayList<>(triggers);
		}
	}

	public void setTriggers(List<EasyTrigger<?>> triggers) {
		this.triggers = new ArrayList<>(triggers);
		save();
	}

	// Be sure to add new types to EasyTriggersTest
	// TODO: might be nice to wire some of the "single value replacement" logic from ModifiableCallout into here, to skip the need for .name on everything
	private static final List<EventDescription<?>> eventTypes = List.of(
			new EventDescriptionImpl<>(AbilityCastStart.class, "An ability has started casting. Corresponds to ACT 20 lines.", "{event.ability}", "{event.ability} ({event.estimatedRemainingDuration})"),
			new EventDescriptionImpl<>(AbilityUsedEvent.class, "An ability has snapshotted. Corresponds to ACT 21/22 lines.", "{event.ability}"),
			new EventDescriptionImpl<>(AbilityCastCancel.class, "An ability was interrupted while casting. Corresponds to ACT 23 lines.", "{event.ability} interrupted"),
			new EventDescriptionImpl<>(EntityKilledEvent.class, "Something died. Corresponds to ACT 25 lines.", "{event.target} died"),
			new EventDescriptionImpl<>(BuffApplied.class, "A buff or debuff has been applied. Corresponds to ACT 26 lines.", "{event.buff} on {event.target}"),
			new EventDescriptionImpl<>(BuffRemoved.class, "A buff or debuff has been removed. Corresponds to ACT 30 lines.", "{event.buff} lost from {event.target}"),
			new EventDescriptionImpl<>(AbilityResolvedEvent.class, "An ability has actually applied. Corresponds to ACT 37 lines.", "{event.ability} resolved"),
			new EventDescriptionImpl<>(ActorControlEvent.class, "Conveys various state changes, such as wiping or finishing a raid. Corresponds to ACT 33 lines.", "Actor control {event.command}"),
			new EventDescriptionImpl<>(ACTLogLineEvent.class, "Any log line, in text form. Use as a last resort.", "Log Line {event.rawFields[0]}"),
			new EventDescriptionImpl<>(ChatLineEvent.class, "In-game chat lines", "{event.name} says {event.line}", "Chat Line {event.name}: {event.line}")
	);


	private static Component generic(Condition<?> cond, EasyTrigger<?> trigger) {
		return new GenericFieldEditor(cond);
	}

	// XXX - DO NOT CHANGE NAMES OF THESE CLASSES OR PACKAGE PATH - FQCN IS PART OF DESERIALIZATION!!!
	private static final List<ConditionDescription<?, ?>> conditions = List.of(
			new ConditionDescription<>(AbilityIdFilter.class, HasAbility.class, "Ability ID", AbilityIdFilter::new, EasyTriggers::generic),
			new ConditionDescription<>(StatusIdFilter.class, HasStatusEffect.class, "Status Effect ID", StatusIdFilter::new, EasyTriggers::generic),
			new ConditionDescription<>(StatusStacksFilter.class, HasStatusEffect.class, "Status Effect Stack Count", StatusStacksFilter::new, EasyTriggers::generic),
			new ConditionDescription<>(SourceEntityTypeFilter.class, HasSourceEntity.class, "Source Combatant", SourceEntityTypeFilter::new, EasyTriggers::generic),
			new ConditionDescription<>(TargetEntityTypeFilter.class, HasTargetEntity.class, "Target Combatant", TargetEntityTypeFilter::new, EasyTriggers::generic),
			new ConditionDescription<>(SourceEntityNpcIdFilter.class, HasSourceEntity.class, "Source Combatant NPC ID", SourceEntityNpcIdFilter::new, EasyTriggers::generic),
			new ConditionDescription<>(TargetEntityNpcIdFilter.class, HasTargetEntity.class, "Target Combatant NPC ID", TargetEntityNpcIdFilter::new, EasyTriggers::generic),
			new ConditionDescription<>(TargetIndexFilter.class, HasTargetIndex.class, "Target Index", TargetIndexFilter::new, EasyTriggers::generic),
			new ConditionDescription<>(TargetCountFilter.class, HasTargetIndex.class, "Target Count", TargetCountFilter::new, EasyTriggers::generic),
			new ConditionDescription<>(DurationFilter.class, HasDuration.class, "Castbar or Status Duration", DurationFilter::new, EasyTriggers::generic),
			new ConditionDescription<>(LogLineRegexFilter.class, ACTLogLineEvent.class, "Log Line Regular Expression (Regex)", LogLineRegexFilter::new, EasyTriggers::generic),
			new ConditionDescription<>(LogLineNumberFilter.class, ACTLogLineEvent.class, "Log Line Number", LogLineNumberFilter::new, EasyTriggers::generic),
			new ConditionDescription<>(ChatLineRegexFilter.class, ChatLineEvent.class, "Chat Line Regular Expression (Regex)", ChatLineRegexFilter::new, EasyTriggers::generic),
			new ConditionDescription<>(ChatLineTypeFilter.class, ChatLineEvent.class, "Chat Line Number", ChatLineTypeFilter::new, EasyTriggers::generic),
			new ConditionDescription<>(GroovyEventFilter.class, Event.class, "Make your own filter code with Groovy", GroovyEventFilter::new, (a, b) -> new GroovyFilterEditor<>(a, (EasyTrigger<Event>) b))
	);

	public static List<EventDescription<?>> getEventDescriptions() {
		return eventTypes;
	}

	@SuppressWarnings("unchecked")
	public static @Nullable <X> EventDescription<X> getEventDescription(Class<X> event) {
		return (EventDescription<X>) eventTypes.stream().filter(desc -> desc.type().equals(event)).findFirst().orElse(null);
	}

	public static List<ConditionDescription<?, ?>> getConditions() {
		return conditions;
	}

	public static <X> List<ConditionDescription<?, ?>> getConditionsApplicableTo(HasMutableConditions<X> trigger) {
		return conditions.stream().filter(cdesc -> cdesc.appliesTo(trigger.classForConditions())).toList();
	}

	@SuppressWarnings("unchecked")
	public static <X extends Condition<Y>, Y> ConditionDescription<X, Y> getConditionDescription(Class<X> cond) {
		ConditionDescription<?, ?> conditionDescription = conditions.stream().filter(item -> item.clazz().equals(cond)).findFirst().orElse(null);
		return (ConditionDescription<X, Y>) conditionDescription;
	}

	public static @Nullable EasyTrigger<?> makeTriggerFromEvent(Event event) {
		if (event instanceof AbilityUsedEvent abu) {
			EasyTrigger<AbilityUsedEvent> trigger = getEventDescription(AbilityUsedEvent.class).newInst();
			trigger.setName(abu.getAbility().getName() + " used");
			makeSourceConditions(abu).forEach(trigger::addCondition);
			makeTargetConditions(abu).forEach(trigger::addCondition);
			makeAbilityConditions(abu).forEach(trigger::addCondition);
			makeTargetIndexConditions(abu).forEach(trigger::addCondition);
			return trigger;
		}
		else if (event instanceof AbilityCastStart acs) {
			EasyTrigger<AbilityCastStart> trigger = getEventDescription(AbilityCastStart.class).newInst();
			trigger.setName(acs.getAbility().getName() + " casting");
			makeSourceConditions(acs).forEach(trigger::addCondition);
			makeTargetConditions(acs).forEach(trigger::addCondition);
			makeAbilityConditions(acs).forEach(trigger::addCondition);
			return trigger;
		}
		else if (event instanceof AbilityCastCancel acc) {
			EasyTrigger<AbilityCastCancel> trigger = getEventDescription(AbilityCastCancel.class).newInst();
			trigger.setName(acc.getAbility().getName() + " cancelled");
			makeSourceConditions(acc).forEach(trigger::addCondition);
			makeAbilityConditions(acc).forEach(trigger::addCondition);
			return trigger;
		}
		else if (event instanceof AbilityResolvedEvent are) {
			EasyTrigger<AbilityResolvedEvent> trigger = getEventDescription(AbilityResolvedEvent.class).newInst();
			trigger.setName(are.getAbility().getName() + " resolved");
			makeSourceConditions(are).forEach(trigger::addCondition);
			makeTargetConditions(are).forEach(trigger::addCondition);
			makeAbilityConditions(are).forEach(trigger::addCondition);
			makeTargetIndexConditions(are).forEach(trigger::addCondition);
			return trigger;
		}
		else if (event instanceof BuffApplied ba) {
			EasyTrigger<BuffApplied> trigger = getEventDescription(BuffApplied.class).newInst();
			trigger.setName(ba.getBuff().getName() + " applied");
			makeSourceConditions(ba).forEach(trigger::addCondition);
			makeTargetConditions(ba).forEach(trigger::addCondition);
			makeStatusConditions(ba).forEach(trigger::addCondition);
			return trigger;
		}
		else if (event instanceof BuffRemoved br) {
			EasyTrigger<BuffRemoved> trigger = getEventDescription(BuffRemoved.class).newInst();
			trigger.setName(br.getBuff().getName() + " removed");
			makeSourceConditions(br).forEach(trigger::addCondition);
			makeTargetConditions(br).forEach(trigger::addCondition);
			makeStatusConditions(br).forEach(trigger::addCondition);
			return trigger;
		}

		return null;
	}


	private static List<Condition<HasTargetIndex>> makeTargetIndexConditions(HasTargetIndex hti) {
		if (hti.getTargetIndex() == 0) {
			TargetIndexFilter tif = new TargetIndexFilter();
			tif.expected = 0;
			return Collections.singletonList(tif);
		}
		else {
			return Collections.emptyList();
		}
	}

	private static List<Condition<HasAbility>> makeAbilityConditions(HasAbility ha) {
		AbilityIdFilter aif = new AbilityIdFilter();
		aif.expected = ha.getAbility().getId();
		return Collections.singletonList(aif);
	}

	private static List<Condition<HasStatusEffect>> makeStatusConditions(HasStatusEffect hsa) {
		StatusIdFilter sif = new StatusIdFilter();
		sif.expected = hsa.getBuff().getId();
		// TODO: refresh vs initial app
		return Collections.singletonList(sif);
	}


	private static List<Condition<HasSourceEntity>> makeSourceConditions(HasSourceEntity hse) {
		XivCombatant source = hse.getSource();
		if (source.isThePlayer()) {
			SourceEntityTypeFilter etf = new SourceEntityTypeFilter();
			etf.type = EntityType.THE_PLAYER;
			return Collections.singletonList(etf);
		}
		// TODO: party member
		else if (source.isPc()) {
			SourceEntityTypeFilter etf = new SourceEntityTypeFilter();
			etf.type = EntityType.ANY_PLAYER;
			return Collections.singletonList(etf);
		}
		else if (source.getbNpcId() != 0) {
			SourceEntityNpcIdFilter enif = new SourceEntityNpcIdFilter();
			enif.expected = source.getbNpcId();
			return Collections.singletonList(enif);
		}
		else {
			return Collections.emptyList();
		}
	}

	private static List<Condition<HasTargetEntity>> makeTargetConditions(HasTargetEntity hse) {
		XivCombatant target = hse.getTarget();
		if (target.isThePlayer()) {
			TargetEntityTypeFilter etf = new TargetEntityTypeFilter();
			etf.type = EntityType.THE_PLAYER;
			return Collections.singletonList(etf);
		}
		// TODO: party member
		else if (target.isPc()) {
			TargetEntityTypeFilter etf = new TargetEntityTypeFilter();
			etf.type = EntityType.ANY_PLAYER;
			return Collections.singletonList(etf);
		}
		else if (target.getType() == CombatantType.NPC) {
			TargetEntityTypeFilter etf = new TargetEntityTypeFilter();
			etf.type = EntityType.NPC;
			return Collections.singletonList(etf);
		}
		else {
			return Collections.emptyList();
		}
	}

}
