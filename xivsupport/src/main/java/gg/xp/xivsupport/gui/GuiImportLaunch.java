package gg.xp.xivsupport.gui;

import gg.xp.xivsupport.events.fflogs.FflogsController;
import gg.xp.xivsupport.events.fflogs.FflogsReportLocator;
import gg.xp.xivsupport.eventstorage.EventReader;
import gg.xp.xivsupport.gui.components.ReadOnlyText;
import gg.xp.xivsupport.gui.tables.filters.TextFieldWithValidation;
import gg.xp.xivsupport.gui.util.CatchFatalError;
import gg.xp.xivsupport.persistence.Platform;
import gg.xp.xivsupport.sys.XivMain;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class GuiImportLaunch {
	private static final Logger log = LoggerFactory.getLogger(GuiImportLaunch.class);

	private GuiImportLaunch() {
	}

	public static void main(String[] args) {
		log.info("GUI Import Init");
		CatchFatalError.run(CommonGuiSetup::setup);
		SwingUtilities.invokeLater(() -> CatchFatalError.run(() -> {
			JFrame frame = new JFrame("Triggevent Import");
			frame.setLocationByPlatform(true);
			JPanel panel = new TitleBorderFullsizePanel("Import");

			JCheckBox decompressCheckbox = new JCheckBox("Decompress events (uses more memory)");
			Path sessionsDir = Paths.get(Platform.getTriggeventDir().toString(), "sessions");
			JFileChooser sessionChooser = new JFileChooser(sessionsDir.toString());
			sessionChooser.setPreferredSize(new Dimension(800, 600));
			JButton importSessionButton = new JButton("Import Session");
			importSessionButton.addActionListener(e -> {
				int result = sessionChooser.showOpenDialog(panel);
				if (result == JFileChooser.APPROVE_OPTION) {
					File file = sessionChooser.getSelectedFile();
					// TODO: this should be async
					CatchFatalError.run(() -> {
						LaunchImportedSession.fromEvents(EventReader.readEventsFromFile(file), decompressCheckbox.isSelected());
					});
					frame.setVisible(false);
				}
			});


			Path actLogDir = Platform.getActDir();
			JFileChooser actLogChooser = new JFileChooser(actLogDir.toString());
			actLogChooser.setPreferredSize(new Dimension(800, 600));
			JButton importActLogButton = new JButton("Import ACT Log");
			importActLogButton.addActionListener(e -> {
				int result = actLogChooser.showOpenDialog(panel);
				if (result == JFileChooser.APPROVE_OPTION) {
					File file = actLogChooser.getSelectedFile();
					// TODO: this should be async
					CatchFatalError.run(() -> {
						LaunchImportedActLog.fromEvents(EventReader.readActLogFile(file), decompressCheckbox.isSelected());
					});
					frame.setVisible(false);
				}
			});

			FflogsController fflogsController = new FflogsController(XivMain.persistenceProvider());
			boolean hasApiKey = !(fflogsController.clientSecret().get().isEmpty()
					|| fflogsController.clientSecret().get().isEmpty());

			JButton importFflogsButton = new JButton("Import from FFLogs");
			Mutable<FflogsReportLocator> locator = new MutableObject<>();
			TextFieldWithValidation<FflogsReportLocator> fflogsUrlField = new TextFieldWithValidation<>(url -> {
				FflogsReportLocator result;
				try {
					result = FflogsReportLocator.fromURL(url);
				}
				catch (Throwable t) {
					importFflogsButton.setEnabled(false);
					throw t;
				}
				importFflogsButton.setEnabled(true);
				return result;
			}, locator::setValue, "Paste your FFLogs URL here");
			importFflogsButton.addActionListener(l -> {
				CatchFatalError.run(() -> {
					LaunchImportedFflogs.fromUrl(locator.getValue());
				});
				frame.setVisible(false);
			});
			fflogsUrlField.setEnabled(hasApiKey);
			importFflogsButton.setEnabled(hasApiKey);
			ReadOnlyText fflogsImportText = new ReadOnlyText("In order to use FFLogs importing, launch the client normally, and then enter your API keys in Advanced > FFLogs.");

			panel.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();

			c.gridx = 0;
			c.gridy = 0;
			Insets fullInsets = new Insets(10, 10, 10, 10);
			c.insets = fullInsets;
			c.fill = GridBagConstraints.HORIZONTAL;
			{
				c.weightx = 1;
				c.gridwidth = GridBagConstraints.REMAINDER;
				JLabel importLabel = new JLabel("Please select a file to import");
				panel.add(importLabel, c);
			}
			{
				c.anchor = GridBagConstraints.LINE_START;
				c.gridy++;
				c.gridwidth = 1;
				c.weightx = 0;
				panel.add(importSessionButton, c);
				c.gridx++;
				c.weightx = 1;
				panel.add(new JLabel("Import a Triggevent Session"), c);
			}
			{
				c.gridy++;
				c.gridx = 0;
				c.gridwidth = 1;
				c.weightx = 0;
				panel.add(importActLogButton, c);
				c.gridx++;
				c.weightx = 1;
				panel.add(new JLabel("Import an ACT Log file"), c);
			}{
				c.gridx = 0;
				c.gridy++;
				c.gridwidth = GridBagConstraints.REMAINDER;
				panel.add(fflogsUrlField, c);
				c.insets = new Insets(0, 10, 10, 10);
				c.gridy++;
				c.gridx = 0;
				c.gridwidth = 1;
				c.weightx = 0;
				panel.add(importFflogsButton, c);
				c.gridx++;
				c.weightx = 1;
				panel.add(new JLabel("Import an FFLogs URL"), c);
				if (!hasApiKey) {
					c.gridx = 0;
					c.gridy++;
					c.gridwidth = GridBagConstraints.REMAINDER;
					panel.add(fflogsImportText, c);
				}
			}
			{
				c.insets = fullInsets;
				c.gridy++;
				c.weighty = 0;
				c.gridx = 0;
				c.gridwidth = 1;
				c.gridwidth = GridBagConstraints.REMAINDER;

				panel.add(decompressCheckbox, c);
			}
//			{
//				c.gridy++;
//				c.weighty = 0;
//				c.gridx = 0;
//				c.gridwidth = GridBagConstraints.REMAINDER;
//				panel.add(new ReadOnlyText("In replay mode, the program will use your existing settings, but any changes you make will not be saved."), c);
//			}
			{
				c.gridy++;
				c.weighty = 1;
				// Filler
				panel.add(new JPanel(), c);
			}


			frame.add(panel);
			frame.setSize(new Dimension(400, 400));
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.setVisible(true);
		}));

	}
}
