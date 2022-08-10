package gg.xp.xivsupport.custompartyoverlay;

import gg.xp.xivsupport.gui.tables.renderers.MpBar;
import gg.xp.xivsupport.models.XivPlayerCharacter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class MpBarComponent extends BasePartyListComponent {
	private final MpBar bar;

	public MpBarComponent() {
		bar = new MpBar();
		bar.setFgTransparency(255);
		bar.setBgTransparency(72);
	}

	@Override
	protected Component makeComponent() {
		return bar;
	}

	@Override
	protected void reformatComponent(@NotNull XivPlayerCharacter xpc) {
		bar.setData(xpc);
		bar.revalidate();
	}
}
