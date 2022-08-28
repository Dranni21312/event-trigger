package gg.xp.xivsupport.events.triggers.easytriggers.conditions;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import gg.xp.reevent.events.Event;
import gg.xp.xivsupport.events.triggers.easytriggers.model.Condition;
import gg.xp.xivsupport.gui.groovy.GroovyManager;
import groovy.lang.GroovyShell;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

public class GroovyEventFilter implements Condition<Event> {
	private static final Logger log = LoggerFactory.getLogger(GroovyEventFilter.class);

	private final GroovyManager mgr;
	private @Nullable GroovyShell shell;


	public Class<? extends Event> eventType = Event.class;
	private String groovyScript = "event != null";
	private boolean strict = true;
	@JsonIgnore
	private Predicate<? extends Event> groovyCompiledScript;

	public GroovyEventFilter(@JacksonInject GroovyManager mgr) {
		this.mgr = mgr;
	}

	@JsonCreator
	public GroovyEventFilter(@JsonProperty("groovyScript") String groovyScript,
	                         @JsonProperty("strict") boolean strict,
	                         @JsonProperty("eventType") Class<? extends Event> eventType,
	                         @JacksonInject GroovyManager mgr
	) {
		this(mgr);
		this.strict = strict;
		this.eventType = eventType;
		this.groovyScript = groovyScript;
	}

	public boolean isStrict() {
		return strict;
	}

	public void setStrict(boolean strict) {
		this.strict = strict;
		// TODO: does this need to recompile?
	}

	public String getGroovyScript() {
		return groovyScript;
	}

	public void setGroovyScript(String groovyScript) {
		try {
			this.groovyCompiledScript = compile(groovyScript);
		}
		catch (Throwable t) {
			// Special handling for deserialization
			if (groovyCompiledScript == null) {
				groovyCompiledScript = (o) -> false;
				// TODO: expose pre-existing errors on the UI
				log.error("Error compiling groovy script", t);
			}
			else {
				throw new IllegalArgumentException(t);
			}
		}
		this.groovyScript = groovyScript;
	}

	private Predicate<? extends Event> compile(String script) {
		if (shell == null) {
			shell = mgr.makeShell();
		}
		String longClassName = eventType.getCanonicalName();
		String shortClassName = eventType.getSimpleName();
		String varName = "event";
		String checkType = strict ? "@CompileStatic" : "";
		String inJavaForm = """
				import %s;
				new Predicate<%s>() {
					%s
					@Override
					public boolean test(%s %s) {
						%s
					}
				};
				""".formatted(longClassName, shortClassName, checkType, shortClassName, varName, script);
		return (Predicate<? extends Event>) shell.evaluate(inJavaForm);
	}

	@Override
	public String fixedLabel() {
		return "Groovy Filter";
	}

	@Override
	public String dynamicLabel() {
		return "(Groovy Expression)";
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean test(Event event) {
		if (groovyCompiledScript == null) {
			try {
				groovyCompiledScript = compile(groovyScript);
			}
			catch (Throwable t) {
				log.error("Error compiling script, disabling", t);
				groovyCompiledScript = (e) -> false;
			}
		}
		if (eventType.isInstance(event)) {
			return ((Predicate<Event>) groovyCompiledScript).test(event);
		}
		else {
			return false;
		}
	}

}
