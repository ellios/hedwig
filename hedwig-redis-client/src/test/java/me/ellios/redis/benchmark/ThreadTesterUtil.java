/**
 *  Copyright 2010 Wallace Wadge
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

/*
 Copyright 2009 Wallace Wadge

This file is part of BoneCP.

BoneCP is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

BoneCP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with BoneCP.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.ellios.redis.benchmark;

import me.ellios.jedis.RedisClientFactory;
import me.ellios.jedis.RedisOp;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/** For unit testing only
 * @author wwadge
 *
 */

@SuppressWarnings("all")
public class ThreadTesterUtil implements Callable<Long>{

    private static RedisOp redisOp = RedisClientFactory.getRedisClient("test");

	/** A dummy query for HSQLDB. */
	public static final String TEST_QUERY = "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS";

	private CountDownLatch startSignal;
	private CountDownLatch doneSignal;
	private Random rand = new Random();
	private int workDelay;
	static AtomicInteger c = new AtomicInteger(0);

	public ThreadTesterUtil(CountDownLatch startSignal, CountDownLatch doneSignal, int workDelay) {
		this.startSignal = startSignal;
		this.doneSignal = doneSignal;
		this.workDelay = workDelay;

	}

	/** {@inheritDoc}
	 * @see java.util.concurrent.Callable#call()
	 */
	public Long call() throws Exception {
		long time = 0;
		try {
			this.startSignal.await();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

        if (this.workDelay > 0){
            Thread.sleep(this.workDelay);
        }
        long start = System.nanoTime();
        redisOp.get("hello");
        time = time + (System.nanoTime() - start);


		this.doneSignal.countDown();
		return time;
	}

}
