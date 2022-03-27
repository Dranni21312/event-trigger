package gg.xp.xivsupport.gui.overlay;

import gg.xp.xivsupport.persistence.PersistenceProvider;

import javax.swing.*;
import java.awt.*;

//@ScanMe
public class ExampleOverlay extends XivOverlay {

	public ExampleOverlay(PersistenceProvider persistence, OverlayConfig oc) {
		super("Example Overlay", "example-overlay2", oc, persistence);
		JPanel panel = new JPanel();
		JButton button = new JButton("Foo");
		button.addActionListener(l -> this.dummyMethodForBreakpoint());
		panel.add(button);
		panel.add(new JLabel("Foo Bar Label Here"));
		panel.setBackground(new Color(200, 100, 0, 255));
		getPanel().add(panel);
	}

	private void dummyMethodForBreakpoint() {
		int foo = 5 + 1;
	}
}
