package com.calendar.calendar5010.model;

import com.calendar.calendar5010.Event;

public interface CalendarListener {
  void onEventAdded(Event event);
  void onEventModified(Event event);
}