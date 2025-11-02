package com.calendar.calendar5010;

import lombok.NonNull;

import java.time.LocalDate;
import java.util.function.Consumer;

public class Util {
  public static ValidationResult checkIsValid(Event event, Calendar calendar) {
    ValidationResult validationResultEvent = event.checkIsValid();
    ValidationResult validationResultCalendar;
    validationResultCalendar = calendar.checkIsValid(event.getListEvents());
    if(!validationResultCalendar.getValid()) {
      return validationResultCalendar;
    }

    return validationResultEvent;
  }

  public static void updateEvent(@NonNull Calendar calendar, @NonNull Event original,
                                 @NonNull EventUpdate update, LocalDate startDate) {
    Event newEvent = original.deepCopy();
    Event oldEvent = original.deepCopy();
    setIfNotNull(startDate, oldEvent::setStartDate);
    setIfNotNull(startDate, update::setStartDate);
    oldEvent.prepareForUpdate();
    applyUpdates(newEvent, update);

    ValidationResult validationResult = checkIsValid(newEvent, calendar);

    if (!validationResult.getValid()) {
      calendar.addEvent(oldEvent);
      throw new IllegalArgumentException(validationResult.getMessage());
    }

    original.copyFrom(newEvent, startDate);
    calendar.addEvent(original);
  }

  private static void applyUpdates(Event event, EventUpdate update) {
    setIfNotNull(update.getStartDate(), event::setStartDate);
    setIfNotNull(update.getStartTime(), event::setStartTime);
    setIfNotNull(update.getEndTime(), event::setEndTime);
    setIfNotNull(update.getEndDate(), event::setEndDate);
    setIfNotNull(update.getDescription(), event::setDescription);
    setIfNotNull(update.getLocation(), event::setLocation);
    setIfNotNull(update.getAllowConflict(), event::setAllowConflict);
    setIfNotNull(update.getVisibility(), event::setVisibility);

    if (event instanceof RecurringEvent) {
      RecurringEvent recurringEvent = (RecurringEvent) event;
      setIfNotNull(update.getRecurrenceDays(), recurringEvent::setRecurrenceDays);
      setIfNotNull(update.getRepeatCount(), recurringEvent::setRepeatCount);
      setIfNotNull(update.getRecurrenceEndDate(), recurringEvent::setRecurrenceEndDate);
      recurringEvent.prepareForUpdate();
    }
  }

  private static <T> void setIfNotNull(T value, Consumer<T> setter) {
    if (value != null) {
      setter.accept(value);
    }
  }
}
