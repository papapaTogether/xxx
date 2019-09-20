package com.zsf.xxx.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

/**
 * @author papapa
 *
 */
@SuppressWarnings("rawtypes")
public abstract class BatchRecursiveAction extends RecursiveAction{

	private static final long serialVersionUID = -2909644333830555865L;
	
	private List items;
	
	private Object ext;
	
	protected BatchRecursiveAction(List items,Object ext){
		this.items = items;
		this.ext = ext;
	}
	
	@SuppressWarnings({ "unchecked" })
	@Override
	protected void compute() {
		if(items != null && items.size() > 0){
			if(items.size() == 1){
				computeItem(items.get(0));
			}else{
				List<BatchRecursiveAction> actions = new ArrayList<>();
				for(Object item : items){
					List subActions = new ArrayList<>();
					subActions.add(item);
					BatchRecursiveAction subBatchRecursiveAction = null;
					try {
						Constructor constructor = this.getClass().getDeclaredConstructors()[0];
						constructor.setAccessible(true);
						subBatchRecursiveAction = (BatchRecursiveAction) constructor.newInstance(subActions,this.ext);
						actions.add(subBatchRecursiveAction);
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException | SecurityException e) {
						e.printStackTrace();
					}
				}
				invokeAll(actions);
				for(BatchRecursiveAction action : actions){
					action.join();
				}
			}
		}
	}

	public abstract void computeItem(Object item);
}
