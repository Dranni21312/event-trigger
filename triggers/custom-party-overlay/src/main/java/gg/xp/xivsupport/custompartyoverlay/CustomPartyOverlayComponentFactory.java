package gg.xp.xivsupport.custompartyoverlay;

import gg.xp.reevent.scan.ScanMe;
import gg.xp.xivsupport.events.actionresolution.SequenceIdTracker;
import gg.xp.xivsupport.events.state.combatstate.ActiveCastRepository;
import gg.xp.xivsupport.events.state.combatstate.StatusEffectRepository;
import gg.xp.xivsupport.gui.tables.StandardColumns;
import gg.xp.xivsupport.persistence.settings.BooleanSetting;
import org.jetbrains.annotations.Nullable;

@ScanMe
public class CustomPartyOverlayComponentFactory {

	private final StatusEffectRepository buffs;
	private final BooleanSetting showPredictedHp;
	private final ActiveCastRepository acr;
	private final SequenceIdTracker sqidTracker;

	public CustomPartyOverlayComponentFactory(
			StatusEffectRepository buffs,
			StandardColumns cols,
			ActiveCastRepository acr,
			SequenceIdTracker sqidTracker
	) {
		this.buffs = buffs;
		showPredictedHp = cols.getShowPredictedHp();
		this.acr = acr;
		this.sqidTracker = sqidTracker;
	}

	public @Nullable RefreshablePartyListComponent makeComponent(CustomOverlayComponentSpec spec) {
		if (!spec.enabled) {
			return null;
		}
		CustomPartyOverlayComponentType type = spec.componentType;
		if (type == CustomPartyOverlayComponentType.BUFFS_WITH_TIMERS) {
			return new BuffsWithTimersComponent(buffs, true);
		}
		else if (false){
			return null;
		}
		RefreshablePartyListComponent component;
		component = switch (type) {
			case NOTHING -> new DoNothingComponent();
			case NAME -> new NameComponent();
			case JOB -> new JobComponent();
			case HP -> new HpBarComponent(showPredictedHp, sqidTracker);
			case BUFFS -> new BuffsWithTimersComponent(buffs, false);
			case BUFFS_WITH_TIMERS -> new BuffsWithTimersComponent(buffs, true);
			case CAST_BAR -> new CastBarPartyComponent(acr);
			case MP_BAR -> new MpBarComponent();
		};
		return component;
	}
}
