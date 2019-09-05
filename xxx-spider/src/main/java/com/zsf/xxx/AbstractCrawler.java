package com.zsf.xxx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author papapa
 *
 */
public abstract class AbstractCrawler {

	private static final Logger log = LoggerFactory.getLogger(AbstractCrawler.class);

	/**
	 * 爬虫入口
	 * 
	 * @param dir
	 *            文件保存目录
	 * @throws IOException
	 */
	public void execute(String dir) throws IOException {
		if (StringUtils.isBlank(dir)) {
			throw new IOException("文件保存目录为空");
		}
		Map<String, String> categories = getCategories();
		if (categories != null) {
			for (Map.Entry<String, String> entry : categories.entrySet()) {
				String title = entry.getKey();
				String href = entry.getValue();
				List<String> viewUrls = getViewUrls(href);
				log.info("获取所有[{}]播放地址成功:{}", title, viewUrls);
				List<String> videoUrls = getVideoUrls(viewUrls);
				log.info("获取所有[{}]视频地址成功:{}", title, videoUrls);

				String downloadFileName = dir + File.separatorChar + "/download_" + title + ".txt";
				FileUtils.writeLines(new File(downloadFileName), videoUrls, false);
			}
		}
	}

	/**
	 * 获取分类集合
	 * 
	 * @return
	 */
	abstract Map<String, String> getCategories();

	/**
	 * 获取视频地址
	 * 
	 * @param href
	 * @return
	 */
	abstract List<String> getViewUrls(String href);

	/**
	 * 获取视频高清地址
	 * 
	 * @param viewUrl
	 * @return
	 * @throws IOException
	 */
	abstract String getVideoUrl(String viewUrl);

	public List<String> getVideoUrls(List<String> viewUrls) {
		List<String> videoUrls = new ArrayList<>();
		if (viewUrls != null && viewUrls.size() > 0) {
			for (String viewUrl : viewUrls) {
				videoUrls.add(getVideoUrl(viewUrl));
			}
		}
		return videoUrls;
	}

	/**
	 * 下载视频
	 * 
	 * @param videoUrl
	 * @param dir
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void downloadVideo(String videoUrl, String dir) throws FileNotFoundException, IOException {
		String fileName = StringUtils.substringBefore(videoUrl, ".mp4?");
		fileName = StringUtils.substringAfterLast(fileName, "/");

		String filePath = dir + File.separatorChar + fileName + ".mp4";

		log.info("开始下载视频:{}。保存路径:{}", videoUrl, filePath);
		HttpUtil.downloadDirect(videoUrl, new FileOutputStream(new File(filePath)));
	}

}
