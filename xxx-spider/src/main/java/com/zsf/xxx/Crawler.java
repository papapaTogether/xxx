package com.zsf.xxx;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author papapa
 *
 */
public interface Crawler {

	/**
	 * 爬虫入口
	 * 
	 * @param dir
	 *            文件保存目录
	 * @throws IOException
	 */
	void execute(String dir) throws IOException;

	/**
	 * 获取分类集合
	 * 
	 * @return
	 */
	Map<String, String> getCategories();

	/**
	 * 获取视频地址
	 * 
	 * @param href
	 * @return
	 */
	List<String> getViewUrls(String href);

	/**
	 * 获取视频高清地址
	 * 
	 * @param viewUrl
	 * @return
	 * @throws IOException
	 */
	String getVideoUrl(String viewUrl);

}
