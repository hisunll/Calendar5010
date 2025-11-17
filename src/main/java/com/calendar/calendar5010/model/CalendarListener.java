package com.calendar.calendar5010.model;

/**
 * A listener interface for receiving notifications about changes to a
 * {@link Calendar}'s events.
 */
public interface CalendarListener {
  /**
   * Called when a new event has been successfully added to the calendar.
   *
   * @param event the event that was added
   */
  void onEventAdded(Event event);

  /**
   * Called when a new event has been successfully modified.
   *
   * @param event the event that was modified
   */
  void onEventModified(Event event);
}