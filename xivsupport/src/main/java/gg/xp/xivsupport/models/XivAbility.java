package gg.xp.xivsupport.models;

import gg.xp.xivsupport.events.actlines.events.NameIdPair;

import java.io.Serializable;
import java.util.Objects;

public class XivAbility implements Serializable, NameIdPair {
	private static final long serialVersionUID = -6170494857373031360L;
	private final long id;
	private final String name;

	public XivAbility(long id, String name) {
		this.id = id;
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return String.format("XivAbility(0x%X:%s)", id, name);
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		XivAbility ability = (XivAbility) o;
		return id == ability.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
