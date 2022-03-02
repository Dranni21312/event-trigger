package gg.xp.xivsupport.events.actlines.events.abilityeffect;

import gg.xp.xivdata.data.StatusEffectLibrary;
import gg.xp.xivsupport.models.XivStatusEffect;

public class StatusAppliedEffect extends AbilityEffect {
	private final XivStatusEffect status;
	private final int rawStacks;
	private final int stacks;
	private final boolean onTarget;

	public StatusAppliedEffect(long flags, long value, long id, int rawStacks, boolean onTarget) {
		super(flags, value, AbilityEffectType.APPLY_STATUS);
		// TODO: get actual name
		this.status = new XivStatusEffect(id, "");
		this.rawStacks = rawStacks;
		this.onTarget = onTarget;
		this.stacks = StatusEffectLibrary.calcActualStacks(id, rawStacks);

	}

	@Override
	public String toString() {
		if (isOnTarget()) {
			return String.format("S(0x%x)", status.getId());
		}
		else {
			return String.format("Sb(0x%x)", status.getId());
		}
	}

	public XivStatusEffect getStatus() {
		return status;
	}

	public boolean isOnTarget() {
		return onTarget;
	}

	public int getRawStacks() {
		return rawStacks;
	}

	public int getStacks() {
		return stacks;
	}

	@Override
	public String getBaseDescription() {
		String formatted = String.format("Applied Status 0x%x to %s", status.getId(), onTarget ? "Target" : "Caster");
		if (stacks > 0) {
			formatted += String.format(" (%s stacks)", stacks);
		}
		return formatted;
	}
}
