package gg.xp.events.ws;

import com.fasterxml.jackson.databind.JsonNode;
import gg.xp.events.BaseEvent;
import gg.xp.events.actlines.events.SystemEvent;
import org.jetbrains.annotations.Nullable;

@SystemEvent
public class ActWsJsonMsg extends BaseEvent {

	private static final long serialVersionUID = -5830123394422861873L;
	private final @Nullable String type;
	private final JsonNode json;

	public ActWsJsonMsg(@Nullable String type, JsonNode json) {
		this.type = type;
		this.json = json;
	}

	public @Nullable String getType() {
		return type;
	}

	public JsonNode getJson() {
		return json;
	}
}
