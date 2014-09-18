/* Copyright 2014 Rick Warren
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package rickbw.incubator.activity;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An {@link ActivityListener} that logs all of its callbacks using the
 * SLF4J API.
 */
public final class Slf4JActivityListener implements ActivityListener {

    public static final LogLevel DEFAULT_START_COMPLETE_LEVEL = LogLevel.INFO;
    public static final LogLevel DEFAULT_FAIL_LEVEL = LogLevel.ERROR;

    private final LogLevel startCompleteLevel;
    private final LogLevel failLevel;


    public Slf4JActivityListener() {
        this(DEFAULT_START_COMPLETE_LEVEL, DEFAULT_FAIL_LEVEL);
    }

    public Slf4JActivityListener(
            final LogLevel startCompleteLevel,
            final LogLevel failLevel) {
        this.startCompleteLevel = Objects.requireNonNull(startCompleteLevel);
        this.failLevel = Objects.requireNonNull(failLevel);
    }

    @Override
    public Object onStarted(final Activity activity) {
        final Logger logger = getLogger(activity.getId());
        log(logger, this.startCompleteLevel, "Activity {} started");
        return logger;
    }

    @Override
    public void onFailure(final Activity activity, final Throwable failure, final Object context) {
        final Logger logger = (Logger) context;
        log(logger, this.failLevel, "Failure in activity {}", failure);
    }

    @Override
    public void onCompleted(final Activity activity, final Object context) {
        final Logger logger = (Logger) context;
        log(logger, this.startCompleteLevel, "Activity {} completed");
    }

    /* This method is dumb. Why doesn't SLF4J have something like this built
     * in, as other Java logging APIs do?
     */
    private static void log(
            final Logger logger,
            final LogLevel level,
            final String format,
            final Object... args) {
        if (level != LogLevel.OFF) {
            switch (level) {
                case ERROR:
                    logger.error(format, args);
                    break;
                case WARN:
                    logger.warn(format, args);
                    break;
                case INFO:
                    logger.info(format, args);
                    break;
                case DEBUG:
                    logger.debug(format, args);
                    break;
                case TRACE:
                    logger.trace(format, args);
                    break;

                default:
                    throw new AssertionError(level);
            }
        }
    }

    private static Logger getLogger(final ActivityId id) {
        // FIXME: We shouldn't have to do this every time the activity runs
        final String loggerName = id.getGroupName() + '.' + id.getTypeName();
        return LoggerFactory.getLogger(loggerName);
    }


    public static enum LogLevel {
        ERROR,
        WARN,
        INFO,
        DEBUG,
        TRACE,
        OFF
    }

}
