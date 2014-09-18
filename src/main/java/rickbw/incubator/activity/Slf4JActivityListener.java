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

import rickbw.incubator.activity.Slf4JActivityListener.ExecutionContext;


/**
 * An {@link ActivityListener} that logs all of its callbacks using the
 * SLF4J API.
 */
public final class Slf4JActivityListener implements ActivityListener<Logger, ExecutionContext> {

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
    public Logger onActivityInitialized(final Activity activity) {
        final ActivityId id = activity.getId();
        final String loggerName = id.getGroupName() + '.' + id.getTypeName();
        return LoggerFactory.getLogger(loggerName);
    }

    @Override
    public ExecutionContext onExecutionStarted(final Activity.Execution execution, final Logger logger) {
        final ExecutionId id = execution.getId();
        log(logger, this.startCompleteLevel, "Started {}", id);
        return new ExecutionContext(logger, id);
    }

    @Override
    public void onExecutionFailure(final ExecutionContext context, final Throwable failure) {
        log(context.logger, this.failLevel, "Failure in {}", context.id, failure);
    }

    @Override
    public void onExecutionCompleted(final ExecutionContext context) {
        log(context.logger, this.startCompleteLevel, "Completed {}", context.id);
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


    public static enum LogLevel {
        ERROR,
        WARN,
        INFO,
        DEBUG,
        TRACE,
        OFF
    }


    public static final class ExecutionContext {
        private final Logger logger;
        private final ExecutionId id;

        private ExecutionContext(final Logger logger, final ExecutionId id) {
            this.logger = logger;
            this.id = id;
        }
    }

}
