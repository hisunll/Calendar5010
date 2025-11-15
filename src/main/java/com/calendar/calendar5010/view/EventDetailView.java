package com.calendar.calendar5010.view;

import com.calendar.calendar5010.model.Calendar;
import com.calendar.calendar5010.model.Event;
import com.calendar.calendar5010.model.EventUpdate;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * View responsible for displaying and modifying a specific Event.
 * This view does NOT check conflicts — Calendar handles validation.
 */
public class EventDetailView {

  private final Calendar calendar;
  private final Event event;

  /**
   * Constructs a view for displaying and editing the details of a specific event.
   *
   * @param calendar the calendar that owns the event
   * @param event the event whose details will be displayed and edited
   */
  public EventDetailView(Calendar calendar, Event event) {
    this.calendar = calendar;
    this.event = event;
  }

  /**
   * Shows event details (in a real UI, this would populate fields).
   */
  public Event getDisplayedEvent() {
    return event;
  }

  /**
   * User edits something and clicks "Save".
   * This view constructs an EventUpdate and lets Calendar handle the update.
   */
  public void submitModifiedEvent(
      String newSubject,
      LocalDate newStartDate,
      LocalTime newStartTime,
      LocalDate newEndDate,
      LocalTime newEndTime,
      String newDescription,
      String newLocation
  ) {

    EventUpdate update = EventUpdate.builder()
        .subject(newSubject)
        .startDate(newStartDate)
        .startTime(newStartTime)
        .endDate(newEndDate)
        .endTime(newEndTime)
        .description(newDescription)
        .location(newLocation)
        .build();

    // this view does not check conflict
    calendar.updateEvent(event, update, event.getStartDate());
  }
}
