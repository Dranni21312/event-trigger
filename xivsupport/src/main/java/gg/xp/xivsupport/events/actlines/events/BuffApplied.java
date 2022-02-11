package gg.xp.xivsupport.events.actlines.events;

import gg.xp.reevent.events.BaseEvent;
import gg.xp.xivdata.jobs.ActionIcon;
import gg.xp.xivdata.jobs.StatusEffectIcon;
import gg.xp.xivsupport.events.actlines.events.abilityeffect.StatusAppliedEffect;
import gg.xp.xivsupport.models.XivCombatant;
import gg.xp.xivsupport.models.XivStatusEffect;

import java.io.Serial;
import java.time.Duration;

// TODO: track new application vs refresh
// Note that stacks decreasing (e.g. Embolden) still counts as "Application".
public class BuffApplied extends BaseEvent implements HasSourceEntity, HasTargetEntity, HasStatusEffect, HasDuration {
	@Serial
	private static final long serialVersionUID = -3698392943125561045L;
	private final XivStatusEffect buff;
	private final Duration duration;
	private final XivCombatant source;
	private final XivCombatant target;
	private final long stacks;
	private final long rawStacks;
	private final boolean isPreApp;
	private boolean isRefresh;


	// Only for pre-apps
	public BuffApplied(AbilityUsedEvent event, StatusAppliedEffect effect) {
		this(effect.getStatus(), 9999, event.getSource(), event.getTarget(), 1, true);
	}

	public BuffApplied(XivStatusEffect buff, double durationRaw, XivCombatant source, XivCombatant target, long stacks) {
		this(buff, durationRaw, source, target, stacks, false);
	}

	public BuffApplied(XivStatusEffect buff, double durationRaw, XivCombatant source, XivCombatant target, long rawStacks, boolean isPreApp) {
		this.buff = buff;
		this.duration = Duration.ofMillis((long) (durationRaw * 1000.0));
		this.source = source;
		this.target = target;
		this.rawStacks = rawStacks;
		long maxStacks = StatusEffectIcon.getCsvValues().get(buff.getId()).getNumStacks();
		if (rawStacks >= 0 && rawStacks <= maxStacks) {
			stacks = rawStacks;
		}
		else {
			stacks = 0;
		}
		this.isPreApp = isPreApp;
	}

	@Override
	public XivStatusEffect getBuff() {
		return buff;
	}

	@Override
	public Duration getInitialDuration() {
		return duration;
	}


	@Override
	public XivCombatant getSource() {
		return source;
	}

	@Override
	public XivCombatant getTarget() {
		return target;
	}

	public long getRawStacks() {
		return rawStacks;
	}

	@Override
	public long getStacks() {
		return stacks;
	}

	public boolean isRefresh() {
		return isRefresh;
	}

	public void setIsRefresh(boolean refresh) {
		isRefresh = refresh;
	}

	public boolean isPreApp() {
		return isPreApp;
	}

	@Override
	public String toString() {
		return "BuffApplied{" +
				"buff=" + buff +
				", duration=" + duration +
				", source=" + source +
				", target=" + target +
				", stacks=" + rawStacks +
				", isRefresh=" + isRefresh +
				'}';
	}
}
