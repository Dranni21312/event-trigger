package gg.xp.xivsupport.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO: have some kind of annotation to allow an already-scoped persistence provider to be injected
public interface PersistenceProvider {

	void save(@NotNull String key, @NotNull Object value);

	<X> X get(@NotNull String key, @NotNull Class<X> type, @Nullable X dflt);

	<X> X get(@NotNull String key, @NotNull TypeReference<X> type, @Nullable X dflt);

	void delete(@NotNull String key);

	void clearAll();

}
