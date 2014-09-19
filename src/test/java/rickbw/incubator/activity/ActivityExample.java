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

import rickbw.incubator.activity.Activity.Execution;
import rickbw.incubator.activity.Slf4JActivityListener.LogLevel;


public class ActivityExample implements Runnable {

    private static final ActivityListenerRegistry registry = ActivityListenerRegistry.shared();

    private static final ActivityId idOfProgram = ActivityId.forMethodName(
            ActivityExample.class,
            "run");
    private static final ActivityId idOfAmazingActivity = ActivityId.forMethodName(
            ActivityExample.class,
            "doSomethingAmazing");
    private static final ActivityId idOfSomewhatAmazingActivity = ActivityId.forMethodName(
            ActivityExample.class,
            "doSomethingElseAmazing");

    private final Activity programActivity = registry.createActivity(idOfProgram);
    private final Activity amazingActivity = registry.createActivity(idOfAmazingActivity);
    private final Activity otherAmazingActivity = registry.createActivity(idOfSomewhatAmazingActivity);


    public static void main(final String... args) {
        registry.add(idOfProgram, new Slf4JActivityListener(LogLevel.INFO, LogLevel.DEBUG, LogLevel.ERROR));
        registry.add(idOfAmazingActivity, new Slf4JActivityListener(LogLevel.INFO, LogLevel.WARN));
        registry.add(idOfSomewhatAmazingActivity, new Slf4JActivityListener());

        final ActivityExample example = new ActivityExample();
        example.programActivity.execute(example, "MyProgram");
    }

    @Override
    public void run() {
        try (Execution exec = this.amazingActivity.start()) {
            doSomethingAmazing();
        }

        try (Execution exec = this.otherAmazingActivity.start()) {
            doSomethingElseAmazing();
        }
    }

    @Override
    public String toString() {
        return "This example";
    }

    private void doSomethingAmazing() {
        System.out.println(this + " is amazing!");
    }

    private void doSomethingElseAmazing() {
        System.out.println(this + ", too, is amazing. Mostly.");
    }

}
