package com.calendar.calendar5010;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CalendarTest {

  private Calendar calendar;
  private final LocalDate DATE_1 = LocalDate.of(2025, 11, 10);
  private final LocalTime TIME_9AM = LocalTime.of(9, 0);

  @BeforeEach
  void setUp() {
    calendar = new Calendar("Work Schedule");
  }

  // --- Create Single Event Tests ---

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
}