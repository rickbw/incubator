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
public interface ActivityListener {

    /**
     * The given {@link Activity} has started. This method must be the first
     * called on any listener, and must only be called once.
     *
     * @return  an Activity-specific "context" object, which will be passed
     *          back to this listener in future calls pertaining to the same
     *          Activity. Use this object in preference to maintaining such
     *          state internally to the listener. It can be null.
     */
    Object onStarted(Activity activity);

    /**
     * A full or partial failure occurred along the way. This method may be
     * called zero or more times at any point in between
     * {@link #onStarted(Activity)} and {@link #onCompleted(Activity, Object)}.
     *
     * Since this method will be called in the context of a previous failure,
     * implementations should take care not to raise exceptions of their own.
     */
    void onFailure(Activity activity, Throwable failure, Object context);

    /**
     * The given {@link Activity} has completed. This method must be called
     * after {@link #onStarted(Activity)} and after any calls to
     * {@link #onFailure(Activity, Throwable, Object)}, and must be called
     * only once.
     */
    void onCompleted(Activity activity, Object context);

}
