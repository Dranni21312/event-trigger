package gg.xp.xivsupport.events.triggers.marks;

import gg.xp.reevent.events.BaseEvent;
import gg.xp.reevent.events.EventContext;
import gg.xp.reevent.scan.HandleEvents;
import gg.xp.reevent.scan.LiveOnly;
import gg.xp.xivsupport.events.debug.DebugCommand;
import gg.xp.xivsupport.events.state.XivState;
import gg.xp.xivsupport.events.state.XivStateImpl;
import gg.xp.xivsupport.models.XivPlayerCharacter;
import gg.xp.xivsupport.persistence.PersistenceProvider;
import gg.xp.xivsupport.persistence.settings.BooleanSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.Serial;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AutoMarkHandler {

	private static final Logger log = LoggerFactory.getLogger(AutoMarkHandler.class);
	private static final ExecutorService exs = Executors.newSingleThreadExecutor();
	private final BooleanSetting useFkeys;
	private final BooleanSetting useTelesto;
	private final XivState state;

	public AutoMarkHandler(PersistenceProvider persistence, XivState state) {
		useFkeys = new BooleanSetting(persistence, "auto-marks.use-fkeys", false);
		// TODO: make this automatic
		useTelesto = new BooleanSetting(persistence, "auto-marks.use-telesto", false);
		this.state = state;
	}

	public BooleanSetting getUseFkeys() {
		return useFkeys;
	}

	public BooleanSetting getUseTelesto() {
		return useTelesto;
	}

	@HandleEvents
	public void amTest(EventContext context, DebugCommand event) {
		if (event.getCommand().equals("amtest")) {
			List<String> args = event.getArgs();
			args.subList(1, args.size())
					.stream()
					.mapToInt(Integer::parseInt)
					.forEach(i -> context.accept(new AutoMarkSlotRequest(i)));
		}
	}

	@HandleEvents
//	@LiveOnly
	public void clearMarks(EventContext context, ClearAutoMarkRequest event) {
		if (!useTelesto.get()) {
			log.info("Clearing marks");
			clearAutoMark(context);
		}
	}

	@HandleEvents
	public void findPartySlot(EventContext context, AutoMarkRequest event) {
		XivPlayerCharacter player = event.getPlayerToMark();
		int index = state.getPartySlotOf(player);
		if (index >= 0) {
			int partySlot = index + 1;
			log.info("Resolved player {} to party slot {}", player.getName(), partySlot);
			context.accept(new AutoMarkSlotRequest(partySlot));
		}
		else {
			log.error("Couldn't resolve player '{}' to party slot", player.getName());
		}
	}

	@HandleEvents
	public void doMark(EventContext context, AutoMarkSlotRequest event) {
		if (!useTelesto.get()) {
			doAutoMarkForSlot(context, event.getSlotToMark());
		}
	}

	public static final class KeyPressRequest extends BaseEvent {
		@Serial
		private static final long serialVersionUID = -3520916842042620376L;
		private final int keyCode;

		// Leaving this private for now - need a way to prevent abuse
		private KeyPressRequest(int keyCode) {
			this.keyCode = keyCode;
		}

		public int getKeyCode() {
			return keyCode;
		}
	}

	// i = 1-8
	private int keycodeForSlot(int i) {
		if (useFkeys.get()) {
			return KeyEvent.VK_F1 - 1 + i;
		}
		else {
			return KeyEvent.VK_NUMPAD1 - 1 + i;
		}
	}

	private int keycodeForClear() {
		if (useFkeys.get()) {
			return KeyEvent.VK_F9;
		}
		else {
			return KeyEvent.VK_NUMPAD9;
		}
	}

	private void clearAutoMark(EventContext context) {
		int keyCode = keycodeForClear();
		context.accept(new KeyPressRequest(keyCode));
	}

	private void doAutoMarkForSlot(EventContext context, int i) {
		int keyCode = keycodeForSlot(i);
		context.accept(new KeyPressRequest(keyCode));
	}

	@LiveOnly
	@HandleEvents
	public static void doKeyPress(EventContext context, KeyPressRequest event) {
		pressAndReleaseKey(event.getKeyCode());
	}

	private static void pressAndReleaseKey(int keyCode) {
		exs.submit(() -> {
			try {
				log.info("Pressing cdKey {} ({})", keyCode, KeyEvent.getKeyText(keyCode));
				new Robot().keyPress(keyCode);
				Thread.sleep(50);
				new Robot().keyRelease(keyCode);
				Thread.sleep(50);
			}
			catch (AWTException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
	}


}
