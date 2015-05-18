package me.ellios.hedwig.common.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Given a Future, retrieve the data without exceptions.
 *
 * @author George Cao(caozhangzhi@qiyi.com)
 * @since 2014-03-04 10
 */
public class FutureRunner {
    private static final Logger LOG = LoggerFactory.getLogger(FutureRunner.class);

    private FutureRunner() {
    }

    public static <R> R run(FutureTask<R> future) {
        if (!future.isDone()) {
            future.run();
        }
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("Cannot get resource from task " + future, e);
        }
    }

    public static <R> List<R> join(Collection<FutureTask<R>> futures) {
        if (CollectionUtils.isEmpty(futures)) {
            LOG.warn("Empty tasks, just return empty list as result.");
            return Collections.emptyList();
        }
        List<R> resources = new ArrayList<>(futures.size());
        for (FutureTask<R> task : futures) {
            resources.add(run(task));
        }
        return resources;
    }
}
