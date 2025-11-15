package com.calendar.calendar5010.controller;

import com.calendar.calendar5010.model.Calendar;
import com.calendar.calendar5010.model.CalendarListener;
import com.calendar.calendar5010.model.CalendarPersistenceManager;
import com.calendar.calendar5010.model.Event;
import com.calendar.calendar5010.view.CreateEventView;
import com.calendar.calendar5010.view.EventDetailView;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * A controller that implements CalendarListener.
 */
@Getter
public class CalendarController implements CalendarListener {

  @Getter(AccessLevel.NONE)
  private final List<Event> addedEvents = new ArrayList<>();
  @Getter(AccessLevel.NONE)
  private final List<Event> modifiedEvents = new ArrayList<>();
  @Getter(AccessLevel.NONE)
  private List<Calendar> calendars = new ArrayList<>();

  private Calendar selectedCalendar;

  private CreateEventView createEventView;
  private EventDetailView eventDetailView;

  @Override
  public void onEventAdded(Event event) {
    if (event != null) {
      addedEvents.add(event);
    }
  }

  @Override
  public void onEventModified(Event event) {
    if (event != null) {
      modifiedEvents.add(event);
    }
  }

  /**
   * Returns an unmodifiable view of the list containing all events that were added.
   *
   * @return an unmodifiable list of {@link Event} objects that were added
   */
  public List<Event> getAddedEvents() {
    return Collections.unmodifiableList(addedEvents);
  }

  /**
   * Returns an unmodifiable view of the list containing all events that were modified.
   *
   * @return an unmodifiable list of {@link Event} objects that were modified
   */
  public List<Event> getModifiedEvents() {
    return Collections.unmodifiableList(modifiedEvents);
  }

  /**
   * Returns an unmodifiable view of the list containing all calendars.
   *
   * @return an unmodifiable list of {@link Calendar} objects
   */
  public List<Calendar> getCalendars() {
    return Collections.unmodifiableList(calendars);
  }

  /**
   * Restores all calendars from disk and selects one to work with.
   *
   * @param storagePath Directory where calendars are saved
   */
  public void restoreCalendars(Path storagePath) {
    calendars = CalendarPersistenceManager.restoreAllCalendars(storagePath);

    if (calendars.isEmpty()) {
      selectedCalendar = new Calendar("Default Calendar");
      calendars.add(selectedCalendar);
    } else {
      selectedCalendar = calendars.get(0);
    }

    selectedCalendar.addCalendarListener(this);
  }

  /**
   * Creates and initializes the views.
   * EventDetailView will show the first event if present.
   *
   * @throws IllegalStateException if {@code selectedCalendar} has not been set
   */
  public void buildViews() {
    if (selectedCalendar == null) {
      throw new IllegalStateException("restoreCalendars must be called first.");
    }

    createEventView = new CreateEventView(selectedCalendar);

    Event eventToShow = selectedCalendar.getEventsId().values()
        .stream()
        .findFirst()
        .orElse(null);

    if (eventToShow != null) {
      eventDetailView = new EventDetailView(selectedCalendar, eventToShow);
    }
  }

}
