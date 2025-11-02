package com.calendar.calendar5010;

import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class RecurringEvent extends Event{
  //required
  private Set<DayOfWeek> recurrenceDays;

  //optional
  private Integer repeatCount;
  private LocalDate recurrenceEndDate;
  @Setter(AccessLevel.NONE)
  private List<Event> events = new ArrayList<>();

  @Builder
  public RecurringEvent(String subject, LocalDate startDate, LocalTime startTime,
                        LocalDate endDate, LocalTime endTime,
                        Visibility visibility, String description,
                        String location, boolean allowConflict,
                        Set<DayOfWeek> recurrenceDays,
                        Integer repeatCount, LocalDate recurrenceEndDate) {
    super(subject, startDate, startTime, endDate, endTime, visibility, description, location, allowConflict);
    this.recurrenceDays = recurrenceDays;
    this.repeatCount = repeatCount;
    this.recurrenceEndDate = recurrenceEndDate;
    ValidationResult validationResult = checkIsValid();
    if(!validationResult.getValid()) {
      throw new IllegalArgumentException(validationResult.getMessage());
    }
    generateSingleEvents();
    setTimeIntervals();
  }

  private void generateSingleEvents() {
    this.events.clear();
    LocalDate current = getStartDate();
    int count = 0;

    LocalDate endLimit = recurrenceEndDate != null ? recurrenceEndDate : getEndDate().plusWeeks(repeatCount);

    while (!current.isAfter(endLimit)) {
      if (recurrenceDays.contains(current.getDayOfWeek())) {
        if (repeatCount != null && count >= repeatCount) {
          break;
        }

        SingleEvent singleEvent = SingleEvent.builder()
          .subject(getSubject())
          .startDate(current)
          .startTime(getStartTime())
          .endDate(current)
          .endTime(getEndTime())
          .visibility(getVisibility())
          .description(getDescription())
          .location(getLocation())
          .allowConflict(getAllowConflict())
          .build();

        this.events.add(singleEvent);
        count++;
      }

      current = current.plusDays(1);
    }

  }

  @Override
  public void setTimeIntervals() {
    getTimeIntervals().clear();

    for(Event event : events) {
      getTimeIntervals().addAll(event.getTimeIntervals());
    }
  }

  @Override
  public ValidationResult checkIsValid(){
    LocalDate startDate = getStartDate();
    LocalTime startTime = getStartTime();
    LocalDate endDate = getEndDate();
    LocalTime endTime = getEndTime();
    String subject = getSubject();

    if (startTime == null && endTime != null) {
      return ValidationResult.error("Cannot set endTime if startTime is null");
    }

    if (subject == null || startDate == null || endDate == null || recurrenceDays == null) {
      return ValidationResult.error("Missing required parameters.");
    }

    if (repeatCount == null && recurrenceEndDate == null) {
      return ValidationResult.error("Missing required parameters.");
    }

    if (repeatCount != null && recurrenceEndDate != null) {
      return ValidationResult.error("Cannot set both repeatCount and recurrenceEndDate at the same time");
    }

    if(getEndDateTime().isBefore(getStartDateTime())){
      return ValidationResult.error("End time cannot be before start time");
    }

    if(startDate.isBefore(endDate)){
      return ValidationResult.error("Recurring event cannot cross day");
    }

    return ValidationResult.valid();
  }
}