package gg.xp.xivsupport.events.triggers.duties.Pandamonium;

import gg.xp.reevent.events.BaseEvent;
import gg.xp.reevent.events.EventContext;
import gg.xp.reevent.scan.AutoChildEventHandler;
import gg.xp.reevent.scan.AutoFeed;
import gg.xp.reevent.scan.FilteredEventHandler;
import gg.xp.reevent.scan.HandleEvents;
import gg.xp.xivdata.data.duties.*;
import gg.xp.xivsupport.callouts.CalloutRepo;
import gg.xp.xivsupport.callouts.ModifiableCallout;
import gg.xp.xivsupport.events.actlines.events.AbilityCastStart;
import gg.xp.xivsupport.events.actlines.events.AbilityUsedEvent;
import gg.xp.xivsupport.events.actlines.events.BuffApplied;
import gg.xp.xivsupport.events.actlines.events.actorcontrol.DutyCommenceEvent;
import gg.xp.xivsupport.events.state.XivState;
import gg.xp.xivsupport.events.state.combatstate.ActiveCastRepository;
import gg.xp.xivsupport.events.state.combatstate.CastResult;
import gg.xp.xivsupport.events.state.combatstate.CastTracker;
import gg.xp.xivsupport.events.triggers.seq.SequentialTrigger;
import gg.xp.xivsupport.events.triggers.seq.SqtTemplates;
import gg.xp.xivsupport.models.ArenaPos;
import gg.xp.xivsupport.models.ArenaSector;
import gg.xp.xivsupport.models.Position;
import gg.xp.xivsupport.models.XivCombatant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CalloutRepo(name = "P8S", duty = KnownDuty.P8S)
public class P8S extends AutoChildEventHandler implements FilteredEventHandler {
	private static final Logger log = LoggerFactory.getLogger(P8S.class);
	private final ModifiableCallout<AbilityCastStart> genesisOfFlame = ModifiableCallout.durationBasedCall("Genesis Of Flame", "Raidwide");
	private final ModifiableCallout<AbilityCastStart> rearingRampage = ModifiableCallout.durationBasedCall("Rearing Rampage", "Raidwides and Spread");
	private final ModifiableCallout<AbilityCastStart> ektothermos = ModifiableCallout.durationBasedCall("Ektothermos", "Raidwide");
	private final ModifiableCallout<AbilityCastStart> spreadLater = new ModifiableCallout<>("Octaflare: Spread Later", "Spread Later", 20_000);
	private final ModifiableCallout<AbilityCastStart> stackLater = new ModifiableCallout<>("Tetraflare: Stack Later", "Stack Later", 20_000);
	private final ModifiableCallout<AbilityCastStart> spread = ModifiableCallout.durationBasedCall("Octaflare: Spread Now", "Spread Later");
	private final ModifiableCallout<AbilityCastStart> stack = ModifiableCallout.durationBasedCall("Tetraflare: Stack Now", "Stack Later");


	private final ModifiableCallout<AbilityCastStart> volcanicTorchesEWOut = ModifiableCallout.durationBasedCall("Torches: East/West Safe", "East/West Out");
	private final ModifiableCallout<AbilityCastStart> volcanicTorchesNSOut = ModifiableCallout.durationBasedCall("Torches: North/South Safe", "North/South Out");
	private final ModifiableCallout<AbilityCastStart> volcanicTorchesCenter = ModifiableCallout.durationBasedCall("Torches: Center", "Center");
	private final ModifiableCallout<AbilityCastStart> volcanicTorchesWOutEIn = ModifiableCallout.durationBasedCall("Torches: W Out, E In", "West Out, East In");
	private final ModifiableCallout<AbilityCastStart> volcanicTorchesWInEOut = ModifiableCallout.durationBasedCall("Torches: W In, E Out", "West In, East Out");
	private final ModifiableCallout<AbilityCastStart> volcanicTorchesNOutSIn = ModifiableCallout.durationBasedCall("Torches: N Out, S In", "North Out, South In");
	private final ModifiableCallout<AbilityCastStart> volcanicTorchesNInSOut = ModifiableCallout.durationBasedCall("Torches: N In, S Out", "North In, South Out");
	private final ModifiableCallout<AbilityCastStart> volcanicTorchesError = ModifiableCallout.durationBasedCall("Torches: Trigger Error", "Error");
	private final ModifiableCallout<AbilityCastStart> volcanicTorches2SafeSpot = ModifiableCallout.durationBasedCall("Torches 2: Safe Corner", "{corner} safe");

	private final ModifiableCallout<AbilityCastStart> sunforgePhoenix = ModifiableCallout.durationBasedCall("Sunforge Phoenix", "In");
	private final ModifiableCallout<AbilityCastStart> sunforgeSerpent = ModifiableCallout.durationBasedCall("Sunforge Serpent", "Out");
	private final ModifiableCallout<AbilityCastStart> sunforgePhoenixStack = ModifiableCallout.durationBasedCall("Sunforge Phoenix", "In and Stack");
	private final ModifiableCallout<AbilityCastStart> sunforgeSerpentStack = ModifiableCallout.durationBasedCall("Sunforge Serpent", "Out and Stack");
	private final ModifiableCallout<AbilityCastStart> sunforgePhoenixSpread = ModifiableCallout.durationBasedCall("Sunforge Phoenix", "In and Spread");
	private final ModifiableCallout<AbilityCastStart> sunforgeSerpentSpread = ModifiableCallout.durationBasedCall("Sunforge Serpent", "Out and Spread");
	private final ModifiableCallout<AbilityCastStart> dogForm = ModifiableCallout.durationBasedCall("Quadruped Form", "Quadruped Form, Knockback Soon");
	private final ModifiableCallout<AbilityCastStart> snakeForm = ModifiableCallout.durationBasedCall("Snake Form", "Snake Form");
	private final ModifiableCallout<BuffApplied> snakeFirstCone = ModifiableCallout.<BuffApplied>durationBasedCall("Snake: First Gaze Cone", "First Gaze Cone").autoIcon();
	private final ModifiableCallout<BuffApplied> snakeSecondCone = ModifiableCallout.<BuffApplied>durationBasedCall("Snake: Second Gaze Cone", "Second Gaze Cone").autoIcon();
	private final ModifiableCallout<BuffApplied> snakeFirstPuddle = ModifiableCallout.<BuffApplied>durationBasedCall("Snake: First Puddle", "First Puddle").autoIcon();
	private final ModifiableCallout<BuffApplied> snakeSecondPuddle = ModifiableCallout.<BuffApplied>durationBasedCall("Snake: Second Puddle", "Second Puddle").autoIcon();
	private final ModifiableCallout<AbilityCastStart> firstGazeCall = ModifiableCallout.durationBasedCallWithOffset("Snake: First Gazes", "Gazes: {gazeSpots}", Duration.ofMillis(1_200));
	private final ModifiableCallout<AbilityUsedEvent> firstGazeDone = new ModifiableCallout<>("Snake: First Gazes Done", "Gaze Done");
	private final ModifiableCallout<AbilityCastStart> secondGazeCall = ModifiableCallout.durationBasedCallWithOffset("Snake: Second Gazes", "Gazes: {gazeSpots}", Duration.ofMillis(1_200));
	private final ModifiableCallout<AbilityUsedEvent> secondGazeDone = new ModifiableCallout<>("Snake: Second Gazes Done", "Gaze Done");
	private final ModifiableCallout<AbilityUsedEvent> snakeOut = new ModifiableCallout<>("Snake: Out", "Out");
	// TODO
//	private final ModifiableCallout<AbilityUsedEvent> dogKb = new ModifiableCallout<>("Quadruped: Knockback", "Knockback");

	private final ModifiableCallout<AbilityUsedEvent> upliftNumber = new ModifiableCallout<>("Quadruped: Rearing Rampage Number", "Bait {num} with {partner}");
	private final ModifiableCallout<BaseEvent> upliftBait = new ModifiableCallout<>("Quadruped: Rearing Rampage Bait", "Bait with {partner}");

	private final ModifiableCallout<AbilityCastStart> doublePinionCenter = ModifiableCallout.durationBasedCallWithOffset("Double Pinion: Center", "Center", Duration.ofMillis(1_000));
	private final ModifiableCallout<AbilityCastStart> doublePinionNS = ModifiableCallout.durationBasedCallWithOffset("Double Pinion: North/South Out", "North/South Out", Duration.ofMillis(1_000));
	private final ModifiableCallout<AbilityCastStart> doublePinionEW = ModifiableCallout.durationBasedCallWithOffset("Double Pinion: East/West Out", "East/West Out", Duration.ofMillis(1_000));
	private final ModifiableCallout<AbilityCastStart> doublePinionCorners = ModifiableCallout.durationBasedCallWithOffset("Double Pinion: Corners", "Corners", Duration.ofMillis(1_000));


	//	private final ModifiableCallout<AbilityCastStart> reforgedReflectionQuadruped = ModifiableCallout.durationBasedCall("Reforged Reflection Quadruped", "Quadruped");
//	private final ModifiableCallout<AbilityCastStart> reforgedReflectionSerpent = ModifiableCallout.durationBasedCall("Reforged Reflection Serpent", "Serpent");
//	private final ModifiableCallout<AbilityCastStart> fourfoldFiresSafe = ModifiableCallout.durationBasedCall("Fourfold Fires Safe Spot", "{safe}");
	private final ModifiableCallout<AbilityCastStart> flameviper = ModifiableCallout.durationBasedCall("Flameviper", "Double Buster with Bleed");
	private final ModifiableCallout<AbilityCastStart> nestOfFlameVipers = ModifiableCallout.durationBasedCall("Nest of Flamevipers", "Proteans");

	private final ArenaPos arenaPos = new ArenaPos(100, 100, 8, 8);

	public P8S(XivState state, ActiveCastRepository acr) {
		this.state = state;
		this.acr = acr;
	}

	private final XivState state;

	private XivState getState() {
		return this.state;
	}

	private final ActiveCastRepository acr;

	public ActiveCastRepository getAcr() {
		return acr;
	}

	@Override
	public boolean enabled(EventContext context) {
		return state.dutyIs(KnownDuty.P8S);
	}

	private enum Flare {
		OctaSpread,
		TetraStack
	}

	private volatile Flare nextFlare;

	@HandleEvents
	public void reset(EventContext context, DutyCommenceEvent event) {
		nextFlare = null;
		seenFirstSnakes = false;
	}

	@HandleEvents
	public void startsCasting(EventContext context, AbilityCastStart event) {
		int id = (int) event.getAbility().getId();
		ModifiableCallout<AbilityCastStart> call;
		// Savage IDs
		switch (id) {
			case 31044 -> call = genesisOfFlame; // raidwide
			case 31210 -> call = ektothermos; // raidwide
			case 0x7910 -> {
				// out
				if (nextFlare == Flare.OctaSpread) {
					call = sunforgeSerpentSpread;
				}
				else if (nextFlare == Flare.TetraStack) {
					call = sunforgeSerpentStack;
				}
				else {
					call = sunforgeSerpent;
				}
			}
			case 0x7911 -> {
				// In
				if (nextFlare == Flare.OctaSpread) {
					call = sunforgePhoenixSpread;
				}
				else if (nextFlare == Flare.TetraStack) {
					call = sunforgePhoenixStack;
				}
				else {
					call = sunforgePhoenix;
				}
			}
			case 0x794b -> call = dogForm; // dog form, kb
			case 0x794c -> call = snakeForm; // snake form, out
			case 0x7933 -> call = rearingRampage; // double hit + raidwide x4, must spread
			case 0x7914 -> {
				call = spreadLater;
				nextFlare = Flare.OctaSpread;
			}
			case 0x7915 -> {
				call = stackLater;
				nextFlare = Flare.TetraStack;
			}
			case 0x7945 -> call = flameviper;
			case 31007 -> call = nestOfFlameVipers;
			case 31006 -> call = stack;
			case 31005 -> call = spread;
			default -> {
				return;
			}
		}
		context.accept(call.getModified(event));
	}
	/*
		Stack/spread notes:
		spread precursor = 7914
		stack precursor = 7915
		spread hit = 7918
		stack hit = 7919
	 */

	//
	@HandleEvents
	public void buffApplied(EventContext context, BuffApplied event) {
		if (event.getTarget().isThePlayer()) {
			ModifiableCallout<BuffApplied> call;
			switch ((int) event.getBuff().getId()) {
				case 3351 -> {
					if (seenFirstSnakes) {
						return;
					}
					if (event.getInitialDuration().toSeconds() < 30) {
						call = snakeFirstCone;
					}
					else {
						call = snakeSecondCone;
					}
				}
				case 3326 -> {
					if (seenFirstSnakes) {
						return;
					}
					if (event.getInitialDuration().toSeconds() < 30) {
						call = snakeFirstPuddle;
					}
					else {
						call = snakeSecondPuddle;
					}
				}
				default -> {
					return;
				}
			}
			context.accept(call.getModified(event));
		}
	}

	private boolean seenFirstSnakes;

	@AutoFeed
	private final SequentialTrigger<BaseEvent> snakeFormGazes = SqtTemplates.sq(60_000,
			AbilityCastStart.class, acs -> acs.abilityIdMatches(0x794c),
			(e1, s) -> {
				AbilityUsedEvent e = s.waitEvent(AbilityUsedEvent.class, aue -> aue.abilityIdMatches(0x794c));
				s.waitMs(1000);
				s.updateCall(snakeOut.getModified(e));
				if (seenFirstSnakes) {
					// This is second snakes
					return;
				}
				{
					List<AbilityCastStart> firstGazes = s.waitEvents(2, AbilityCastStart.class, acs -> acs.abilityIdMatches(0x792B));
					AbilityCastStart sampleCast = firstGazes.get(0);
					s.waitThenRefreshCombatants(100);
					List<ArenaSector> firstBadSpots = firstGazes.stream()
							.map(AbilityCastStart::getSource)
							.map(cbt -> getState().getLatestCombatantData(cbt))
							.map(arenaPos::forCombatant)
							.toList();

					s.updateCall(firstGazeCall.getModified(sampleCast, Map.of("gazeSpots", firstBadSpots)));
				}
				// Why, SE. Why did you make the snapshot of the actual cast NOT the thing that does the petrifaction?
				// Also, sometimes the second set starts its cast before the first goes off. Other times, it's the other
				// way around, so we have to loop to account for both.
				{
					AbilityUsedEvent firstDone = s.waitEvent(AbilityUsedEvent.class, aue -> aue.abilityIdMatches(0x6724));
					s.updateCall(firstGazeDone.getModified(firstDone));
				}
				// Find second set
				{
					List<AbilityCastStart> secondGazes;
					int limit = 20;
					do {
						if (limit-- <= 0) {
							log.error("Error in snake form gazes! Quitting");
							return;
						}
						s.waitMs(50);
						secondGazes = getAcr().getAll().stream()
								.filter(ct -> ct.getCast().abilityIdMatches(0x792b) && ct.getResult() == CastResult.IN_PROGRESS)
								.map(CastTracker::getCast)
								.toList();
					} while (secondGazes.size() < 2);
					List<ArenaSector> secondBadSpots = secondGazes.stream()
							.map(AbilityCastStart::getSource)
							.map(cbt -> getState().getLatestCombatantData(cbt))
							.map(arenaPos::forCombatant)
							.toList();
					s.waitMs(2000);
					s.updateCall(secondGazeCall.getModified(secondGazes.get(0), Map.of("gazeSpots", secondBadSpots)));
					// Make sure we don't hit something from the first set if they arrive in a weird order
					AbilityUsedEvent secondDone = s.waitEvent(AbilityUsedEvent.class, aue -> aue.abilityIdMatches(0x6724));
					s.updateCall(secondGazeDone.getModified(secondDone));
				}
				seenFirstSnakes = true;

			});

	private enum DoublePinionSpots {
		NS_OUT,
		EW_OUT,
		CORNERS,
		CENTER
	}

	/*
		When the real Heph is casting it, the basic ability ID is fine, since there's one for in and one for out.
	 */
	@AutoFeed
	private final SequentialTrigger<BaseEvent> doublePinionFang = SqtTemplates.sq(2_000,
			AbilityCastStart.class, acs -> acs.abilityIdMatches(0x7950, 0x7951),
			(e1, s) -> {
				AbilityCastStart e2 = s.waitEvent(AbilityCastStart.class, acs -> acs.abilityIdMatches(0x7950, 0x7951));
				s.waitThenRefreshCombatants(100);
				Set<DoublePinionSpots> safeSpots = EnumSet.allOf(DoublePinionSpots.class);
				for (AbilityCastStart acs : List.of(e1, e2)) {
					XivCombatant cbt = getState().getLatestCombatantData(acs.getSource());
					ArenaSector sector = arenaPos.forCombatant(cbt);
					if (sector == ArenaSector.WEST || sector == ArenaSector.EAST) {
						// 7950 is 'out', 7951 is 'in', judging by the fact that 7950 has a single fake cast while 7951 has two (one for each side it hits).
						if (acs.abilityIdMatches(0x7950)) {
							// Facing east/west, cleaving through middle
							safeSpots.remove(DoublePinionSpots.CENTER);
							safeSpots.remove(DoublePinionSpots.EW_OUT);
						}
						else {
							// Facing east/west, cleaving sides
							safeSpots.remove(DoublePinionSpots.NS_OUT);
							safeSpots.remove(DoublePinionSpots.CORNERS);
						}
					}
					else if (sector == ArenaSector.NORTH || sector == ArenaSector.SOUTH) {
						if (acs.abilityIdMatches(0x7950)) {
							// Facing north/south, cleaving through middle
							safeSpots.remove(DoublePinionSpots.CENTER);
							safeSpots.remove(DoublePinionSpots.NS_OUT);
						}
						else {
							// Facing north/south, cleaving sides
							safeSpots.remove(DoublePinionSpots.EW_OUT);
							safeSpots.remove(DoublePinionSpots.CORNERS);
						}
					}
					else {
						log.error("Double Pinion: Error! Invalid position {} for combatant {}", sector, cbt);
						return;
					}
					log.info("Double Pinion Values Now: {}", safeSpots);
				}
				// It *should* be fine to skip this, but until more testing, I want to make sure that we don't get
				// an invalid combination.
				if (safeSpots.size() != 1) {
					log.error("Bad values in Double Pinion! {}", safeSpots);
				}
				ModifiableCallout<AbilityCastStart> call = switch (safeSpots.iterator().next()) {
					case NS_OUT -> doublePinionNS;
					case EW_OUT -> doublePinionEW;
					case CORNERS -> doublePinionCorners;
					case CENTER -> doublePinionCenter;
				};
				s.updateCall(call.getModified(e1));
			});

	// x and y range from 0 to 3
	// 0 = north/west, 3 = south/east
	private record VolcanicTorchPos(int x, int y) {
	}

	private static VolcanicTorchPos torchPos(XivCombatant cbt) {
		Position pos = cbt.getPos();
		int x = (int) Math.round((pos.x() - 85.0) / 10.0);
		int y = (int) Math.round((pos.y() - 85.0) / 10.0);
		if (x < 0 || x > 3 || y < 0 || y > 3) {
			throw new IllegalArgumentException(String.format("Bad x/y: %s, %s -> %s, %s", pos.x(), pos.y(), x, y));
		}
		return new VolcanicTorchPos(x, y);
	}

	private boolean[][] computeTorchFlameBadSpots(List<AbilityCastStart> casts) {
		// x, y
		boolean[][] badSpots = new boolean[4][4];
		casts.stream()
				.map(AbilityCastStart::getSource)
				.map(cbt -> getState().getLatestCombatantData(cbt))
				.map(P8S::torchPos)
				.forEach(tp -> badSpots[tp.x][tp.y] = true);
		StringBuilder sb = new StringBuilder("Torches Map:");
		for (int y = 0; y < 4; y++) {
			sb.append('\n');
			for (int x = 0; x < 4; x++) {
				sb.append(badSpots[x][y] ? "X" : ".");
			}
		}
		log.info("Computed torches: {}", sb);
		return badSpots;
	}

	@AutoFeed
	private final SequentialTrigger<BaseEvent> volcanicTorches1sq = SqtTemplates.sq(15_000,
			AbilityCastStart.class, acs -> acs.abilityIdMatches(0x78F7),
			(e1, s) -> {
				List<AbilityCastStart> casts = s.waitEvents(12, AbilityCastStart.class, acs -> acs.abilityIdMatches(0x7927));
				AbilityCastStart sample = casts.get(0);
				s.waitThenRefreshCombatants(100);
				// x, y
				boolean[][] badSpots = computeTorchFlameBadSpots(casts);
				ModifiableCallout<AbilityCastStart> call;
				boolean nOut = !badSpots[1][0] && !badSpots[2][0];
				boolean nIn = !badSpots[1][1] && !badSpots[2][1];
				boolean sIn = !badSpots[1][2] && !badSpots[2][2];
				boolean sOut = !badSpots[1][3] && !badSpots[2][3];
				boolean wOut = !badSpots[0][1] && !badSpots[0][2];
				boolean wIn = !badSpots[1][1] && !badSpots[1][2];
				boolean eIn = !badSpots[2][1] && !badSpots[2][2];
				boolean eOut = !badSpots[3][1] && !badSpots[3][2];
				if (sOut && nOut) {
					call = volcanicTorchesNSOut;
				}
				else if (sOut && nIn) {
					call = volcanicTorchesNInSOut;
				}
				else if (sIn && nOut) {
					call = volcanicTorchesNOutSIn;
				}
				else if (sIn && nIn && wIn && eIn) {
					call = volcanicTorchesCenter;
				}
				else if (wOut && eOut) {
					call = volcanicTorchesEWOut;
				}
				else if (wIn && eOut) {
					call = volcanicTorchesWInEOut;
				}
				else if (wOut && eIn) {
					call = volcanicTorchesWOutEIn;
				}
				else {
					call = volcanicTorchesError;
				}
				s.updateCall(call.getModified(sample));
			});

	@AutoFeed
	private final SequentialTrigger<BaseEvent> volcanicTorches2sq = SqtTemplates.sq(15_000,
			AbilityCastStart.class, acs -> acs.abilityIdMatches(0x791E, 31007),
			(e1, s) -> {
				log.info("Torches 2 Start");
				List<AbilityCastStart> casts = s.waitEvents(15, AbilityCastStart.class, acs -> acs.abilityIdMatches(0x7927));
				AbilityCastStart sample = casts.get(0);
				s.waitThenRefreshCombatants(100);
				// x, y
				boolean[][] badSpots = computeTorchFlameBadSpots(casts);
				ArenaSector safeSpot;
				if (!badSpots[0][0]) {
					safeSpot = ArenaSector.NORTHWEST;
				}
				else if (!badSpots[3][0]) {
					safeSpot = ArenaSector.NORTHEAST;
				}
				else if (!badSpots[0][3]) {
					safeSpot = ArenaSector.SOUTHWEST;
				}
				else if (!badSpots[3][3]) {
					safeSpot = ArenaSector.SOUTHEAST;
				}
				else {
					log.info("Volcanic Torches 2: Bad Safe Spots!");
					s.updateCall(volcanicTorchesError.getModified(sample));
					return;
				}
				log.info("Volcanic Torches 2: Done! {}", safeSpot);
				s.updateCall(volcanicTorches2SafeSpot.getModified(sample, Map.of("corner", safeSpot)));
			});

	@AutoFeed
	private final SequentialTrigger<BaseEvent> rearingRampageNumSq = SqtTemplates.sq(60_000,
			AbilityCastStart.class, acs -> acs.abilityIdMatches(0x7933),
			(e1, s) -> {
				int playerNum = 0;
				boolean firstCallDone = false;
				AbilityUsedEvent last = null;
				XivCombatant partner = null;
				for (int i = 1; i <= 4; i++) {
					List<AbilityUsedEvent> upliftHits = s.waitEventsQuickSuccession(2, AbilityUsedEvent.class, aue -> aue.abilityIdMatches(0x7935) && aue.isFirstTarget(), Duration.ofMillis(100));
					last = upliftHits.get(0);
					// You *can* get hit more than once if people are dead, so just skip over subsequent calls
					if (firstCallDone) {
						continue;
					}
					AbilityUsedEvent playerHit;
					playerHit = upliftHits.stream().filter(hit -> hit.getTarget().isThePlayer()).findAny().orElse(null);
					if (playerHit != null) {
						// Not sure if we actually need to account for the possibility that there's only 1 hit, seems like a guaranteed wipe at that point
						partner = upliftHits.stream().filter(hit -> !hit.getTarget().isThePlayer()).findAny().map(AbilityUsedEvent::getTarget).orElse(null);
						firstCallDone = true;
						playerNum = i;
						s.updateCall(upliftNumber.getModified(playerHit, Map.of("num", playerNum, "partner", partner == null ? "null" : partner)));
					}
				}
				if (playerNum == 0) {
					log.warn("Uplift: Player did not get hit! Maybe they're dead?");
					return;
				}
				// #1 doesn't need to wait for anything, they can go as soon as the uplifts are done
				Map<String, Object> params = Map.of("partner", partner == null ? "null" : partner);
				if (playerNum == 1) {
					s.updateCall(upliftBait.getModified(last, params));
				}
				for (int i = 2; i <= 4; i++) {
					AbilityUsedEvent event = s.waitEvent(AbilityUsedEvent.class, aue -> aue.abilityIdMatches(0x7937) && aue.isFirstTarget());
					if (i == playerNum) {
						s.updateCall(upliftBait.getModified(event, params));
					}
				}
			});


//	@AutoFeed
//	private final SequentialTrigger<BaseEvent> cthonicVent = new SequentialTrigger<>(
//			10_000,
//			BaseEvent.class, event -> event instanceof AbilityCastStart acs && acs.abilityIdMatches(0x0, 0x0, 0x0), //????, ????+88(?), ????+1
//			(e1, s) -> {
//				List<AbilityCastStart> cthonicCasts = new ArrayList<>(s.waitEvents(1, AbilityCastStart.class, event -> event.abilityIdMatches(0x0, 0x0, 0x0))); // same as above
//				cthonicCasts.add((AbilityCastStart) e1);
//				List<XivCombatant> suneaters = new ArrayList<>();
//				log.info("CthonicVent: Got suneater casts");
//				s.waitMs(100);
//				s.refreshCombatants(100);
//				log.info("CthonicVent: done with delay");
//				for(AbilityCastStart acs : cthonicCasts) {
//					suneaters.add(this.getState().getLatestCombatantData(acs.getSource()));
//				}
//				log.info("CthonicVent: done finding positions, finding safe spots");
//				if(suneaters.size() != 2) {
//					log.error("Invalid number of suneaters found! Data: {}", cthonicCasts);
//					return;
//				}
//				log.info("CthonicVent: found suneaters 1:{}, 2:{}", arenaPos.forCombatant(suneaters.get(0)).getFriendlyName(), arenaPos.forCombatant(suneaters.get(1)).getFriendlyName());
//				Set<ArenaSector> safe = EnumSet.copyOf(ArenaSector.quadrants);
//				safe.remove(arenaPos.forCombatant(suneaters.get(0)));
//				safe.remove(arenaPos.forCombatant(suneaters.get(1)));
//				ArenaSector combined = ArenaSector.tryCombineTwoQuadrants(new ArrayList<>(safe));
//
//				Map<String, Object> args = Map.of("safe", combined == null ? safe : combined);
//				s.accept(fourfoldFiresSafe.getModified(cthonicCasts.get(0), args));
//			}
//	);

	// TODO: generic torch flame
	// TODO: single and dual sunforge
	// Scorched Pinion 7953

//	private final SequentialTrigger<BaseEvent> }

}