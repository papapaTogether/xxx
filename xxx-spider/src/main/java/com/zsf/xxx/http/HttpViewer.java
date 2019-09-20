package com.zsf.xxx.http;

import java.io.IOException;

import org.apache.commons.lang3.RandomUtils;
import org.jsoup.nodes.Document;

/**
 * @author papapa
 *
 */
public interface HttpViewer {

	/**
	 * 获取url响应后的html内容
	 * @param url
	 * @return
	 * @throws IOException
	 */
	String getResponesStr(String url) throws IOException;
	
	/**
	 * 获取url响应后的文档对像
	 * @param url
	 * @return
	 * @throws IOException
	 */
	Document getResponseDoc(String url) throws IOException;
	
	/**
	 * 获取httpViewer的一个随机实例
	 * @return
	 */
	public static HttpViewer getRandomInstance(){
		 HttpViewer[] VIEWERS = new HttpViewer[]{new CorsAnywhereViewer(),new HttpDebugerViewer(),new JsonpAfeldViewer()}; 
		int index = RandomUtils.nextInt(0, 2);
		return VIEWERS[index];
	}
}
