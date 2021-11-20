package gg.xp.xivsupport.models;

import gg.xp.xivdata.jobs.Job;

public class XivPlayerCharacter extends XivCombatant {
	private static final long serialVersionUID = 8719229961190925919L;
	private final Job job;
	private final XivWorld world;

	public XivPlayerCharacter(long id,
	                          String name,
	                          Job job,
	                          XivWorld world,
	                          boolean isLocalPlayerCharacter,
	                          long typeRaw,
	                          HitPoints hp,
	                          ManaPoints mp,
	                          Position pos,
	                          long bNpcId,
	                          long bNpcNameId,
	                          long partyType,
	                          long level,
	                          long ownerId
	) {
		super(id, name, true, isLocalPlayerCharacter, typeRaw, hp, mp, pos, bNpcId, bNpcNameId, partyType, level, ownerId);
		this.job = job;
		this.world = world;
	}

	public Job getJob() {
		return job;
	}

	public XivWorld getWorld() {
		return world;
	}

	@Override
	public String toString() {
		return String.format("XivPlayerCharacter(0x%X:%s, %s, %s, %s, %s)", getId(), getName(), getJob(), getWorld(), getLevel(), isThePlayer());
	}

}
