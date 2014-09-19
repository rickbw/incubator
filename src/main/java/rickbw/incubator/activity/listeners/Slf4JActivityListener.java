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
package rickbw.incubator.activity.listeners;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

import rickbw.incubator.activity.Activity;
import rickbw.incubator.activity.ActivityId;
import rickbw.incubator.activity.ActivityListener;
import rickbw.incubator.activity.Activity.Execution;
import rickbw.incubator.activity.listeners.Slf4JActivityListener.ExecutionContext;


/**
 * An {@link ActivityListener} that logs all of its callbacks using the
 * SLF4J API.
 */
public final class Slf4JActivityListener implements ActivityListener<Logger, ExecutionContext> {

    private static final LogLevel DEFAULT_START_LEVEL = LogLevel.INFO;
    private static final LogLevel DEFAULT_COMPLETE_LEVEL = LogLevel.TRACE;
    private static final LogLevel DEFAULT_FAIL_LEVEL = LogLevel.ERROR;

    private final LogLevel startLevel;
    private final LogLevel completeLevel;
    private final LogLevel failLevel;


    public Slf4JActivityListener() {
        this(DEFAULT_START_LEVEL, DEFAULT_COMPLETE_LEVEL, DEFAULT_FAIL_LEVEL);
    }

    public Slf4JActivityListener(
            final LogLevel startCompleteLevel,
            final LogLevel failLevel) {
        this(startCompleteLevel, startCompleteLevel, failLevel);
    }

    public Slf4JActivityListener(
            final LogLevel startLevel,
            final LogLevel completeLevel,
            final LogLevel failLevel) {
        this.startLevel = Objects.requireNonNull(startLevel);
        this.completeLevel = Objects.requireNonNull(completeLevel);
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
        if (this.startLevel.isEnabled(logger)) {
            final String format = buildPrefixedMessage(execution, "--> ", "Started {}");
            this.startLevel.log(logger, format, execution.getId());
        }
        return new ExecutionContext(logger, execution);
    }

    @Override
    public void onExecutionFailure(final ExecutionContext context, final Throwable failure) {
        this.failLevel.log(context.logger, "Failure in {}", context.execution, failure);
    }

    @Override
    public void onExecutionCompleted(final ExecutionContext context) {
        if (this.completeLevel.isEnabled(context.logger)) {
            final String format = buildPrefixedMessage(context.execution, "<-- ", "Completed {}");
            this.completeLevel.log(context.logger, format, context.execution);
        }
    }

    private static String buildPrefixedMessage(
            final Activity.Execution execution,
            final String delim,
            final String suffix) {
        final StringBuilder buf = new StringBuilder();
        for (Optional<Activity.Execution> parent = execution.getParent();
                parent.isPresent();
                parent = parent.get().getParent()) {
            buf.append(delim);
        }
        buf.append(suffix);
        return buf.toString();
    }


    public static enum LogLevel {
        ERROR {
            @Override
            boolean isEnabled(final Logger logger) {
                return logger.isErrorEnabled();
            }

            @Override
            void log(final Logger logger, final String format, final Object... args) {
                logger.error(format, args);
            }
        },

        WARN {
            @Override
            boolean isEnabled(final Logger logger) {
                return logger.isWarnEnabled();
            }

            @Override
            void log(final Logger logger, final String format, final Object... args) {
                logger.warn(format, args);
            }
        },

        INFO {
            @Override
            boolean isEnabled(final Logger logger) {
                return logger.isInfoEnabled();
            }

            @Override
            void log(final Logger logger, final String format, final Object... args) {
                logger.info(format, args);
            }
        },

        DEBUG {
            @Override
            boolean isEnabled(final Logger logger) {
                return logger.isDebugEnabled();
            }

            @Override
            void log(final Logger logger, final String format, final Object... args) {
                logger.debug(format, args);
            }
        },

        TRACE {
            @Override
            boolean isEnabled(final Logger logger) {
                return logger.isTraceEnabled();
            }

            @Override
            void log(final Logger logger, final String format, final Object... args) {
                logger.trace(format, args);
            }
        },

        OFF {
            @Override
            boolean isEnabled(final Logger logger) {
                return false;
            }

            @Override
            void log(final Logger logger, final String format, final Object... args) {
                // do nothing
            }
        },
        ;

        /*package*/ abstract boolean isEnabled(Logger logger);
        /*package*/ abstract void log(Logger logger, String format, Object... args);
    }


    public static final class ExecutionContext {
        private final Logger logger;
        private final Activity.Execution execution;

        private ExecutionContext(final Logger logger, final Activity.Execution execution) {
            this.logger = logger;
            this.execution = execution;
        }
    }

}
