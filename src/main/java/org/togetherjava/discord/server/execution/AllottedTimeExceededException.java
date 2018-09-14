package org.togetherjava.discord.server.execution;

import java.time.Duration;
import org.apache.commons.lang3.time.DurationFormatUtils;

/**
 * Indicates the the time you were allowed to use was exceeded.
 */
public class AllottedTimeExceededException extends RuntimeException {

  public AllottedTimeExceededException(Duration maxTime) {
    super(
        "You exceeded the alloted time of '"
            + DurationFormatUtils.formatDurationWords(maxTime.toMillis(), true, true)
            + "'."
    );
  }
}
