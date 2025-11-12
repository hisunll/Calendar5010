package com.calendar.calendar5010;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Calendar maintains the event collections, performs conflict checks.
 * It provides core operations for create, update, and query.
 */
@Getter
@Setter
public class Calendar {
  private String title;
  private Boolean allowConflict =  false;

  @Setter(AccessLevel.NONE)
  @Getter(AccessLevel.NONE)
  private Map<String, Event> events = new HashMap<>();

  @Setter(AccessLevel.NONE)
  @Getter(AccessLevel.NONE)
  private Map<String, Event> eventsId = new HashMap<>();

  @Setter(AccessLevel.NONE)
  @Getter(AccessLevel.NONE)
  private Map<String, RecurringEvent> recurringEvents = new HashMap<>();

  @Setter(AccessLevel.NONE)
  @Getter(AccessLevel.NONE)
  private final Map<LocalDate, Set<TimeInterval>> dailyBuckets = new HashMap<>();

  /**
   * Construct a calendar with the given title.
   *
   * @param title calendar title
   */
  public Calendar(String title) {
    this.title = title;
  }

  /**
   * Construct a calendar with the given title and default conflict policy.
   *
   * @param title calendar title
   * @param allowConflict default flag used when an event does not explicitly set allowConflict
   */
  public Calendar(String title, Boolean allowConflict) {
    this.title = title;
    this.allowConflict = allowConflict;
  }

  /**
   * Get the map of id and event.
   *
   * @return eventsId
   */
  public Map<String, Event> getEventsId() {
    return Collections.unmodifiableMap(eventsId);
  }

  /**
   * Get the map of id and recurring event.
   *
   * @return recurringEvents
   */
  public Map<String, RecurringEvent> getRecurringEvents() {
    return Collections.unmodifiableMap(recurringEvents);
  }

  private Boolean checkConflictDaily(TimeInterval timeInterval, Set<TimeInterval> timeIntervals) {
    if (timeIntervals == null) {
      return false;
    }
    return timeInterval.conflictsWith(timeIntervals);
  }

  private ValidationResult checkConflict(Event event) {
    List<TimeInterval> timeIntervals = event.getTimeIntervals();

    for (TimeInterval timeInterval : timeIntervals) {
      if (checkConflictDaily(timeInterval, dailyBuckets.get(timeInterval.date()))) {
        return ValidationResult.error("Conflicting with existing event");
      }
    }

    return ValidationResult.valid();
  }

  /**
   * Validate a list of events, including duplicate checks and conflicts against existing events.
   *
   * @param eventsList events to validate
   * @return {@link ValidationResult#valid()} when all are valid,
   *     otherwise a result with an error message
   */
  public ValidationResult checkIsValid(List<Event> eventsList) {
    for (Event event : eventsList) {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(event.getSubject())
          .append(event.getStartDate())
          .append(event.getStartTime());

      if (events.containsKey(stringBuilder.toString())) {
        return ValidationResult.error("Event already exists");
      }

      ValidationResult validationResult = checkConflict(event);
      if (!event.getAllowConflict() && !validationResult.getValid()) {
        return validationResult;
      }
    }

    return ValidationResult.valid();
  }

  private void addEvent(Event event) {
    List<Event> eventsList = event.getListEvents();
    for (Event e : eventsList) {
      String stringBuilder = e.getSubject()
          + e.getStartDate()
          + e.getStartTime();
      events.put(stringBuilder, e);
      eventsId.put(e.getId(), e);
    }

    for (TimeInterval interval : event.getTimeIntervals()) {
      dailyBuckets.computeIfAbsent(interval.date(), k -> new HashSet<>()).add(interval);
    }

    if (event instanceof RecurringEvent) {
      RecurringEvent recurringEvent = (RecurringEvent) event;
      recurringEvents.put(recurringEvent.getId(), recurringEvent);
    }
  }

  /**
   * Create an event and write it into the calendar, maintaining daily buckets and indices.
   *
   * @param event event to create
   * @throws IllegalArgumentException when the event is invalid or produces disallowed conflicts
   */
  public void createEvent(@NonNull Event event) {
    if (event.checkAllowConflictNull()) {
      event.setAllowConflict(allowConflict);
    }

    ValidationResult validationResult = Util.checkIsValid(event, this);
    if (!validationResult.getValid()) {
      throw new IllegalArgumentException(validationResult.getMessage());
    }

    addEvent(event);
  }

  /**
   * Temporarily delete an event and its expanded children.
   *
   * @param event event to remove
   * @throws IllegalArgumentException when the event does not exist
   */
  public void deleteEventTemp(@NonNull Event event) {
    Map<String, Event> removedEvents = new HashMap<>();
    List<Event> eventsList = event.getListEvents();

    if (event instanceof RecurringEvent) {
      RecurringEvent recurringEvent = (RecurringEvent) event;
      if (!recurringEvents.containsKey(recurringEvent.getId())) {
        throw new IllegalArgumentException("Event does not exist");
      }
      recurringEvents.remove(recurringEvent.getId());
    }

    try {
      for (Event e : eventsList) {
        String key = e.getSubject()
            + e.getStartDate()
            + e.getStartTime();
        if (!events.containsKey(key)
            || !e.equals(events.get(key))
            || !eventsId.containsKey(e.getId())) {
          throw new IllegalArgumentException("Event does not exist");
        }
        removedEvents.put(key, events.get(key));
        events.remove(key);
        eventsId.remove(e.getId());
      }

      for (TimeInterval interval : event.getTimeIntervals()) {
        LocalDate now = interval.date();
        Set<TimeInterval> intervals = dailyBuckets.get(now);

        if (intervals != null) {
          intervals.removeIf(i -> i.eventId().equals(interval.eventId()));
          if (intervals.isEmpty()) {
            dailyBuckets.remove(now);
          }
        }
      }
    } catch (Exception e) {
      for (Map.Entry<String, Event> entry : removedEvents.entrySet()) {
        events.put(entry.getKey(), entry.getValue());
        eventsId.put(entry.getValue().getId(), entry.getValue());
      }
      if (event instanceof RecurringEvent) {
        RecurringEvent recurringEvent = (RecurringEvent) event;
        recurringEvents.put(recurringEvent.getId(), recurringEvent);
      }
      throw e;
    }


  }

  /**
   * Update an event.
   *
   * @param original original event
   * @param update update data
   * @param date date to update
   */
  public void updateEvent(@NonNull Event original, @NonNull EventUpdate update, LocalDate date) {
    Util.updateEvent(this, original, update, date);
  }

  /**
   * Retrieve an event by subject, date, and start time.
   *
   * @param subject subject
   * @param startDate start date
   * @param startTime start time
   * @return matched event
   */
  public Event getEvent(String subject, LocalDate startDate, LocalTime startTime) {
    String key = subject + startDate + startTime;
    return events.get(key);
  }

  /**
   * Retrieve all events for the given list of dates.
   *
   * @param dates list of dates
   * @return list containing all events for matching dates
   */
  public List<Event> getEventByDate(List<LocalDate> dates) {
    Set<Event> eventsDate = new HashSet<>();

    for (LocalDate date : dates) {
      Set<TimeInterval> intervals = dailyBuckets.get(date);

      for (TimeInterval interval : intervals) {
        String key = interval.eventId();
        eventsDate.add(eventsId.get(key));
      }
    }

    return new ArrayList<>(eventsDate);
  }

  /**
   * Determine whether the given date and time are busy.
   *
   * @param date date
   * @param time time
   * @return {@code true} if busy, otherwise {@code false}
   */
  public Boolean isBusy(LocalDate date, LocalTime time) {
    LocalDateTime dateTime = LocalDateTime.of(date, time);
    LocalDateTime oneMinuteBefore = dateTime.minusMinutes(1);
    TimeInterval timeInterval = new TimeInterval("temp", date, oneMinuteBefore.toLocalTime(), time);
    return checkConflictDaily(timeInterval, dailyBuckets.get(date));
  }

}