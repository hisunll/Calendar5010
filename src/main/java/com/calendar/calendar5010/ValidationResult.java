package com.calendar.calendar5010;

import lombok.Getter;

@Getter
public class ValidationResult {
  private final Boolean valid;
  private final String message;

  private ValidationResult(Boolean valid, String message) {
    this.valid = valid;
    this.message = message;
  }

  public static ValidationResult valid() {
    return new ValidationResult(true, "Valid");
  }

  public static ValidationResult error(String message) {
    return new ValidationResult(false, message);
  }
}
