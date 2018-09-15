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

import java.net.URI;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author John Yeary <jyeary@bluelotussoftware.com>
 */
public class Main {

    public static void main(String[] args) {
        ExecutorService es = Executors.newSingleThreadExecutor();
        
        URI uri = URI.create(Main.class.getProtectionDomain().getCodeSource().getLocation().toExternalForm());
        System.out.println(uri);
        System.out.println(Paths.get(uri).getParent().toString());
        
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    es.shutdown();
                    es.awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    System.out.println("Interrupted in shutdown hook.");
                } finally {
                    if (!es.isTerminated()) {
                        System.out.println("Running tasks did not shut down gracefully. Service will shut down forcibly.");
                    }
                    if (!es.isShutdown()) {
                        System.out.println("Executor did not shutdown in the specified time gracefully. Shutting down Executor forcibly.");
                        es.shutdownNow();
                    }
                }
            }));
            AutoShutdownImpl service = new AutoShutdownImpl();
            
            System.out.println("Submitting CustomCallable");
            CustomCallable<AutoShutdown> cc = new CustomCallable(service);
            CustomFutureTask<AutoShutdown> wcft = new CustomFutureTask<>(cc);
            Future<AutoShutdown> fs = es.submit(wcft, service);
            System.out.println("Setting Service execute=false");
            service.setExecute(false);

            try {
                AutoShutdownImpl ret = (AutoShutdownImpl) fs.get();
                service.setExecute(true);
                System.out.println("Submit CustomFutureTask...");
                CustomFutureTask<AutoShutdown> cfts = new CustomFutureTask(service);
                Future<AutoShutdown> fts = es.submit(cfts, ret);
                System.out.println("Cancel CustomFutureTask...");
                fts.cancel(true);
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace(System.err);
            }

            System.out.println("Resubmitting service in a new CustomFutureTask");
            CustomFutureTask<AutoShutdown> cfts = new CustomFutureTask(service);
            Future<AutoShutdown> fts = es.submit(cfts, service);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                System.out.println("Interupted");
            }

            System.out.println("Calling Cancel on resubmitted CustomFutureTask");
            fts.cancel(true);
        } finally {
            es.shutdown();
        }
    }

}
