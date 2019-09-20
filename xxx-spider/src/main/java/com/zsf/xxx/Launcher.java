package com.zsf.xxx;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author papapa
 *
 */
public class Launcher {

	private static final Logger log = LoggerFactory.getLogger(Launcher.class);

	public static void main(String[] args) throws IOException {
		InputStream in = Launcher.class.getResourceAsStream("/LICENSE");
		if(in != null){
			List<String> lines = IOUtils.readLines(in,Charset.forName("UTF-8"));
			log.info(StringUtils.join(lines,"\r\n"));			
		}
		log.info("爬虫开始");
		String fileDir = FileUtils.getTempDirectoryPath();//默认文件保存目录
		if(args != null && args.length > 0){
			fileDir = args[0];
		}
		log.info("视频地址文件保存在:{}",fileDir);
		//new PornhubCrawler().execute(fileDir);
		new XvideosCrawler().execute(fileDir);
		log.info("爬虫停止");
	}
}
