# Fully-Event-Driver XIV Trigger Prototype

### Why?

The current solutions each have advantages and disadvantages.

Triggernometry makes it very easy to whip up quick triggers. However, where it falls flat is making triggers that are
more complicated. You *can* bodge things like loops into it, but it's overall not going to be the best tool for the job
as soon as you hit the "I wish I was just writing code rather that trying to do this in a GUI" point. There's also the
fact that it is more painful to debug than code with a proper debugger+IDE, triggers lack automated testing, and while
you get expression-level validation, you don't get validation for things like variable names.

It is also poor at code re-use. Making a "function" is doable, but more complicated and hacky than it should be.

Cactbot, with its triggers being written in JS, makes it easier to write complex triggers, and even offers some degree
of state management (most things should not carry over from pull to pull). However, it still doesn't take the next
logical step of abstracting away the actual log lines into objects where everything is already parsed.

Both suffer from the issue that the idea of running log lines through regices should be *every* trigger's job, when in
reality, it should just be done once and parsed into a convenient object.

Then, there's the bespoke ACT plugins, like the Jail plugin. These are, in my opinion, severely lacking in
functionality, and suffer from re-use issues as well. For example, what if I want automarks, *and* a personal callout?
Setting up two separate triggers for that would make it prone to getting the logic or priority inconsistent between the
two, leading to wrong callouts.

### So How Does This Work?

The core idea here is to take events, starting with events at as low a level as possible, and emit new events based off
those.

For example, in everyone's favorite mechanic, Titan Jails, here is how it might look:

1. ACT log reader sees a log line
   of `21|2021-09-30T19:43:43.1650000-07:00|40016AA2|Titan|2B6C|Rock Throw|10669D22|Some Dude|...`
2. It emits
   a `ACTLogLineEvent("21|2021-09-30T19:43:43.1650000-07:00|40016AA2|Titan|2B6C|Rock Throw|10669D22|Some Dude|...")`
3. Another event handler will read the ACTLogLineEvent and parse it into a rich object, like:

```
AbilityUsedEvent(
    time = 2021-09-30T19:43:43.1650000-07:00,
    caster = Entity(name=Titan, id=40016AA2),
    ability = Ability(name=Rock Throw, id=2B6C),
    target = Entity(name=Some Dude, id=10669D22)
)
```

4. Then, another event handler, subscribed to `AbilityUsedEvent`, would turn it into a more specific event:

```
TitanJailEvent(player = Entity(name=Some Dude, id=10669D22))
```

5. Finally, yet another event handler would be subscribed to `TitanJailEvent`, and would put this player into a list.
   Nothing would happen yet.
6. Upon receiving two more `TitanJailEvent`s, this handler would then emit one final event:

```
UnsortedTitanJailsSolvedEvent(players = [Entity(name=some dude, ...), Entity(...), Entity(...)])
```
7. However, we need to sort the list by whatever priority system we want. We would make something to do that, and then
it would emit another event:
```
FInalTitanJailsSolvedEvent(players = [Entity(name=some dude, ...), Entity(...), Entity(...)])
```

9. Both an automarker plugin, and a personal callout plugin could subscribe to the `FinalTitanJailsSolvedEvent`. Perhaps
   others too, such as visual auras.

### Why?

Unlike other solutions, every small piece of this could be unit tested, and the whole solution could be end-to-end
tested. In addition, once you have a working `TitanJailsSolvedEvent`, any additional triggers need not concern
themselves with any of that logic - they merely subscribe to that event and can do whatever they wish with it. In
addition, even the thing that collects the three players who have been jailed need not concern itself with regices at
all - it just needs to do something like this:

```java

@Scope(Scopes.PULL) // One instance of this class per pull
public final class JailStuff {
	// Collect jailed players
	private final List<Entity> jailedPlayers = new ArrayList<>();

	// Handle event - 'acceptor' is where we send back new synthetic events.
	public void handleEvent(EventAcceptor acceptor, AbilityUsedEvent event) {
		// This is our filter - these are the only ability IDs we care about.
		if (event.getAbility().getId() != 0x2B6C && event.getAbility().getId() != 0x2B6B) {
			return;
		}
		jailedPlayers.add(event.getTarget());
		if (jailedPlayers.size() == 3) {
			acceptor.accept(new UnsortedTitanJailsSolvedEvent(new ArrayList<>(jailedPlayers)));
		}
	}
}
```

That's it. The code is very readable and understandable. No regex parsing - that's already handled by the time we get
here. We just use `event.getAbility().getId()` to check if it's one of the ability IDs we care about, and then we
extract the player out of it. The sorting/prioritization, as well as the actual callout/marking, are completely
de-coupled from the collection logic.

You also avoid a lot of nonsense - hex vs decimal conversion, some hex IDs being in lowercase while most are upper,
as well as the ability to abstract away certain details that are useless 99% of the time (e.g. 21 NetworkAbility 
vs 22 NetworkAOEAbility).

Another advantage of abstracting away the log lines is that if log line format or fields change in the future, only a
single update is needed, rather than potentially every trigger needing an update.