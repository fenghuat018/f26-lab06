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

**Will the untouched consumer still compile and pass?** 

No. The untouched consumer should fail at compile time in the `lab06-consumer` module, because its existing calls still use the old positional `createBooking(...)` signature. If those overloads are removed and replaced by `createBooking(BookingRequest)`, the compiler will not find a matching method.

**Where.** 

The affected call sites are in `consumer/src/main/java/edu/cmu/cs214/frontdesk/FrontDesk.java`:  
`bookWalkIn(...)` calls `api.createBooking(roomId, startMinute, endMinute, null)`, and `joinWaitlist(...)` calls `api.createBooking(roomId, startMinute, endMinute, guestName)`.

**What about the tests in `api/`, after you update them?** 

After updating the `api` tests to call `createBooking(BookingRequest)`, the `api` module should compile and its tests should pass. But that is not evidence that the consumer still works, because the consumer is a separate module with its own unchanged call sites. The real contract break would show up when Maven tries to compile `lab06-consumer`.

### Step 1: after the fold

**What the build printed.** 

`mvn -B clean test` built `lab06-booking-parent` first:

`lab06-booking-parent ............................... SUCCESS`

Then it built `lab06-api`. The API module compiled 5 source files, compiled
its test source, and ran `edu.cmu.cs214.booking.InMemoryBookingServiceTest`:

`Tests run: 6, Failures: 0, Errors: 0, Skipped: 0`

`lab06-api .......................................... SUCCESS`

Then it tried to build `lab06-consumer`, but compilation failed:

`lab06-consumer ..................................... FAILURE`

The compiler errors were:

`consumer/src/main/java/edu/cmu/cs214/frontdesk/FrontDesk.java:[27,19] method createBooking in interface edu.cmu.cs214.booking.BookingApi cannot be applied to given types;`

`required: edu.cmu.cs214.booking.BookingRequest`

`found:    java.lang.String,long,long,<nulltype>`

`reason: actual and formal argument lists differ in length`

`consumer/src/main/java/edu/cmu/cs214/frontdesk/FrontDesk.java:[33,19] method createBooking in interface edu.cmu.cs214.booking.BookingApi cannot be applied to given types;`

`required: edu.cmu.cs214.booking.BookingRequest`

`found:    java.lang.String,long,long,java.lang.String`

`reason: actual and formal argument lists differ in length`

The build ended with `BUILD FAILURE`.

**Which module's tests ran, and which did not.** And what that tells you about
who can detect a contract break.

The `api` module's tests ran and passed. The `consumer` module's tests did not
run, because `consumer` failed at compile time before Surefire could start its
test phase.

This shows that the producer's own tests can pass even when the external
consumer is broken. The contract break is detected by compiling the untouched
consumer against the changed API.

### Step 2: the deprecation path

**What you added.** The signatures that came back, and what they delegate to.

I added back both old positional overloads on `BookingApi` as deprecated
default methods:

`Booking createBooking(String roomId, long startMinute, long endMinute, String waitlistKey)`

This delegates to:

`createBooking(new BookingRequest(roomId, startMinute, endMinute, waitlistKey, null))`

`Booking createBooking(String roomId, long startMinute, long endMinute, String waitlistKey, String notes)`

This delegates to:

`createBooking(new BookingRequest(roomId, startMinute, endMinute, waitlistKey, notes))`

**The warnings.** Paste one deprecation warning line from the build log (from
a `mvn -B clean test` run, since a rerun with nothing to compile prints none).

`[WARNING] /Users/tongfenghua/My mac/CMU-Learning/Agent Eng/f26-lab06/consumer/src/main/java/edu/cmu/cs214/frontdesk/FrontDesk.java:[27,19] createBooking(java.lang.String,long,long,java.lang.String) in edu.cmu.cs214.booking.BookingApi has been deprecated`

**What the deprecation path resolves.** Who can now build that could not build
during step 1, and who is on which schedule.

The untouched `consumer` module can now build again. In step 1, it failed at
compile time because its old positional calls no longer matched the API. With
the deprecated overloads restored, old callers can keep building for now, while
new callers can move to `createBooking(BookingRequest)`.

**What the warnings accomplish that a README note would not.** Be concrete
about where the warning shows up and who sees it without looking for it.

The warnings show up directly in the compiler output at the old call sites in
`FrontDesk.java`. A caller sees the warning during their normal build, including
the file and line number to update. A README note only helps someone who knows
to go read it.

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
