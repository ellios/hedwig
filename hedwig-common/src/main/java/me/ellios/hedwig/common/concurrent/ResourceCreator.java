package me.ellios.hedwig.common.concurrent;

import com.google.common.base.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.FutureTask;


/**
 * Create unique object.
 *
 * @author George Cao
 * @since 2014-03-04 10
 */
public class ResourceCreator<K, R> {
    private static final Logger LOG = LoggerFactory.getLogger(ResourceCreator.class);

    public List<R> create(final Collection<K> keys, final Function<K, R> func) {
        List<FutureTask<R>> futureTasks = newTask(keys, func);
        return FutureRunner.join(futureTasks);
    }

    public R create(final K key, Function<K, R> func) {
        FutureTask<R> future = newTask(key, func);
        return FutureRunner.run(future);
    }

    private final transient ConcurrentMap<K, FutureTask<R>> concurrentMap;

    public ResourceCreator(ConcurrentMap<K, FutureTask<R>> concurrentMap) {
        this.concurrentMap = concurrentMap;
    }

    private List<FutureTask<R>> newTask(final Collection<K> keys, final Function<K, R> func) {
        if (CollectionUtils.isEmpty(keys) || null == func) {
            LOG.warn("Invalid parameter keys {} or func {}", keys, func);
            return Collections.emptyList();
        }
        List<FutureTask<R>> tasks = new ArrayList<>(keys.size());
        for (final K key : keys) {
            FutureTask<R> task = newTask(key, func);
            if (null == task) {
                LOG.warn("Cannot create new task for key {} with function {}", key, func);
                continue;
            }
            tasks.add(task);
        }
        return tasks;
    }

    private FutureTask<R> newTask(final K key, final Function<K, R> func) {
        if (null == key || null == func) {
            LOG.warn("Invalid parameter key {} or func {}", key, func);
            return null;
        }
        FutureTask<R> future = concurrentMap.get(key);
        if (null == future) {
            FutureTask<R> newTask = new FutureTask<>(new Callable<R>() {
                @Override
                public R call() {
                    return func.apply(key);
                }
            });
            FutureTask<R> oldTask = concurrentMap.putIfAbsent(key, newTask);
            if (null != oldTask) {
                return oldTask;
            } else {
                return newTask;
            }
        } else {
            LOG.warn("Resource for key {} already exists, and its state is {}", key, future.isDone());
        }
        return future;
    }
}
