package com.calendar.calendar5010.model;

public interface CalendarListener {
  void onEventAdded(Event event);
  void onEventModified(Event event);
}