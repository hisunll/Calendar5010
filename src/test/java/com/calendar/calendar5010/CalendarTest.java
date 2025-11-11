package com.calendar.calendar5010;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CalendarTest {

  private Calendar calendar;
  private final LocalDate DATE_1 = LocalDate.of(2025, 11, 10);
  private final LocalTime TIME_9AM = LocalTime.of(9, 0);
  private final LocalTime TIME_10AM = LocalTime.of(10, 0);

  @BeforeEach
  void setUp() {
    calendar = new Calendar("Work Schedule");
  }

  // --- Create Single Event Tests ---
  @Test
  void shouldSuccessfullyAddValidSingleEventWithoutConflictStatus() {
    Event event = SingleEvent.builder()
      .subject("Meeting")
      .startDate(DATE_1).endDate(DATE_1).startTime(TIME_9AM).endTime(LocalTime.of(10, 0))
      .build();

    Calendar calendar2 = new Calendar("Work Schedule", true);
    calendar2.createEvent(event);
    assertNotNull(calendar2.getEvent("Meeting", DATE_1, TIME_9AM));
  }

  @Test
  void shouldSuccessfullyAddValidSingleEvent() {
    Event event = SingleEvent.builder()
      .subject("Meeting")
      .startDate(DATE_1).endDate(DATE_1).startTime(TIME_9AM).endTime(LocalTime.of(10, 0))
      .build();
    calendar.createEvent(event);
    assertNotNull(calendar.getEvent("Meeting", DATE_1, TIME_9AM));
  }

  @Test
  void shouldRejectEventWithSameSubjectDateAndTime() {
    Event event1 = SingleEvent.builder().subject("Lunch").startDate(DATE_1).endDate(DATE_1).startTime(LocalTime.of(12, 0)).endTime(LocalTime.of(13, 0)).build();
    calendar.createEvent(event1);

    Event event2 = SingleEvent.builder()
      .subject("Lunch")
      .startDate(DATE_1)
      .endDate(DATE_1)
      .startTime(LocalTime.of(12, 0)) // 相同 subject, date, time
      .endTime(LocalTime.of(14, 0))
      .build();

    assertThrows(IllegalArgumentException.class, () -> calendar.createEvent(event2));
  }

  @Test
  void shouldRejectConflictingEventWhenConflictsDisallowed() {
    Event event1 = SingleEvent.builder().subject("Existing").startDate(DATE_1).endDate(DATE_1).startTime(TIME_9AM).endTime(LocalTime.of(10, 0)).build();
    calendar.createEvent(event1);

    Event event2 = SingleEvent.builder().subject("Conflict").startDate(DATE_1).endDate(DATE_1).startTime(LocalTime.of(9, 30)).endTime(LocalTime.of(10, 30)).build();

    assertThrows(IllegalArgumentException.class, () -> calendar.createEvent(event2), "Should be rejected due to conflict.");
  }

  @Test
  void shouldAllowConflictingEventWhenConflictAllowedOnEvent() {
    Event event1 = SingleEvent.builder().subject("Meeting").startDate(DATE_1).endDate(DATE_1).startTime(TIME_9AM).endTime(LocalTime.of(10, 0)).build();
    calendar.createEvent(event1);

    Event event2 = SingleEvent.builder()
      .subject("Walk")
      .startDate(DATE_1).endDate(DATE_1).startTime(LocalTime.of(9, 30)).endTime(LocalTime.of(10, 30))
      .allowConflict(true)
      .build();

    assertDoesNotThrow(() -> calendar.createEvent(event2));
    assertNotNull(calendar.getEvent("Walk", DATE_1, LocalTime.of(9, 30)));
  }

  // --- Query Calendar Tests ---

  @Test
  void shouldRetrieveEventBySubjectDateAndTime() {
    Event event = SingleEvent.builder().subject("Presentation").startDate(DATE_1).endDate(DATE_1).startTime(TIME_9AM).endTime(LocalTime.of(10, 0)).build();
    calendar.createEvent(event);

    Event retrieved = calendar.getEvent("Presentation", DATE_1, TIME_9AM);
    assertNotNull(retrieved);
  }

  @Test
  void shouldRetrieveAllEventsOnSpecificDate() {
    // Event 1: All day on DATE_1
    Event event1 = SingleEvent.builder().subject("AllDay").startDate(DATE_1).endDate(DATE_1).allowConflict(true).build();
    // Event 2: 10:00-11:00 on DATE_1
    Event event2 = SingleEvent.builder().subject("Timed").startDate(DATE_1).endDate(DATE_1)
      .startTime(TIME_9AM).endTime(LocalTime.of(10, 0)).allowConflict(true).build();

    calendar.createEvent(event1);
    calendar.createEvent(event2);

    // Query: Only DATE_1
    List<Event> eventsOnDate = calendar.getEventByDate(List.of(DATE_1));
    assertEquals(2, eventsOnDate.size());
  }

  // --- Edit Single Event Tests ---

  @Test
  void shouldSuccessfullyUpdateEventLocation() {
    Event original = SingleEvent.builder().subject("EditTest").startDate(DATE_1).endDate(DATE_1).startTime(TIME_9AM).endTime(LocalTime.of(10, 0)).location("Room A").build();
    calendar.createEvent(original);

    EventUpdate update = EventUpdate.builder().location("Room B").build();

    calendar.updateEvent(original, update, null);

    Event updatedEvent = calendar.getEvent("EditTest", DATE_1, TIME_9AM);
    assertEquals("Room B", updatedEvent.getLocation());
  }

  @Test
  void shouldRejectUpdateThatCreatesConflict() {
    // Event 1 (Existing): 10:00 - 11:00
    Event event1 = SingleEvent.builder().subject("Existing").startDate(DATE_1).endDate(DATE_1).startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(11, 0)).build();
    calendar.createEvent(event1);

    // Event 2 (To be moved): 14:00 - 15:00
    Event event2 = SingleEvent.builder().subject("ToMove").startDate(DATE_1).endDate(DATE_1).startTime(LocalTime.of(14, 0)).endTime(LocalTime.of(15, 0)).build();
    calendar.createEvent(event2);

    EventUpdate conflictUpdate = EventUpdate.builder().startTime(LocalTime.of(10, 30)).build();
    assertThrows(IllegalArgumentException.class, () -> calendar.updateEvent(event2, conflictUpdate, null));
    assertEquals(LocalTime.of(14, 0), calendar.getEvent("ToMove", DATE_1, LocalTime.of(14, 0)).getStartTime());
  }

  @Test
  void isBusyReturnsFalseWhenNoEvents() {
    Calendar calendar = new Calendar("BusyTest");
    LocalDate d = LocalDate.of(2025, 11, 3);
    assertFalse(calendar.isBusy(d, LocalTime.of(10, 0)));
  }

  @Test
  void isBusyDetectsOverlapAndBoundary() {
    Calendar calendar = new Calendar("BusyTest");
    LocalDate d = LocalDate.of(2025, 11, 3);

    Event meeting = SingleEvent.builder()
      .subject("Meeting")
      .startDate(d)
      .endDate(d)
      .startTime(LocalTime.of(10, 0))
      .endTime(LocalTime.of(11, 0))
      .allowConflict(true)
      .build();
    calendar.createEvent(meeting);

    assertTrue(calendar.isBusy(d, LocalTime.of(10, 1)), "Start time should be busy");
    assertTrue(calendar.isBusy(d, LocalTime.of(10, 30)), "Inside interval should be busy");
    assertTrue(calendar.isBusy(d, LocalTime.of(11, 0)), "Boundary end time should be busy");
    assertFalse(calendar.isBusy(d, LocalTime.of(11, 1)), "After end time should be free");
  }

  // --- updateEvent @NonNull checks ---

  @Test
  void shouldThrowNullPointerOnUpdateWithNullOriginal() {
    Calendar calendar = new Calendar("Update Null Original");
    EventUpdate update = EventUpdate.builder().location("Room B").build();
    assertThrows(NullPointerException.class, () -> calendar.updateEvent(null, update, null));
  }

  @Test
  void shouldThrowNullPointerOnUpdateWithNullUpdate() {
    Calendar calendar = new Calendar("Update Null Update");
    Event original = SingleEvent.builder().subject("EditTest").startDate(DATE_1).endDate(DATE_1).startTime(TIME_9AM).endTime(TIME_10AM).location("Room A").build();
    calendar.createEvent(original);
    assertThrows(NullPointerException.class, () -> calendar.updateEvent(original, null, null));
  }

  @Test
  void shouldRollbackRecurringIndicesWhenDeleteThrowsMidway() {
    Calendar calendar = new Calendar("Rollback Rec Mid");
    RecurringEvent rec = RecurringEvent.builder()
      .subject("RecMid")
      .startDate(DATE_1).endDate(DATE_1)
      .startTime(TIME_9AM).endTime(TIME_10AM)
      .recurrenceDays(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
      .repeatCount(3)
      .allowConflict(false)
      .build();
    calendar.createEvent(rec);

    // Force failure on the 3rd child to ensure some removals occurred before throw
    List<Event> children = new ArrayList<>(rec.getListEvents());
    Event third = children.get(children.size() - 1);
    String keyThird = third.getSubject() + third.getStartDate() + third.getStartTime();
    calendar.getEvents().remove(keyThird);

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> calendar.deleteEventTemp(rec));
    assertTrue(ex.getMessage().contains("Event does not exist"));

    // Recurring index should be restored in catch path
    assertTrue(calendar.getRecurringEvents().containsKey(rec.getId()));
  }

  @Test
  void shouldThrowWhenDeletingNonexistentRecurringEvent() {
    Calendar calendar = new Calendar("Delete Nonexistent Rec");
    RecurringEvent rec = RecurringEvent.builder()
      .subject("RecNone")
      .startDate(DATE_1).endDate(DATE_1)
      .startTime(TIME_9AM).endTime(TIME_10AM)
      .recurrenceDays(Set.of(DayOfWeek.MONDAY))
      .repeatCount(2)
      .build();

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> calendar.deleteEventTemp(rec));
    assertTrue(ex.getMessage().contains("Event does not exist"));
  }

}