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


/**
 * An ActivityListener is the point of extensibility in the Activities
 * framework. In allows third parties to inject behaviors on activity start
 * and end, such as:
 * <ul>
 *  <li>Logging</li>
 *  <li>Performance measurements</li>
 *  <li>Transaction management</li>
 * </ul>
 *
 * A listener may be shared across multiple activities, and even a single
 * activity may span multiple threads. Nevertheless, the caller is responsible
 * for ensuring that a given listener won't be called concurrently for the
 * same activity.
 */
public interface ActivityListener<AC, EC> {

    /**
     * The given {@link Activity} has been initialized. If this listener
     * requires any expensive per-Activity initialization, it should do so
     * here, and return it. The result will be passed back to the other
     * methods.
     */
    AC onActivityInitialized(Activity activity);

    /**
     * A particular {@link Activity.Execution} has started. This method must
     * be the first called for that execution, and must only be called once.
     *
     * @param activityContext   The object previously returned from a call
     *                          to {@link #onActivityInitialized(Activity)}
     *
     * @return  Any per-Execution context that this listener may need. If no
     *          per-Execution context is needed, simply return the argument.
     */
    EC onExecutionStarted(AC activityContext);

    /**
     * A full or partial failure occurred along the way. This method may be
     * called zero or more times at any point in between
     * {@link #onExecutionStarted(Object)} and
     * {@link #onExecutionCompleted(Object)}.
     *
     * Since this method will be called in the context of a previous failure,
     * implementations should take care not to raise exceptions of their own.
     */
    void onExecutionFailure(EC activityContext, Throwable failure);

    /**
     * A particular {@link Activity.Execution} has completed. This method must
     * be called after {@link #onExecutionStarted(Object)} and after any calls
     * to {@link #onExecutionFailure(Object, Throwable)}, and must be called
     * only once for any given Execution.
     */
    void onExecutionCompleted(EC activityContext);

}
