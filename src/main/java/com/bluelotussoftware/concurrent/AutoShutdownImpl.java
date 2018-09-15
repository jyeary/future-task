package com.bluelotussoftware.concurrent;

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
import java.util.concurrent.Callable;

/**
 *
 * @author John Yeary <jyeary@bluelotussoftware.com>
 * @param <V>
 */
public class AutoShutdownImpl<V> implements AutoShutdown, Callable<AutoShutdown> {

    private volatile boolean execute = true;

    public AutoShutdownImpl start() {
        try {
            while (execute) {
                Thread.sleep(1000);
                System.out.println("Sleeping");
            }
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        return this;
    }

    @Override
    public void shutdown() {
        System.out.println("Running shutdown()");
        Runtime.getRuntime().exit(0);
    }

    @Override
    public AutoShutdownImpl call() throws Exception {
        return start();
    }

    public boolean isExecute() {
        return execute;
    }

    public void setExecute(boolean execute) {
        this.execute = execute;
    }

}
