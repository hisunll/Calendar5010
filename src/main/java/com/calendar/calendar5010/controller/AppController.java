package com.calendar.calendar5010.controller;

import com.calendar.calendar5010.model.Calendar;
import com.calendar.calendar5010.model.CalendarPersistenceManager;
import com.calendar.calendar5010.model.Event;
import com.calendar.calendar5010.view.CreateEventView;
import com.calendar.calendar5010.view.EventDetailView;
import com.calendar.calendar5010.view.EventListView;
import java.nio.file.Path;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * High-level application controller that wires the model,
 * the CalendarController listener, and all views together.
 */
@SuppressWarnings("EI_EXPOSE_REP")
@Getter
public class AppController {

  @Getter(AccessLevel.NONE)
  private final CalendarController calendarListener = new CalendarController();
  @Getter(AccessLevel.NONE)
  private List<Calendar> calendars;
  @Getter(AccessLevel.NONE)
  private Calendar selected;

  private CreateEventView createEventView;
  private EventDetailView eventDetailView;
  private EventDetailView detailView;
  private EventListView listView;

  /**
   * Restore all calendars from the given directory.
   */
  public void restoreCalendars(Path dir) throws Exception {
    calendars = CalendarPersistenceManager.restoreAllCalendars(dir);

    if (calendars.isEmpty()) {
      selected = new Calendar("Default");
    } else {
      selected = calendars.get(0);
    }

    // register listener
    selected.addCalendarListener(calendarListener);
  }

  /**
   * Builds and shows UI views.
   */
  public void buildViews() {

    if (selected == null) {
      throw new IllegalStateException("restoreCalendars must be called first.");
    }

    createEventView = new CreateEventView(selected, this);
    Event show = selected.getEventsId().values().stream().findFirst().orElse(null);

    if (show != null) {
      eventDetailView = new EventDetailView(selected, show, this);
    }

    listView = new EventListView(this, selected);
    listView.setVisible(true);
  }

  /**
   * Displays the detail view for the specified event.
   *
   * @param event the event whose details should be displayed
   */
  public void showEventDetail(Event event) {
    detailView = new EventDetailView(selected, event, this);
    detailView.setVisible(true);
  }

}
