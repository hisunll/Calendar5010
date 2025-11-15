package com.calendar.calendar5010.view;

import com.calendar.calendar5010.model.Calendar;
import com.calendar.calendar5010.model.Event;
import com.calendar.calendar5010.model.SingleEvent;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * View responsible for creating events.
 * It holds a reference to a specific Calendar and calls its createEvent()
 * when the user submits the form.
 */
public class CreateEventView {

  private final Calendar calendar;

  /**
   * Constructs a view responsible for creating new events in the given calendar.
   *
   * @param calendar the calendar into which new events will be created
   */
  public CreateEventView(Calendar calendar) {
    this.calendar = calendar;
  }

  /**
   * Simulates the user filling the "create event" form and clicking "Save".
   * This view does NOT check conflict — Calendar handles that.
   */
  public void submitNewEvent(
      String subject,
      LocalDate startDate,
      LocalTime startTime,
      LocalDate endDate,
      LocalTime endTime,
      String description,
      String location
  ) {
    Event event = SingleEvent.builder()
        .subject(subject)
        .startDate(startDate)
        .startTime(startTime)
        .endDate(endDate)
        .endTime(endTime)
        .description(description)
        .location(location)
        .visibility(Event.Visibility.PUBLIC)
        .build();

    calendar.createEvent(event);
  }
}
