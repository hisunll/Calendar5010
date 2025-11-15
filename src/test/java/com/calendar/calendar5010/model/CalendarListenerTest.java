package com.calendar.calendar5010.model;

import controller.CalendarController;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class CalendarListenerTest {

  private SingleEvent newEvent(String name) {
    return SingleEvent.builder()
      .subject(name)
      .startDate(LocalDate.of(2025, 11, 20))
      .startTime(LocalTime.of(9, 0))
      .endDate(LocalDate.of(2025, 11, 20))
      .endTime(LocalTime.of(10, 0))
      .description("description")
      .location("location")
      .visibility(Event.Visibility.PUBLIC)
      .build();
  }

  @ParameterizedTest
  @CsvSource({
    "2, -1, 2",   // two listeners, both should receive event-added
    "3,  1, 2",   // three listeners, remove index=1, left 2 listeners
    "1, -1, 1"    // one listener, receives exactly one event
  })
  public void testEventAddedParameterized(int listenerCount, int removeIndex, int expectedAddCalls) {
    Calendar cal = new Calendar("MyCal");
    CalendarController[] controllers = new CalendarController[listenerCount];

    for (int i = 0; i < listenerCount; i++) {
      controllers[i] = new CalendarController();
      cal.addCalendarListener(controllers[i]);
    }

    if (removeIndex >= 0 && removeIndex < listenerCount) {
      cal.removeCalendarListener(controllers[removeIndex]);
    }

    SingleEvent e = newEvent("Meeting");
    cal.createEvent(e);
    int actualAddCalls = 0;

    for (CalendarController c : controllers) {
      if (!c.getAddedEvents().isEmpty()) {
        actualAddCalls++;
      }
    }

    assertEquals(expectedAddCalls, actualAddCalls);
  }

  @Test
  public void testDuplicateRegistration() {
    Calendar cal = new Calendar("DupTest");
    CalendarController c1 = new CalendarController();

    cal.addCalendarListener(c1);
    cal.addCalendarListener(c1);

    SingleEvent e = newEvent("Dup");
    cal.createEvent(e);

    assertEquals(1, c1.getAddedEvents().size());
    assertSame(e, c1.getAddedEvents().get(0));
  }

  /**
   * Test that all registered listeners receive modified-event notification.
   */
  @Test
  public void testEventModified() {
    Calendar cal = new Calendar("Cal");

    CalendarController c1 = new CalendarController();
    CalendarController c2 = new CalendarController();
    cal.addCalendarListener(c1);
    cal.addCalendarListener(c2);

    SingleEvent e = newEvent("Original");
    cal.createEvent(e);

    EventUpdate update = EventUpdate.builder()
      .subject("Updated")
      .build();

    cal.updateEvent(e, update, e.getStartDate());

    assertEquals(1, c1.getModifiedEvents().size());
    assertEquals(1, c2.getModifiedEvents().size());

    assertSame(e, c1.getModifiedEvents().get(0));
    assertSame(e, c2.getModifiedEvents().get(0));
  }
}
