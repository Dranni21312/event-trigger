package gg.xp.xivsupport.timelines;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class MakeTimelines {

	private static final Logger log = LoggerFactory.getLogger(MakeTimelines.class);

	private MakeTimelines() {
	}

	private static String stripFileName(String input) {
		int splitPoint = input.lastIndexOf('/');
		return input.substring(0, splitPoint + 1);
	}

	public static void main(String[] args) {
		WebDriverManager.chromedriver().setup();
		ChromeOptions opts = new ChromeOptions();
		opts.setHeadless(true);
		ChromeDriver driver = new ChromeDriver(opts);
		Map<Long, String> zoneToFile = new HashMap<>();
		try {
			driver.get("https://quisquous.github.io/cactbot/ui/raidboss/raidboss.html?OVERLAY_WS=wss://127.0.0.1:10501");

			Object out = driver.executeScript("""
					return await new Promise((resolve) => {
					    const id = 'fakeId' + Math.random();
					    window['webpackChunkcactbot'].push([[id], null, (req) => resolve(req)]);
					}).then((req) => {
					    return req(parseInt(Object.keys(webpackChunkcactbot[0].find(a=>!Array.isArray(a)))[0])).Z;
					});
					""");

			((Map<?, ?>) out).forEach((file, content) -> {
				if (content instanceof Map contentMap) {
					Object timelineFileRaw = contentMap.get("timelineFile");
					if (timelineFileRaw == null) {
						return;
					}
					String timelineFileName = timelineFileRaw.toString();
					Object zoneIdRaw = contentMap.get("zoneId");
					Long zoneId = (Long) zoneIdRaw;
					String fullTimelineFilePath = stripFileName(file.toString()) + timelineFileName;
					zoneToFile.put(zoneId, timelineFileName);
					String fileContents = ((Map<?, ?>) out).get(fullTimelineFilePath).toString().replaceAll("\r\n", "\n");
					try {
						Files.writeString(Path.of("timelines", "src", "main", "resources", "timeline", timelineFileName), fileContents);
					}
					catch (IOException e) {
						throw new RuntimeException("Error processing timeline for '" + file + '\'', e);
					}
				}
			});
			String inCsvFormat = zoneToFile.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e -> e.getKey() + ",\"" + e.getValue() + '"').collect(Collectors.joining("\n"));
			Files.writeString(Path.of("timelines", "src", "main", "resources", "timelines.csv"), inCsvFormat, StandardCharsets.UTF_8);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			driver.quit();
		}
	}


}
