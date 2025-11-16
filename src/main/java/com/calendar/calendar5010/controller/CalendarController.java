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

}
