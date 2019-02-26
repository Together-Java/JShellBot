package org.togetherjava.discord.server.execution;

import java.time.Duration;
import org.apache.commons.lang3.time.DurationFormatUtils;

/**
 * Indicates the the time you were allowed to use was exceeded.
 */
public class AllottedTimeExceededException extends RuntimeException {

  /**
   * Creates a new alloted time exception.
   *
   * @param maxTime the macimum allowed time that was exceeded
   */
  public AllottedTimeExceededException(Duration maxTime) {
    super(
        "You exceeded the allotted time of '"
            + DurationFormatUtils.formatDurationWords(maxTime.toMillis(), true, true)
            + "'."
    );
  }
}
