package gg.xp.xivsupport.gui.tables.renderers;

import gg.xp.xivsupport.models.XivStatusEffect;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StatusEffectsRenderer implements TableCellRenderer {
	private final TableCellRenderer fallback = new DefaultTableCellRenderer();
	private final ActionAndStatusRenderer renderer = new ActionAndStatusRenderer(true, false, false);
	private final ComponentListRenderer listRenderer = new ComponentListRenderer(0);
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if (value instanceof Collection) {
			Component defaultLabel = fallback.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
			Collection<?> coll = (Collection<?>) value;
			if (coll.isEmpty()) {
				return defaultLabel;
			}
			listRenderer.setBackground(defaultLabel.getBackground());
			List<Component> comps = new ArrayList<>();
			StringBuilder tooltipBuilder = new StringBuilder();
			coll.forEach(obj -> {
				Component component = (renderer.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, column));
				comps.add(component);
				if (obj instanceof XivStatusEffect) {
					XivStatusEffect status = (XivStatusEffect) obj;
					tooltipBuilder.append(status.getName());
					long id = status.getId();
					tooltipBuilder.append(" (0x").append(Long.toString(id, 16))
							.append(", ").append(id).append(")\n\n");
				}
			});
			listRenderer.setComponents(comps);
			listRenderer.setToolTipText(tooltipBuilder.toString().stripTrailing());
			return listRenderer;
		}
		return fallback.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}
}
