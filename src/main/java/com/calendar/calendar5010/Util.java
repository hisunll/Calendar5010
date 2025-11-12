package com.calendar.calendar5010;

import java.time.LocalDate;
import java.util.function.Consumer;
import lombok.NonNull;

/**
 * Utility helpers for event validation and safe calendar updates.
 */
public class Util {
  private Util() {}

  /**
   * Validates the given event both individually and against the calendar.
   *
   * @param event the event to validate
   * @param calendar the calendar used to check for conflicts
   * @return a {@link ValidationResult} indicating validity or providing an error message
   */
  public static ValidationResult checkIsValid(Event event, Calendar calendar) {
    ValidationResult validationResultEvent = event.checkIsValid();
    ValidationResult validationResultCalendar;
    validationResultCalendar = calendar.checkIsValid(event.getListEvents());
    if (!validationResultCalendar.getValid()) {
      return validationResultCalendar;
    }

    return validationResultEvent;
  }

  /**
   * Applies a partial update to an existing event in the calendar safely.
   *
   * @param calendar the calendar to update
   * @param original the original event that will be updated
   * @param update the update payload containing optional fields
   * @param startDate the date from which the update is considered effective
   * @throws IllegalArgumentException when the updated event is invalid
   */
  public static void updateEvent(@NonNull Calendar calendar, @NonNull Event original,
                                 @NonNull EventUpdate update, LocalDate startDate) {
    Event oldEvent = original.deepCopy();
    setIfNotNull(startDate, oldEvent::setStartDate);
    setIfNotNull(startDate, update::setStartDate);
    oldEvent.prepareForUpdate();
    Event newEvent = original.deepCopy();
    applyUpdates(newEvent, update);
    newEvent.prepareForUpdate();
    calendar.deleteEventTemp(oldEvent);

    ValidationResult validationResult = checkIsValid(newEvent, calendar);

    if (!validationResult.getValid()) {
      calendar.createEvent(oldEvent);
      throw new IllegalArgumentException(validationResult.getMessage());
    }

    original.copyFrom(newEvent, startDate);
    calendar.createEvent(newEvent);
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
      recurringEvent.generateSingleEvents();
    }
  }

  private static <T> void setIfNotNull(T value, Consumer<T> setter) {
    if (value != null) {
      setter.accept(value);
    }
  }
}
