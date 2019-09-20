package com.zsf.xxx.util;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

/**
 * @author papapa 并发调用工具类
 */
public class ParallelComputeUtil {

	public static <V> V compute(RecursiveTask<V> recursiveTask) {
		ForkJoinPool forkJoinPool = new ForkJoinPool();
		V value = forkJoinPool.invoke(recursiveTask);
		forkJoinPool.shutdown();
		return value;
	}

	public static List<?> compute(BatchRecursiveTask recursiveTask) {
		ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() * 2);
		List<?> value = forkJoinPool.invoke(recursiveTask);
		forkJoinPool.shutdown();
		return value;
	}

	public static void execute(RecursiveAction recursiveAction) {
		ForkJoinPool forkJoinPool = new ForkJoinPool();
		forkJoinPool.execute(recursiveAction);
		forkJoinPool.shutdown();
	}

	public static void execute(BatchRecursiveAction recursiveAction) {
		ForkJoinPool forkJoinPool = new ForkJoinPool();
		forkJoinPool.execute(recursiveAction);
		forkJoinPool.shutdown();
	}
}
