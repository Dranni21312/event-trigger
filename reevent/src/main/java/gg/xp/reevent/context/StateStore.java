package gg.xp.reevent.context;

public interface StateStore {
	<X> X get(Class<X> clazz);

	<X> void putCustom(Class<X> clazz, X instance);
}
