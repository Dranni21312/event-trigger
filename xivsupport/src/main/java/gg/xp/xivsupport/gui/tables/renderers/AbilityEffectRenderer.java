package gg.xp.xivsupport.gui.tables.renderers;

import gg.xp.xivdata.data.ActionLibrary;
import gg.xp.xivdata.data.HasIconURL;
import gg.xp.xivdata.data.StatusEffectLibrary;
import gg.xp.xivsupport.events.actlines.events.abilityeffect.AbilityEffect;
import gg.xp.xivsupport.events.actlines.events.abilityeffect.BlockedDamageEffect;
import gg.xp.xivsupport.events.actlines.events.abilityeffect.DamageTakenEffect;
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
		if (value instanceof DamageTakenEffect dte) {
			text = dte.getSeverity().getSymbol() + dte.getAmount();
			icon = ActionLibrary.iconForId(9);
		}
		else if (value instanceof HealEffect heal) {
			text = heal.getSeverity().getSymbol() + heal.getAmount();
			icon = ActionLibrary.iconForId(3594);
		}
		else if (value instanceof MpGain mpGain) {
			text = "+" + mpGain.getAmount();
			icon = ActionLibrary.iconForId(7562);
		}
		else if (value instanceof MpLoss mpLoss) {
			text = "-" + mpLoss.getAmount();
			icon = ActionLibrary.iconForId(7562);
		}
		else if (value instanceof ParriedDamageEffect pde) {
			// TODO: can blocked/parried also be crit/dhit/etc?
			text = Long.toString(pde.getAmount());
			icon = ActionLibrary.iconForId(16140);
		}
		else if (value instanceof BlockedDamageEffect bde) {
			// TODO: can blocked/parried also be crit/dhit/etc?
			text = Long.toString(bde.getAmount());
			icon = ActionLibrary.iconForId(3542);
		}
		else if (value instanceof InvulnBlockedDamageEffect invuln) {
			// TODO: can blocked/parried also be crit/dhit/etc?
			text = Long.toString(invuln.getAmount());
			icon = ActionLibrary.iconForId(30);
		}
		else if (value instanceof StatusAppliedEffect statusApplied) {
			text = "+";
			icon = StatusEffectLibrary.iconForId(statusApplied.getStatus().getId(), statusApplied.getStacks());
			textOnRight = true;
		}
		else if (value instanceof StatusNoEffect sne) {
			// TODO: does this also have a 'stacks' value?
			text = "X";
			icon = StatusEffectLibrary.iconForId(sne.getStatus().getId(), 1);
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
		return Collections.unmodifiableList(components);
	}
}
