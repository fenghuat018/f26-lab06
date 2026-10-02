# Contract Worksheet

One section per milestone. Fill each one in as you go, in order. Write each
prediction before you run anything. That is the part a TA asks about.

Keep it short and specific. Point at methods, call sites, and error text.

---

## Milestone 1: The notes overload

### Prediction (write this before you run the build, and you can deliberate with your agent)

**Will the consumer, untouched, still compile and pass?** Yes.

**Why.** 

The consumer’s existing call sites still use the same arguments as before, so the compiler should continue resolving those calls to the original method signature. Adding an overload is additive as long as the old method remains and the new overload does not make the existing calls ambiguous. The new overload only affects callers that use the new parameter list or argument types.

### What happened

**The result.** 

The build passed. Maven built all three reactor modules successfully: `lab06-booking-parent`, `lab06-api`, and `lab06-consumer`.

For `lab06-api`, Maven ran `InMemoryBookingServiceTest`: 6 tests ran, 0 failures, 0 errors, 0 skipped.

For `lab06-consumer`, Maven ran `FrontDeskTest`: 7 tests ran, 0 failures, 0 errors, 0 skipped.

The reactor summary printed `SUCCESS` for all modules and ended with `BUILD SUCCESS`.

**If your prediction was wrong,** 

It was not wrong. The untouched consumer still compiled and passed.

**Is an additive change always safe in Java?** 

No. For example, in this project the original call in `consumer` is:

`api.createBooking(roomId, startMinute, endMinute, null)`

Right now that clearly matches:

`createBooking(String roomId, long startMinute, long endMinute, String waitlistKey)`

But imagine the API added another four-argument overload like:

`createBooking(String roomId, long startMinute, long endMinute, Integer priority)`

Then the existing call with `null` as the fourth argument could become ambiguous, 
because `null` can match both `String` and `Integer`. The consumer did not change, 
but it could fail at compile time.

---

## Milestone 2: The request object

### Prediction (write this before you run the build)

**Will the untouched consumer still compile and pass?** Yes or no, and if no,
which module goes red and whether at compile time or test time.

**Where.** Name the call sites you expect to be affected, if any.

**What about the tests in `api/`, after you update them?** And whether their
result is evidence about the consumer.

### Step 1: after the fold

**What the build printed.** Paste it for each module, including file and
line for anything that failed.

**Which module's tests ran, and which did not.** And what that tells you about
who can detect a contract break.

### Step 2: the deprecation path

**What you added.** The signatures that came back, and what they delegate to.

**The warnings.** Paste one deprecation warning line from the build log (from
a `mvn -B clean test` run, since a rerun with nothing to compile prints none).

**What the deprecation path resolves.** Who can now build that could not build
during step 1, and who is on which schedule.

**What the warnings accomplish that a README note would not.** Be concrete
about where the warning shows up and who sees it without looking for it.

---

## Milestone 3: The misuse critique

Not coded. One misuse, one redesign, one cost. Discuss it with your TA.

### The misuse

**What is easy to get wrong.** One specific thing about the API surface.

**The call site.** File and line in `consumer/`, with the call. Show the
code that a reader cannot understand without opening the javadoc, or that a
caller could get wrong with the compiler still happy.

**What goes wrong when it happens.** Silent bad behavior, wrong data, a crash
somewhere far away?

### The redesign

**The proposal.** Types, enums, factories, or whatever you are proposing. Show
the new signature and the new call site.

**Why the mistake is now hard or impossible to make.** Point at the mechanism,
such as the compiler, a validating constructor, or an exhaustive switch.

### One tradeoff

**What it costs.** Something real, such as caller ceremony, migration burden
against the deprecation path you just built, or more types for a newcomer to
learn. "No real downside" does not count.

**When the price is worth paying.** A condition under which it is.
