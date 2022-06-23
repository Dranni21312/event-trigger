package gg.xp.xivsupport.replay.gui;

import gg.xp.xivsupport.gui.WrapLayout;
import gg.xp.xivsupport.gui.overlay.RefreshLoop;
import gg.xp.xivsupport.gui.tables.filters.TextFieldWithValidation;
import gg.xp.xivsupport.persistence.PropertiesFilePersistenceProvider;
import gg.xp.xivsupport.persistence.gui.BooleanSettingGui;
import gg.xp.xivsupport.replay.ReplayController;
import org.picocontainer.MutablePicoContainer;

import javax.swing.*;
import javax.swing.border.TitledBorder;

public final class ReplayControllerGui {

	private final ReplayController controller;
	private final JPanel panel;
	private final JLabel progressLabel;
	private final JButton advanceButton;
	private final JButton playPauseButton;
	private volatile boolean playing;
	private int advanceAmount = 1;

	public ReplayControllerGui(MutablePicoContainer container, ReplayController controller) {
		this.controller = controller;
		{
			panel = new JPanel();
			panel.setLayout(new WrapLayout());
			panel.setBorder(new TitledBorder("Replay Controls"));
		}
		{
			PropertiesFilePersistenceProvider provider = container.getComponent(PropertiesFilePersistenceProvider.class);
			if (provider != null) {
				JCheckBox cb = new BooleanSettingGui(provider.getReadOnlySetting(), "Don't Save Settings").getComponent();
				cb.setToolTipText("When using replays, it can be useful to block settings changes, as things like echo commands may change settings.\nAlso useful if you just want to play around while having an easy way to revert.");
				panel.add(cb);
			}
		}
		{
			TextFieldWithValidation<Integer> textBox = new TextFieldWithValidation<>(Integer::parseInt, i -> this.advanceAmount = i, "1");
			panel.add(textBox);
		}
		{
			advanceButton = new JButton("Advance By");
			advanceButton.addActionListener(e -> controller.advanceByAsync(advanceAmount));
			panel.add(advanceButton);
		}
		{
			playPauseButton = new JButton("Play");
			playPauseButton.addActionListener(e -> {
				if (playing) {
					playing = false;
				}
				else {
					playing = true;
					controller.advanceByAsyncWhile(() -> playing);
				}
				updateAll();
			});
			panel.add(playPauseButton);
		}
		{
			progressLabel = new JLabel();
			panel.add(progressLabel);
		}
		{
			updateAll();
			controller.addCallback(this::refresh);
			new RefreshLoop<>("ReplayControllerRefresher", this, ReplayControllerGui::refresh, (unused) -> 200L).start();
		}
	}

	private void refresh() {
		SwingUtilities.invokeLater(this::updateAll);
	}

	private void updateAll() {
		progressLabel.setText(String.format("Replay: %s / %s", controller.getCurrentPosition(), controller.getCount()));
		if (controller.hasMoreEvents()) {
			if (playing) {
				playPauseButton.setText("Pause");
				playPauseButton.setEnabled(true);
				advanceButton.setEnabled(false);
			}
			else {
				playPauseButton.setText("Play");
				playPauseButton.setEnabled(true);
				advanceButton.setEnabled(true);
			}
		}
		else {
			playPauseButton.setText("Play");
			playPauseButton.setEnabled(false);
			advanceButton.setEnabled(false);
		}
	}

	public JPanel getPanel() {
		return panel;
	}

}
