package gg.xp.xivsupport.events.triggers.duties.Pandamonium;

import gg.xp.reevent.events.EventContext;
import gg.xp.reevent.scan.AutoChildEventHandler;
import gg.xp.reevent.scan.FilteredEventHandler;
import gg.xp.reevent.scan.HandleEvents;
import gg.xp.xivdata.data.duties.*;
import gg.xp.xivsupport.callouts.CalloutRepo;
import gg.xp.xivsupport.callouts.ModifiableCallout;
import gg.xp.xivsupport.events.actlines.events.AbilityCastStart;
import gg.xp.xivsupport.events.actlines.events.BuffApplied;
import gg.xp.xivsupport.events.actlines.events.HasDuration;
import gg.xp.xivsupport.events.state.XivState;
import gg.xp.xivsupport.events.state.combatstate.StatusEffectRepository;
import gg.xp.xivsupport.events.triggers.util.RepeatSuppressor;
import gg.xp.xivsupport.models.ArenaPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@CalloutRepo(name = "P6", duty = KnownDuty.P6)
public class P6 extends AutoChildEventHandler implements FilteredEventHandler {
	private static final Logger log = LoggerFactory.getLogger(P6.class);
	private final ModifiableCallout<AbilityCastStart> aethericPolyominoid = ModifiableCallout.durationBasedCall("Aetheric Polyominoid", "tiles"); //7855 tile explosion
	private final ModifiableCallout<AbilityCastStart> polyominoidSigma = ModifiableCallout.durationBasedCall("Polyominoid Sigma", "tiles swapping");
	private final ModifiableCallout<AbilityCastStart> chorosIxouSides = ModifiableCallout.durationBasedCall("Choros Ixou Sides hit", "front back"); //785A? 7858?
	private final ModifiableCallout<AbilityCastStart> chorosIxouFrontBack = ModifiableCallout.durationBasedCall("Choros Ixou Front Back hit", "side"); //7857? 7859?
	private final ModifiableCallout<AbilityCastStart> hemitheosDarkIV = ModifiableCallout.durationBasedCall("Hemitheos's Dark IV", "raidwide");
	private final ModifiableCallout<AbilityCastStart> synergy = ModifiableCallout.durationBasedCall("Synergy", "tankbuster"); //785C on MT, 785D on OT
	private final ModifiableCallout<AbilityCastStart> stropheIxouCW = ModifiableCallout.durationBasedCall("Strophe Ixou", "sides, clockwise");
	private final ModifiableCallout<AbilityCastStart> stropheIxouCCW = ModifiableCallout.durationBasedCall("Strophe Ixou", "sides, counter clockwise");
	private final ModifiableCallout<AbilityCastStart> darkAshes = ModifiableCallout.durationBasedCall("Dark Ashes", "spread"); //785E real boss

	private final ModifiableCallout<HasDuration> glossomorph = ModifiableCallout.durationBasedCall("Glossomorph debuff", "point away soon").autoIcon();

	private final ArenaPos arenaPos = new ArenaPos(100, 100, 8, 8);

	public P6(XivState state, StatusEffectRepository buffs) {
		this.state = state;
		this.buffs = buffs;
	}

	private final XivState state;
	private XivState getState() {
		return this.state;
	}

	private final StatusEffectRepository buffs;
	private StatusEffectRepository getBuffs() {
		return this.buffs;
	}

	@Override
	public boolean enabled(EventContext context) {
		return state.zoneIs(0x43B);
	}

	private final RepeatSuppressor buffAppliedSupp = new RepeatSuppressor(Duration.ofSeconds(21)); //longest buff duration is 20s

	@HandleEvents
	public void startsCasting(EventContext context, AbilityCastStart event) {
		long id = event.getAbility().getId();
		ModifiableCallout<AbilityCastStart> call;
		if (id == 0x7853)
			call = aethericPolyominoid;
		else if (id == 0x7856)
			call = polyominoidSigma;
		else if (id == 0x7858)
			call = chorosIxouSides;
		else if (id == 0x7857)
			call = chorosIxouFrontBack;
		else if (id == 0x784E)
			call = hemitheosDarkIV;
		else if (id == 0x785B) //see synergy declaration
			call = synergy;
		else if (id == 0x7A11)
			call = stropheIxouCCW;
		else if (id == 0x7A12)
			call = stropheIxouCW;
		else if (id == 0x785F && event.getTarget().isThePlayer())
			call = darkAshes;
		else
			return;

		context.accept(call.getModified(event));
	}

	@HandleEvents
	public void buffApplied(EventContext context, BuffApplied event) {
		long id = event.getBuff().getId();
		Duration duration = event.getInitialDuration();
		ModifiableCallout<HasDuration> call;
		if (event.getTarget().isThePlayer() && buffAppliedSupp.check(event) && id == 0xCF2) //CFA bad glossomorph
			call = glossomorph;
		else
			return;

		context.accept(call.getModified(event));
	}
}
