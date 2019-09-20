package com.zsf.xxx;

import java.io.File;
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
public abstract class AbstractCrawler implements Crawler{

	private static final Logger log = LoggerFactory.getLogger(AbstractCrawler.class);

	@Override
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

				String downloadFileName = dir + File.separatorChar + title + File.separatorChar + "download" + ".txt";
				FileUtils.writeLines(new File(downloadFileName),"UTF-8", videoUrls, false);
				log.info("[{}]视频链接保存在{}",title,downloadFileName);
				
				log.info("开始下载[{}]下的视频",title);
				downloadVideos(dir + File.separatorChar + title, videoUrls);
				log.info("结束下载[{}]下的视频",title);
			}
		}
	}

	public void downloadVideos(String dir,List<String> videoUrls){
		for(String videoUrl : videoUrls){
			downloadVideo(dir,videoUrl);				
		}
	}
	
	public List<String> getVideoUrls(List<String> viewUrls) {
		List<String> videoUrls = new ArrayList<>();
		if (viewUrls != null && viewUrls.size() > 0) {
			for (String viewUrl : viewUrls) {
				String videoUrl = getVideoUrl(viewUrl);
				if(StringUtils.isNotBlank(videoUrl)){
					videoUrls.add(videoUrl);
				}
			}
		}
		return videoUrls;
	}
}
