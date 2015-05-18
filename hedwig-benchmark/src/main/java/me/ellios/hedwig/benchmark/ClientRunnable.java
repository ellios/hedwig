/*
 * This is  a part of the Video Resource System(VRS).
 * Copyright (C) 2010-2012 iqiyi.com Corporation
 * All rights reserved.
 *
 * Licensed under the iqiyi.com private License.
 */
package me.ellios.hedwig.benchmark;

import java.util.List;

/**
 * @author wangweiping
 *
 */
public interface ClientRunnable extends Runnable {
	public List<long[]> getResults();
}
