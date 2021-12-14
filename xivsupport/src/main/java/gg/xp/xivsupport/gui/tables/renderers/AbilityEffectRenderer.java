package gg.xp.xivsupport.gui.tables.renderers;

import gg.xp.xivdata.jobs.ActionIcon;
import gg.xp.xivdata.jobs.HasIconURL;
import gg.xp.xivdata.jobs.StatusEffectIcon;
import gg.xp.xivsupport.events.actlines.events.abilityeffect.AbilityEffect;
import gg.xp.xivsupport.events.actlines.events.abilityeffect.BlockedDamageEffect;
import gg.xp.xivsupport.events.actlines.events.abilityeffect.DamageEffect;
import gg.xp.xivsupport.events.actlines.events.abilityeffect.HealEffect;
import gg.xp.xivsupport.events.actlines.events.abilityeffect.InvulnBlockedDamageEffect;
import gg.xp.xivsupport.events.actlines.events.abilityeffect.MpGain;
import gg.xp.xivsupport.events.actlines.events.abilityeffect.MpLoss;
import gg.xp.xivsupport.events.actlines.events.abilityeffect.ParriedDamageEffect;
import gg.xp.xivsupport.events.actlines.events.abilityeffect.StatusAppliedEffect;
import gg.xp.xivsupport.events.actlines.events.abilityeffect.StatusNoEffect;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AbilityEffectRenderer {
	private final TableCellRenderer fallback = new DefaultTableCellRenderer();
	private final boolean iconOnly;

	public AbilityEffectRenderer(boolean iconOnly) {
		this.iconOnly = iconOnly;
	}

	public List<Component> getTableCellRendererComponents(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

		Component defaultLabel;
		if (!(value instanceof AbilityEffect)) {
			return Collections.singletonList(fallback.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column));
		}
		String text;
		HasIconURL icon;
		boolean textOnRight = false;
		if (value instanceof DamageEffect) {
			text = ((DamageEffect) value).getSeverity().getSymbol() + ((DamageEffect) value).getAmount();
			icon = ActionIcon.forId(9);
		}
		else if (value instanceof HealEffect) {
			text = ((HealEffect) value).getSeverity().getSymbol() + ((HealEffect) value).getAmount();
			icon = ActionIcon.forId(3594);
		}
		else if (value instanceof MpGain) {
			text = "+" + ((MpGain) value).getAmount();
			icon = ActionIcon.forId(7562);
		}
		else if (value instanceof MpLoss) {
			text = "-" + ((MpLoss) value).getAmount();
			icon = ActionIcon.forId(7562);
		}
		else if (value instanceof ParriedDamageEffect) {
			// TODO: can blocked/parried also be crit/dhit/etc?
			text = Long.toString(((ParriedDamageEffect) value).getAmount());
			icon = ActionIcon.forId(16140);
		}
		else if (value instanceof BlockedDamageEffect) {
			// TODO: can blocked/parried also be crit/dhit/etc?
			text = Long.toString(((BlockedDamageEffect) value).getAmount());
			icon = ActionIcon.forId(3542);
		}
		else if (value instanceof InvulnBlockedDamageEffect) {
			// TODO: can blocked/parried also be crit/dhit/etc?
			text = Long.toString(((InvulnBlockedDamageEffect) value).getAmount());
			icon = ActionIcon.forId(30);
		}
		else if (value instanceof StatusAppliedEffect) {
			text = "+";
			icon = StatusEffectIcon.forId(((StatusAppliedEffect) value).getStatus().getId());
			textOnRight = true;
		}
		else if (value instanceof StatusNoEffect) {
			text = "X";
			icon = StatusEffectIcon.forId(((StatusNoEffect) value).getStatus().getId());
			textOnRight = true;
		}
		else {
			return Collections.singletonList(fallback.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column));
		}
		defaultLabel = fallback.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);
		if (iconOnly) {
			return Collections.singletonList(IconTextRenderer.getComponent(icon, defaultLabel, true, textOnRight, true));
		}
		List<Component> components = new ArrayList<>();
		if (textOnRight) {
			components.add(fallback.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column));
			components.add(IconTextRenderer.getComponent(icon, defaultLabel, true, false, true));
		}
		else {
			components.add(IconTextRenderer.getComponent(icon, defaultLabel, true, false, true));
			components.add(fallback.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column));
		}
		return components;
	}
}
