package com.calendar.calendar5010;

import org.junit.jupiter.api.Test;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class RecurringEventTest {

  private final LocalDate START_DATE = LocalDate.of(2025, 11, 3); // A Monday
  private final LocalTime START_TIME = LocalTime.of(10, 0);
  private final LocalTime END_TIME = LocalTime.of(11, 0);
  private final Set<DayOfWeek> MONDAY_WEDNESDAY = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);

  // --- Constraints Validation Tests ---

  @Test
  void shouldBeValidWithRepeatCountOnly() {
    // Constraint: One of repeatCount or recurrenceEndDate must be non-null.
    assertDoesNotThrow(() -> RecurringEvent.builder()
      .subject("Valid Count").startDate(START_DATE).endDate(START_DATE).startTime(START_TIME).endTime(END_TIME)
      .recurrenceDays(MONDAY_WEDNESDAY)
      .repeatCount(5)
      .recurrenceEndDate(null)
      .build());
  }

  @Test
  void shouldBeValidWithRecurrenceEndDateOnly() {
    // Constraint: One of repeatCount or recurrenceEndDate must be non-null.
    assertDoesNotThrow(() -> RecurringEvent.builder()
      .subject("Valid EndDate").startDate(START_DATE).endDate(START_DATE).startTime(START_TIME).endTime(END_TIME)
      .recurrenceDays(MONDAY_WEDNESDAY)
      .repeatCount(null)
      .recurrenceEndDate(LocalDate.of(2025, 12, 3))
      .build());
  }

  @Test
  void shouldRejectIfBothRepeatCountAndEndDateAreNull() {
    // Constraint: Must have one defined
    assertThrows(IllegalArgumentException.class, () -> RecurringEvent.builder()
      .subject("Invalid Null").startDate(START_DATE).endDate(START_DATE).startTime(START_TIME).endTime(END_TIME)
      .recurrenceDays(MONDAY_WEDNESDAY)
      .repeatCount(null)
      .recurrenceEndDate(null)
      .build(), "Should reject because both repeatCount and recurrenceEndDate are null.");
  }

  // --- Core Functionality Tests ---

  @Test
  void shouldGenerateEventsByRepeatCount() {
    // Repeats 3 times, Mon and Wed. Expected: 11/3(M), 11/5(W), 11/10(M)
    RecurringEvent recurringEvent = RecurringEvent.builder()
      .subject("Fitness").startDate(START_DATE).endDate(START_DATE).startTime(START_TIME).endTime(END_TIME)
      .recurrenceDays(MONDAY_WEDNESDAY)
      .repeatCount(3)
      .build();

    assertEquals(3, recurringEvent.getListEvents().size());
    assertTrue(recurringEvent.getListEvents().stream().anyMatch(e -> e.getStartDate().isEqual(LocalDate.of(2025, 11, 10))));
  }

  @Test
  void shouldRejectRecurringEventIfItCrossesDay() {
    // Rule: A single event in the recurring series can span only one day.
    assertThrows(IllegalArgumentException.class, () -> RecurringEvent.builder()
      .subject("Bad Recurrence")
      .startDate(START_DATE)
      .endDate(START_DATE.plusDays(1)) // Violates rule
      .startTime(START_TIME).endTime(END_TIME)
      .recurrenceDays(Set.of(DayOfWeek.MONDAY))
      .repeatCount(2)
      .build(), "Recurring event cannot cross day");
  }

  @Test
  void shouldRejectRecurringEventWithConflictingInstance() {
    Calendar calendar = new Calendar("Recurring Conflict Calendar");
    LocalDate conflictDate = LocalDate.of(2025, 11, 5); // Conflict date: Wednesday

    // Existing event: 11/5 10:30-11:30
    Event existingEvent = SingleEvent.builder().subject("Conflicting Meeting").startDate(conflictDate).endDate(conflictDate).startTime(LocalTime.of(10, 30)).endTime(LocalTime.of(11, 30)).build();
    calendar.createEvent(existingEvent);

    // New recurring event: Mon and Wed 10:00-11:00 (11/5 instance conflicts)
    RecurringEvent newRecurringEvent = RecurringEvent.builder()
      .subject("Daily Standup").startDate(START_DATE).endDate(START_DATE).startTime(START_TIME).endTime(END_TIME)
      .recurrenceDays(MONDAY_WEDNESDAY)
      .allowConflict(false)
      .repeatCount(5)
      .build();

    // Rule: Application should refuse to create a recurring event if any of the individual events would conflict.
    assertThrows(IllegalArgumentException.class, () -> calendar.createEvent(newRecurringEvent),
      "Should reject recurring event due to conflict on 11/5");
  }

  @Test
  void shouldModifyASingleInstanceOfARecurringEvent() {
    Calendar calendar = new Calendar("Recurrence Edit Calendar");
    LocalDate dateToModify = LocalDate.of(2025, 11, 5); // Wednesday instance

    RecurringEvent recurringEvent = RecurringEvent.builder().subject("Weekly Sync").startDate(START_DATE).endDate(START_DATE).startTime(START_TIME).endTime(END_TIME)
      .recurrenceDays(MONDAY_WEDNESDAY).repeatCount(5).build();
    calendar.createEvent(recurringEvent);

    // Update for a single instance on 11/5
    EventUpdate update = EventUpdate.builder().location("Online Meeting").build();

    // Modify a single instance by passing the date
    calendar.updateEvent(recurringEvent, update, dateToModify);

    // Verify the modified instance
    Event modifiedEvent = calendar.getEvent("Weekly Sync", dateToModify, START_TIME);
    assertEquals("Online Meeting", modifiedEvent.getLocation());

    // Verify another instance (11/3) was not modified
    Event unmodifiedEvent = calendar.getEvent("Weekly Sync", START_DATE, START_TIME);
    assertNull(unmodifiedEvent.getLocation());
  }

  @Test
  void shouldModifyDetachedSingleEventFromRecurringSeries() {
    Calendar calendar = new Calendar("Detached Update Calendar");

    // 1. Create and add Recurring Event (Generates single events internally)
    RecurringEvent recurringEvent = RecurringEvent.builder()
      .subject("Team Huddle").startDate(START_DATE).endDate(START_DATE).startTime(START_TIME).endTime(END_TIME)
      .recurrenceDays(Set.of(DayOfWeek.MONDAY))
      .repeatCount(2)
      .allowConflict(false)
      .build();
    calendar.createEvent(recurringEvent);

    // 2. Get the specific SingleEvent instance from the calendar
    LocalDate instanceDate = LocalDate.of(2025, 11, 10);
    Event singleEventInstance = calendar.getEvent("Team Huddle", instanceDate, START_TIME);

    // The retrieved event should be a SingleEvent
    assertTrue(singleEventInstance instanceof SingleEvent);

    // 3. Prepare the update
    EventUpdate update = EventUpdate.builder().location("Cafeteria").build();

    // 4. Update the SingleEvent directly (using null for date)
    // This simulates passing the list element to the update method
    calendar.updateEvent(singleEventInstance, update, null);

    // 5. Verify the SingleEvent was updated
    Event updatedInstance = calendar.getEvent("Team Huddle", instanceDate, START_TIME);
    assertEquals("Cafeteria", updatedInstance.getLocation());

    // 6. Verify the parent RecurringEvent still exists (if applicable) and its other instance is unchanged
    Event unmodifiedInstance = calendar.getEvent("Team Huddle", START_DATE, START_TIME);
    assertNull(unmodifiedInstance.getLocation());
  }
}