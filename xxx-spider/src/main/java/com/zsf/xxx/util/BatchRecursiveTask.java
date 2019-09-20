package com.zsf.xxx.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.RecursiveTask;

/**
 * @author papapa
 *
 */
@SuppressWarnings("rawtypes")
public abstract class BatchRecursiveTask extends RecursiveTask<List>{

	private static final long serialVersionUID = 2119394771132854398L;
	
	private List items;
	
	private Object ext;
	
	protected BatchRecursiveTask(List items,Object ext){
		this.items = items;
		this.ext = ext;
	}
	
	public Object getExt(){
		return this.ext;
	}
	
	@SuppressWarnings({ "unchecked" })
	@Override
	protected List compute() {
		List values = new ArrayList<>();
		if(items != null && items.size() > 0){
			if(items.size() == 1){
				values.add(computeItem(items.get(0)));
			}else{
				BatchRecursiveTask[] tasks = new BatchRecursiveTask[items.size()];
				int index = 0;
				Constructor constructor = this.getClass().getDeclaredConstructors()[0];
				constructor.setAccessible(true);
				Type[] types = constructor.getGenericParameterTypes();//匿名内部内有多个参数
				Object[] initargs = new Object[types.length];
				for(Object item : items){
					try {
						initargs[types.length-1] = this.ext;
						initargs[types.length-2] = Arrays.asList(item);					
						tasks[index] = (BatchRecursiveTask) constructor.newInstance(initargs);
						index++;
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException  | SecurityException e) {
						e.printStackTrace();
					}
				}
				invokeAll(tasks);
				for(BatchRecursiveTask task : tasks){
					values.addAll(task.join());
				}
			}
		}
		return values;
	}
	public abstract Object computeItem(Object item);
}