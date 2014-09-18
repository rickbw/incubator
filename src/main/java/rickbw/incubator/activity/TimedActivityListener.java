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
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.Timer;

import rickbw.incubator.activity.TimedActivityListener.TimedActivity;
import rickbw.incubator.activity.TimedActivityListener.TimedExecution;


public final class TimedActivityListener implements ActivityListener<TimedActivity, TimedExecution> {

    private final TimeUnit durationUnit;
    private final TimeUnit rateUnit;


    public TimedActivityListener() {
        this(TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
    }

    public TimedActivityListener(final TimeUnit durationUnit, final TimeUnit rateUnit) {
        this.durationUnit = Objects.requireNonNull(durationUnit);
        this.rateUnit = Objects.requireNonNull(rateUnit);
    }

    @Override
    public TimedActivity onActivityInitialized(final Activity activity) {
        return new TimedActivity(activity.getId());
    }

    @Override
    public TimedExecution onExecutionStarted(final Activity.Execution execution, final TimedActivity timed) {
        return new TimedExecution(timed);
    }

    @Override
    public void onExecutionFailure(final TimedExecution timed, final Throwable failure) {
        timed.failureOccurred();
    }

    @Override
    public void onExecutionCompleted(final TimedExecution timed) {
        timed.completed();
    }


    public final class TimedActivity {
        private final Timer timer;
        private final Meter failureMeter;

        private TimedActivity(final ActivityId id) {
            final String timerActivityName = id.getActivityName() + "-times";
            final MetricName timerName = new MetricName(id.getGroupName(), id.getTypeName(), timerActivityName);
            this.timer = Metrics.newTimer(timerName, durationUnit, rateUnit);

            final String failureMeterActivityName = id.getActivityName() + "-failures";
            final MetricName failureMeterName = new MetricName(id.getGroupName(), id.getTypeName(), failureMeterActivityName);
            this.failureMeter = Metrics.newMeter(failureMeterName, "failures", rateUnit);
        }
    }

    public final class TimedExecution {
        private final TimedActivity timed;
        private final Stopwatch stopwatch = Stopwatch.createStarted();

        private TimedExecution(final TimedActivity timed) {
            this.timed = timed;
        }

        private void failureOccurred() {
            this.timed.failureMeter.mark();
        }

        private void completed() {
            final long elapsed = this.stopwatch.stop().elapsed(TimeUnit.NANOSECONDS);
            this.timed.timer.update(elapsed, TimeUnit.NANOSECONDS);
        }
    }

}
