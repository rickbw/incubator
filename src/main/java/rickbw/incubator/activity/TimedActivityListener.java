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

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.Timer;


public class TimedActivityListener implements ActivityListener {

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
    public Object onStarted(final Activity activity) {
        return new TimedActivity(activity.getId());
    }

    @Override
    public void onFailure(final Activity activity, final Throwable failure, final Object context) {
        final TimedActivity timed = (TimedActivity) context;
        timed.failureOccurred();
    }

    @Override
    public void onCompleted(final Activity activity, final Object context) {
        final TimedActivity timed = (TimedActivity) context;
        timed.completed();
    }


    private final class TimedActivity {
        private final long startTimeNanos;
        private final Timer timer;
        private final Meter failureMeter;

        public TimedActivity(final ActivityId id) {
            // FIXME: We shouldn't have to do all this every time the activity runs

            final String timerActivityName = id.getActivityName() + "-times";
            final MetricName timerName = new MetricName(id.getGroupName(), id.getTypeName(), timerActivityName);
            this.timer = Metrics.newTimer(timerName, durationUnit, rateUnit);

            final String failureMeterActivityName = id.getActivityName() + "-failures";
            final MetricName failureMeterName = new MetricName(id.getGroupName(), id.getTypeName(), failureMeterActivityName);
            this.failureMeter = Metrics.newMeter(failureMeterName, "failures", rateUnit);

            this.startTimeNanos = System.nanoTime();
        }

        public void failureOccurred() {
            this.failureMeter.mark();
        }

        public void completed() {
            final long elapsed = System.nanoTime() - this.startTimeNanos;
            this.timer.update(elapsed, TimeUnit.NANOSECONDS);
        }
    }

}
