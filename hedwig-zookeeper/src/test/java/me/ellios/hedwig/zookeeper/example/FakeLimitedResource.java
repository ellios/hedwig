/*
 * Copyright 2012 Netflix, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package me.ellios.hedwig.zookeeper.example;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simulates some external resource that can only be access by one process at a time
 */
public class FakeLimitedResource
{
    private final AtomicBoolean inUse = new AtomicBoolean(false);

    public void     use() throws InterruptedException
    {
        // in a real application this would be accessing/manipulating a shared resource

        if ( !inUse.compareAndSet(false, true) )
        {
            throw new IllegalStateException("Needs to be used by one client at a time");
        }

        try
        {
            System.out.println("====================================================");
            System.out.println("I am running. ");
            Thread.sleep((long) (30000 * Math.random()));
            System.out.println("I am run out");
            System.out.println("====================================================");
        }
        finally
        {
            inUse.set(false);
        }
    }
}
