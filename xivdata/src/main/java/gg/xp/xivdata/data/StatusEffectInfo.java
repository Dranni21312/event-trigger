package gg.xp.xivdata.data;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public record StatusEffectInfo(
		long statusEffectId,
		long baseIconId,
		long maxStacks,
		String name,
		String description,
		boolean canDispel,
		boolean isPermanent,
		int partyListPriority,
		boolean isFcBuff
) {

	// TODO: column 19 == 1 means it's FC buff, food, etc and so should be hidden
	// Column 20 == HoT/DoT (as well as psuedos like Horoscope)

	public List<Long> getAllIconIds() {
		if (maxStacks == 0 || maxStacks == 1) {
			return Collections.singletonList(baseIconId);
		}
		else {
			return LongStream.range(baseIconId, baseIconId + maxStacks).boxed().collect(Collectors.toList());
		}
	}

	public long iconIdForStackCount(long stacks) {
		if (stacks == 0) {
			return baseIconId;
		}
		else {
			return baseIconId + stacks - 1;
		}
	}

	public @Nullable StatusEffectIcon getIcon(long stacks) {
		return StatusEffectLibrary.iconId(iconIdForStackCount(stacks));
	}

	public List<StatusEffectIcon> getAllIcons() {
		if (maxStacks == 0) {
			StatusEffectIcon icon = getIcon(0);
			if (icon == null) {
				return Collections.emptyList();
			}
			else {
				return Collections.singletonList(icon);
			}
		}
		else {
			return LongStream.range(1, maxStacks + 1).mapToObj(this::getIcon).filter(Objects::nonNull).toList();
		}
	}
}
