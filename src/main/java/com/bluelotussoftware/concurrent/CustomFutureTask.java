/*
 * Copyright 2018 John Yeary <jyeary@bluelotussoftware.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bluelotussoftware.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 *
 * @author John Yeary <jyeary@bluelotussoftware.com>
 * @param <V> The type of the object returned.
 */
public class CustomFutureTask<V> extends FutureTask<V> {

    private Runnable runnable;
    private Callable<V> callable;

    public CustomFutureTask(Runnable runnable, V result) {
        super(runnable, result);
        this.runnable = runnable;
    }

    public CustomFutureTask(Callable<V> callable) {
        super(callable);
        this.callable = callable;
    }

    @Override
    protected void done() {
        System.out.println("Running done()");
        if (runnable != null) {
            if (runnable instanceof AutoShutdown) {
                ((AutoShutdown) runnable).shutdown();
            }
        }

        if (callable != null) {
            System.out.println("Running callable done()");
            if (callable instanceof AutoShutdown) {
                ((AutoShutdown) callable).shutdown();
            }
        }

        // Set runnable to reduce footprint (GC). callable is set to null automatically after done() completes in FutureTask.
        runnable = null;
    }

}
