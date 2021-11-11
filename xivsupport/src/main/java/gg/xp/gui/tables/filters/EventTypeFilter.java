package gg.xp.gui.tables.filters;

import gg.xp.events.Event;

import javax.swing.*;
import java.awt.*;
import java.util.function.Predicate;

public class EventTypeFilter implements VisualFilter<Event> {

	private final JComboBox<FilterOption> comboBox;
	private FilterOption currentOption = FilterOption.BOTH;

	public EventTypeFilter(Runnable filterUpdatedCallback) {
		comboBox = new JComboBox<>(FilterOption.values());
		comboBox.addItemListener(event -> {
			currentOption = (FilterOption) event.getItem();
			filterUpdatedCallback.run();
		});
	}

	@Override
	public boolean passesFilter(Event item) {
		return currentOption.test(item);
	}

	private enum FilterOption implements Predicate<Event> {
		BOTH("Both", e -> true),
		PRIMO("Primo", e -> e.getParent() == null),
		SYNTH("Synthetic",  e -> e.getParent() != null);

		private final String name;
		private final Predicate<Event> pred;

		FilterOption(String name, Predicate<Event> pred) {
			this.name = name;
			this.pred = pred;
		}

		@Override
		public boolean test(Event event) {
			return pred.test(event);
		}
	}

	@Override
	public Component getComponent() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel("Event Type: ");
		label.setLabelFor(comboBox);
		panel.add(label);
		panel.add(comboBox);
		return panel;
	}
}
