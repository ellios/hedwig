package me.ellios.hedwig.benchmark;

import java.util.List;

/**
 * @author wangweiping
 *
 */
public interface ClientRunnable extends Runnable {
	public List<long[]> getResults();
}
