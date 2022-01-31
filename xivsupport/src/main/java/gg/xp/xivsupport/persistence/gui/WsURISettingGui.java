package gg.xp.xivsupport.persistence.gui;

import gg.xp.xivsupport.gui.WrapLayout;
import gg.xp.xivsupport.gui.tables.filters.TextFieldWithValidation;
import gg.xp.xivsupport.persistence.settings.ResetMenuOption;
import gg.xp.xivsupport.persistence.settings.WsURISetting;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.Locale;

public class WsURISettingGui {

	private final TextFieldWithValidation<URI> textBox;
	private final WsURISetting setting;
	private final String label;
	private JLabel jLabel;
	private volatile boolean resetInProgress;

	public WsURISettingGui(WsURISetting setting, String label) {
		this.setting = setting;
		textBox = new TextFieldWithValidation<>(str -> {
			URI uri = URI.create(str);
			String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
			if ("ws".equals(scheme) || "wss".equals(scheme)) {
				return uri;
			}
			else {
				throw new IllegalArgumentException("Protocol must be WS or WSS");
			}
		}, this::setNewValue, setting.get().toString());
		textBox.setColumns(20);
		textBox.setComponentPopupMenu(ResetMenuOption.resetOnlyMenu(setting, this::reset));
		this.label = label;
	}

	private void setNewValue(URI newValue) {
		if (!resetInProgress) {
			setting.set(newValue);
		}
	}

	private void reset() {
		resetInProgress = true;
		try {
			textBox.setText(setting.getDefault().toString());
			setting.delete();
		}
		finally {
			resetInProgress = false;
		}
	}

	public Component getTextBoxOnly() {
		return textBox;
	}

	public Component getLabelOnly() {
		if (jLabel == null) {
			jLabel = new JLabel(label);
			jLabel.setLabelFor(textBox);
		}
		return jLabel;
	}

	public Component getResetButton() {
		JButton jButton = new JButton("Reset");
		jButton.addActionListener(l -> {
			// TODO: Kind of bad
			textBox.setText(setting.getDefault().toString());
			setting.resetToDefault();
		});
		return jButton;
	}

	public JPanel getComponent() {
		JPanel box = new JPanel();
		box.setLayout(new WrapLayout());
		box.add(getTextBoxOnly());
		box.add(getResetButton());
		box.add(getLabelOnly());
		box.setMaximumSize(box.getPreferredSize());
		return box;
	}
}
