package gg.xp.xivsupport.triggers.Arcadion;

import gg.xp.reevent.events.BaseEvent;
import gg.xp.reevent.events.EventContext;
import gg.xp.reevent.scan.AutoChildEventHandler;
import gg.xp.reevent.scan.AutoFeed;
import gg.xp.reevent.scan.FilteredEventHandler;
import gg.xp.xivdata.data.*;
import gg.xp.xivdata.data.duties.*;
import gg.xp.xivsupport.callouts.CalloutRepo;
import gg.xp.xivsupport.callouts.ModifiableCallout;
import gg.xp.xivsupport.callouts.RawModifiedCallout;
import gg.xp.xivsupport.events.actlines.events.AbilityCastStart;
import gg.xp.xivsupport.events.actlines.events.AbilityUsedEvent;
import gg.xp.xivsupport.events.actlines.events.BuffApplied;
import gg.xp.xivsupport.events.state.XivState;
import gg.xp.xivsupport.events.state.combatstate.StatusEffectRepository;
import gg.xp.xivsupport.events.triggers.seq.SequentialTrigger;
import gg.xp.xivsupport.events.triggers.seq.SqtTemplates;
import gg.xp.xivsupport.events.triggers.support.NpcCastCallout;
import gg.xp.xivsupport.models.ArenaPos;
import gg.xp.xivsupport.models.ArenaSector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@CalloutRepo(name = "M3S", duty = KnownDuty.M3S)
public class M3S extends AutoChildEventHandler implements FilteredEventHandler {
	private static final Logger log = LoggerFactory.getLogger(M3S.class);

	public M3S(XivState state, StatusEffectRepository buffs) {
		this.state = state;
		this.buffs = buffs;
	}

	private XivState state;
	private StatusEffectRepository buffs;
	private static final ArenaPos ap = new ArenaPos(100, 100, 5, 5);

	@Override
	public boolean enabled(EventContext context) {
		return state.dutyIs(KnownDuty.M3S);
	}

	// TODO
	private static final long TODO = 0;

	@NpcCastCallout(0x93EB)
	private final ModifiableCallout<AbilityCastStart> quadrupleLariatIn = ModifiableCallout.durationBasedCall("Quadruple Lariat In", "In and Partners");
	@NpcCastCallout(0x93EA)
	private final ModifiableCallout<AbilityCastStart> quadrupleLariatOut = ModifiableCallout.durationBasedCall("Quadruple Lariat Out", "Out and Partners");
	// Boss cast is 93D8 but it is shorter duration
	@NpcCastCallout(0x93E9)
	private final ModifiableCallout<AbilityCastStart> octupleLariatIn = ModifiableCallout.durationBasedCall("Octuple Lariat In", "In and Spread");
	@NpcCastCallout(0x93E8)
	private final ModifiableCallout<AbilityCastStart> octupleLariatOut = ModifiableCallout.durationBasedCall("Octuple Lariat Out", "Out and Spread");
	@NpcCastCallout(0x9425)
	private final ModifiableCallout<AbilityCastStart> brutalImpact = ModifiableCallout.durationBasedCall("Brutal Impact", "Raidwide - Multi Hit");
	@NpcCastCallout(0x9423)
	private final ModifiableCallout<AbilityCastStart> knuckleSandwich = ModifiableCallout.durationBasedCall("Knuckle Sandwich", "Tank Buster - Multi Hit");

	// Real is 93E0
	@NpcCastCallout(0x93F5)
	private final ModifiableCallout<AbilityCastStart> quadroboomDiveOut = ModifiableCallout.durationBasedCall("Quadroboom Dive (Out)", "Out into Role Pairs");
	@NpcCastCallout(0x93F6)
	private final ModifiableCallout<AbilityCastStart> quadroboomDiveKb = ModifiableCallout.durationBasedCall("Quadroboom Dive (KB)", "Knockback into Role Pairs");
	// Real is 93EF
	@NpcCastCallout(0x93EC)
	private final ModifiableCallout<AbilityCastStart> octoboomDiveOut = ModifiableCallout.durationBasedCall("Quadroboom Dive (Out)", "Out into Spreads");
	@NpcCastCallout(0x93ED)
	private final ModifiableCallout<AbilityCastStart> octoboomDiveKb = ModifiableCallout.durationBasedCall("Quadroboom Dive (KB)", "Knockback into Spreads");

	private final ModifiableCallout<?> tagTeamSafeSpot = new ModifiableCallout<>("Tag Team 1: Safe Spot", "{safe} safe");

	@AutoFeed
	private final SequentialTrigger<BaseEvent> tagTeam = SqtTemplates.multiInvocation(60_000,
			AbilityCastStart.class, acs -> acs.abilityIdMatches(0x93E7),
			(e1, s) -> {
				log.info("Tag team: start");
				// The tether units do not move into place until after the "Chain Deathmatch" cast starts
//				var tether = s.waitEvent(TetherEvent.class, te -> te.eitherTargetMatches(XivCombatant::isThePlayer));
//				var tetherAdd = tether.getTargetMatching(cbt -> !cbt.isThePlayer());
//				log.info("Tag team: Tethered to: {}", tetherAdd);
//				var otherAdd = state.npcsById(tetherAdd.getbNpcId()).stream().filter(cbt -> !(cbt.equals(tetherAdd))).findFirst().orElseThrow(() -> new RuntimeException("Could not find other add"));
//				log.info("Tag team: Other add: {} at {}", otherAdd, otherAdd.getPos());

				// The tether comes out earlier, but we can't do anything with the information anyway, so just use the
				// buff instead since we don't have to figure out what the real unit is.
				BuffApplied myBuff = s.waitEvent(BuffApplied.class, ba -> ba.buffIdMatches(0xFB3) && ba.getTarget().isThePlayer());
				List<AbilityCastStart> casts = s.waitEventsQuickSuccession(2, AbilityCastStart.class, acs -> acs.abilityIdMatches(0x9b2c, 0x9b3e));

				// The one that we want to get hit by
				AbilityCastStart goodCast = casts.stream().filter(acs -> acs.getSource().equals(myBuff.getSource())).findFirst().orElseThrow();
				// The one that we don't want to get hit by
				AbilityCastStart badCast = casts.stream().filter(acs -> !acs.getSource().equals(myBuff.getSource())).findFirst().orElseThrow();

				ArenaSector goodArea = ap.forCombatant(goodCast.getSource()).plusQuads(
						// If the enemy is hitting the right, that is 1 quarter CCW
						goodCast.abilityIdMatches(0x9b2c) ? -1 : 1
				);
				ArenaSector badArea = ap.forCombatant(badCast.getSource()).plusQuads(
						// If the enemy is hitting the right, that is 1 quarter CCW
						badCast.abilityIdMatches(0x9b2c) ? -1 : 1
				);

				// If good is N, and bad is E, then the spot we want is W, so we take the arc from good to bad, and
				// go the opposite way.
				ArenaSector neededSpot = goodArea.plusEighths(-goodArea.eighthsTo(badArea));

				s.setParam("safe", neededSpot);
				s.updateCall(tagTeamSafeSpot);

				// Next part is to avoid both hits - is this always the spot that got double hit initially?
				// The hits aren't obvious - maybe it's 9B34 lariat combo?
				// 9b34/9b2c might just be right?
				// 9b35/9b2e might be left?
				// At least one seems to be missing its cast location data
				// 2c/2e seem to come from the same units that the buff comes from
			}, (e1, s) -> {
				// Double tether version

			});

	@AutoFeed
	private final SequentialTrigger<BaseEvent> finalFusedown = SqtTemplates.sq(60_000,
			AbilityCastStart.class, acs -> acs.abilityIdMatches(0x9406),
			(e1, s) -> {
				log.info("Final fusedown: start");
				s.waitEvent(AbilityUsedEvent.class, aue -> aue.getPrecursor() == e1);
				s.waitMs(1000);
				var playerBuff = buffs.findStatusOnTarget(state.getPlayer(), ba -> ba.buffIdMatches(0xFAF, 0xFB0));
				if (playerBuff == null) {
					throw new RuntimeException("No debuff on player!");
				}
				// Player has long
				boolean playerLong = playerBuff.buffIdMatches(0xFB0);
				if (state.playerJobMatches(Job::isDps)) {
					// DPS has long
					log.info("DPS long");
				}
				else {
					// Supports have long
					log.info("Supports long");
				}
				// TODO: determine safe spot
				if (playerLong) {

				}
			});

	private final ModifiableCallout<AbilityCastStart> fuseFieldInitial = ModifiableCallout.durationBasedCall("Fuse Field: Initial", "Pop Fuses Sequentially");
	private final ModifiableCallout<BuffApplied> fuseFieldShort = ModifiableCallout.<BuffApplied>durationBasedCall("Fuse Field: Short", "Short Fuse").autoIcon();
	private final ModifiableCallout<BuffApplied> fuseFieldLong = ModifiableCallout.<BuffApplied>durationBasedCall("Fuse Field: Long", "Long Fuse").autoIcon();
	@AutoFeed
	private final SequentialTrigger<BaseEvent> fuseField = SqtTemplates.sq(60_000,
			AbilityCastStart.class, acs -> acs.abilityIdMatches(0x93EE),
			(e1, s) -> {
				s.updateCall(fuseFieldInitial, e1);
				var buff = s.waitEvent(BuffApplied.class, ba -> ba.getTarget().isThePlayer() && ba.buffIdMatches(0xFB4));
				RawModifiedCallout<BuffApplied> call;
				if (buff.getInitialDuration().toSeconds() > 30) {
					call = s.updateCall(fuseFieldLong, buff);
				}
				else {
					call = s.updateCall(fuseFieldShort, buff);
				}
				s.waitBuffRemoved(buffs, buff);
				call.forceExpire();
			});

	// Bombs: role spread + avoid bombs

	@AutoFeed
	private final SequentialTrigger<BaseEvent> quadroboomSpecial = SqtTemplates.sq(60_000,
			AbilityCastStart.class, acs -> acs.abilityIdMatches(0x940A),
			(e1, s) -> {

			});


	// debuffs + bombs
	// seems to be role assigned
	// FB1, FB2 and FBA, FBB debuffs seem to be related
	// FAF = 5 second FB1 on NPC
	// FB0 = 10 second FB2 on NPC
	// FB8 = 5 second FBA on player
	// FB9 = 10 second FBB on player
	// short debuff to short fuse
	// It's curtain call but with 2 debuff timers instead of four


	/*
	Knockback towers:
	Some towers go off earlier and you need to get knocked into the next set of towers
	Boss does a 270% cleave, behind him is safe? so get knocked back to him
	Pattern 1: sides, corners, center
	Pattern 2:
	 */
	/*
	Second chain - chained to boss and add, so need to get hit by boss's 270 degree cleave (then still dodge second set)

	 */
	/*
	Spinny mechanic TBD
	need to look at rotation and initial position, plus the cross-tthrough
	 */

	/*
	TODO: bombarian specials

	There is a towers + kb mechanic, but you dodge a side cleave instead of 270 at the end
	 */
}


