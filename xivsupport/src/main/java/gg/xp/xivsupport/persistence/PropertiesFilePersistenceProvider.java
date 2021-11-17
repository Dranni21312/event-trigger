package gg.xp.xivsupport.persistence;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PropertiesFilePersistenceProvider extends BaseStringPersistenceProvider {

	private static final Logger log = LoggerFactory.getLogger(PropertiesFilePersistenceProvider.class);
	private static final ExecutorService exs = Executors.newSingleThreadExecutor();
	private final Properties properties;
	private final File file;

	public static PropertiesFilePersistenceProvider inUserDataFolder(String baseName) {
		String userDataDir = System.getenv("APPDATA");
		log.info("Appdata: {}", userDataDir);
		File file = Paths.get(userDataDir, "triggevent", baseName + ".properties").toFile();
		log.info("Using file: {}", file);
		return new PropertiesFilePersistenceProvider(file);
	}

	public PropertiesFilePersistenceProvider(String filePath) {
		this(new File(filePath));
	}

	public PropertiesFilePersistenceProvider(File file) {
		this.file = file;
		Properties properties = new Properties();
		try {
			try (FileInputStream stream = new FileInputStream(file)) {
				properties.load(stream);
			}
		}
		catch (FileNotFoundException e) {
			log.info("Properties file does not yet exist");
		}
		catch (IOException e) {
			throw new RuntimeException("Could not load properties", e);
		}
		this.properties = properties;
		writeChangesToDisk();
	}

	private void writeChangesToDisk() {
		exs.submit(() -> {
			try {
				File parentFile = file.getParentFile();
				if (parentFile != null) {
					parentFile.mkdirs();
				}
				try (FileOutputStream stream = new FileOutputStream(file)) {
					properties.store(stream, "Saved programmatically");
				}
			}
			catch (IOException e) {
				log.error("Error saving properties! Changes may not be saved!", e);
			}
		});
	}

	@Override
	protected String rewriteKey(String originalKey) {
		return super.rewriteKey(originalKey);
	}

	@Override
	protected void setValue(@NotNull String key, @Nullable String value) {
		properties.setProperty(key, value);
		writeChangesToDisk();
	}

	@Override
	protected void deleteValue(@NotNull String key) {
		properties.remove(key);
		writeChangesToDisk();
	}

	@Override
	protected @Nullable String getValue(@NotNull String key) {
		return properties.getProperty(key);
	}

	@Override
	protected void clearAllValues() {
		properties.clear();
		writeChangesToDisk();
	}
}
