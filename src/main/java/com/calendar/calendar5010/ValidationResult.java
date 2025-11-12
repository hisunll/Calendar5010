package com.calendar.calendar5010;

import lombok.Getter;

/**
 * Represents the outcome of a validation operation.
 */
@Getter
public class ValidationResult {
  private final Boolean valid;
  private final String message;

  private ValidationResult(Boolean valid, String message) {
    this.valid = valid;
    this.message = message;
  }

  /**
   * Creates a successful validation result.
   *
   * @return a {@code ValidationResult} indicating success
   */
  public static ValidationResult valid() {
    return new ValidationResult(true, "Valid");
  }

  /**
   * Creates a failed validation result with the provided message.
   *
   * @param message explanation of the validation failure
   * @return a {@code ValidationResult} indicating failure
   */
  public static ValidationResult error(String message) {
    return new ValidationResult(false, message);
  }
}
