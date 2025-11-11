package com.calendar.calendar5010;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Getter
@Setter
public class Calendar {
  private String title;
  private Map<String, Event> events = new HashMap<>();
  private Boolean allowConflict =  false;
  private Map<String, Event> eventsId = new HashMap<>();
  private Map<String, RecurringEvent> recurringEvents = new HashMap<>();

  private final Map<LocalDate, Set<TimeInterval>> dailyBuckets = new HashMap<>();

  public Calendar(String title) {
    this.title = title;
  }

  public Calendar(String title, Boolean allowConflict) {
    this.title = title;
    this.allowConflict = allowConflict;
  }

  private Boolean checkConflictDaily(TimeInterval timeInterval, Set<TimeInterval> timeIntervals) {
    if(timeIntervals == null) {
      return false;
    }
    return timeInterval.conflictsWith(timeIntervals);
  }

  private ValidationResult checkConflict(Event event) {
    List<TimeInterval> timeIntervals = event.getTimeIntervals();

    for (TimeInterval timeInterval : timeIntervals) {
      if(checkConflictDaily(timeInterval, dailyBuckets.get(timeInterval.date()))) {
        return ValidationResult.error("Conflicting with existing event");
      }
    }

    return ValidationResult.valid();
  }

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
      String stringBuilder = e.getSubject() +
        e.getStartDate() +
        e.getStartTime();
      events.put(stringBuilder, e);
      eventsId.put(e.getId(), e);
    }

    for (TimeInterval interval : event.getTimeIntervals()) {
      dailyBuckets.computeIfAbsent(interval.date(), k -> new HashSet<>()).add(interval);
    }

    if(event instanceof RecurringEvent) {
      RecurringEvent recurringEvent = (RecurringEvent) event;
      recurringEvents.put(recurringEvent.getId(), recurringEvent);
    }
  }

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

  public void deleteEventTemp(@NonNull Event event) {
    Map<String, Event> removedEvents = new HashMap<>();
    List<Event> eventsList = event.getListEvents();

    if (event instanceof RecurringEvent) {
      RecurringEvent recurringEvent = (RecurringEvent) event;
      if(!recurringEvents.containsKey(recurringEvent.getId())) {
        throw new IllegalArgumentException("Event does not exist");
      }
      recurringEvents.remove(recurringEvent.getId());
    }

    try {
      for (Event e : eventsList) {
        String key = e.getSubject() +
          e.getStartDate() +
          e.getStartTime();
        if(!events.containsKey(key) || !e.equals(events.get(key)) || !eventsId.containsKey(e.getId())) {
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

  public void updateEvent(@NonNull Event original, @NonNull EventUpdate update, LocalDate date) {
    Util.updateEvent(this, original, update, date);
  }

  public Event getEvent(String subject, LocalDate startDate, LocalTime startTime) {
    String key = subject +
      startDate +
      startTime;
    return events.get(key);
  }

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

  public Boolean isBusy(LocalDate date, LocalTime time) {
    LocalDateTime dateTime = LocalDateTime.of(date, time);
    LocalDateTime oneMinuteBefore = dateTime.minusMinutes(1);
    TimeInterval timeInterval = new TimeInterval("temp", date, oneMinuteBefore.toLocalTime(), time);
    return checkConflictDaily(timeInterval, dailyBuckets.get(date));
  }

}