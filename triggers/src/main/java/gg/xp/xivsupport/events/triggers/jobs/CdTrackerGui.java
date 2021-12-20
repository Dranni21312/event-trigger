package gg.xp.xivsupport.events.triggers.jobs;

import gg.xp.reevent.scan.ScanMe;
import gg.xp.xivdata.jobs.Cooldown;
import gg.xp.xivdata.jobs.Job;
import gg.xp.xivdata.jobs.JobType;
import gg.xp.xivsupport.events.triggers.jobs.gui.BaseCdTrackerGui;
import gg.xp.xivsupport.gui.TitleBorderFullsizePanel;
import gg.xp.xivsupport.gui.WrapLayout;
import gg.xp.xivsupport.gui.extra.PluginTab;
import gg.xp.xivsupport.persistence.gui.BooleanSettingGui;
import gg.xp.xivsupport.persistence.gui.IntSettingSpinner;
import gg.xp.xivsupport.persistence.gui.LongSettingGui;
import gg.xp.xivsupport.persistence.settings.BooleanSetting;
import gg.xp.xivsupport.persistence.settings.IntSetting;
import gg.xp.xivsupport.persistence.settings.LongSetting;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ScanMe
public class CdTrackerGui extends BaseCdTrackerGui {

	private final CdTracker backend;

	public CdTrackerGui(CdTracker backend) {
		this.backend = backend;
	}

	@Override
	public String getTabName() {
		return "Cooldown Tracker";
	}

	@Override
	public int getSortOrder() {
		return 5;
	}

	// TODO: here's how I can do settings in a reasonable way:
	// Have a gui with 5 or so checkboxes (all on/off, this, DPS, healer, tank)
	// Let each callout be selectable (including ctrl/shift)
	// Then cascade changes to multiple callouts

	// TODO: make a single settings class to hold these
	@Override
	protected LongSetting cdAdvance() {
		return backend.getCdTriggerAdvancePersonal();
	}

	@Override
	protected BooleanSetting enableTts() {
		return backend.getEnableTtsPersonal();
	}

	@Override
	protected IntSetting overlayMax() {
		return backend.getOverlayMaxPersonal();
	}

	@Override
	protected Map<Cooldown, BooleanSetting> cds() {
		return backend.getPartyCdSettings();
	}
}
