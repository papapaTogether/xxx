package com.zsf.xxx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author papapa
 *
 */
public class XvideosCrawler extends AbstractCrawler{

	private static final Logger log = LoggerFactory.getLogger(XvideosCrawler.class);

	private static final String BASE_URL = "https://www.xvideos.com";

	/**
	 * 获取视频高清地址
	 * 
	 * @param viewUrl
	 * @return
	 * @throws IOException
	 */
	@Override
	public String getVideoUrl(String viewUrl) {
		String videoUrl = null;
		Document doc;
		try {
			doc = HttpUtil.getDocument(viewUrl);
			if (doc == null) {
				return null;
			}
			Elements scripts = doc.getElementsByTag("script");
			if (scripts == null || scripts.size() == 0) {
				return null;
			}
			for (Element script : scripts) {
				String scriptStr = script.html();
				if (StringUtils.contains(scriptStr, "html5player.setVideoUrlHigh('")) {
					videoUrl = StringUtils.substringBetween(scriptStr, "html5player.setVideoUrlHigh('", "');");
					break;
				}
			}
		} catch (IOException e) {
			log.error("获取视频地址错误:[" + viewUrl + "]", e);
		}

		return videoUrl;
	}

	/**
	 * 获取该分类地址下的所有播放地址
	 * 
	 * @param href
	 * @return
	 * @throws IOException
	 */
	@Override
	public List<String> getViewUrls(String href) {
		List<String> viewUrls = new ArrayList<>();
		Document doc;
		try {
			doc = HttpUtil.getDocument(href);
			if (doc == null) {
				return null;
			}
			List<String> pageUrls = getPageUrls(doc);
			if (pageUrls != null) {
				viewUrls.addAll(pageUrls);
			}

			Element nextPageElement = doc.selectFirst("div.pagination > ul > li > a.next-page");
			if (nextPageElement != null) {
				String nextPageHref = BASE_URL + nextPageElement.attr("href");// 下一页地址
				if (nextPageHref.contains("/3") || nextPageHref.contains("p=3")) {// 为了测试只获取2页数据
					return viewUrls;
				}
				viewUrls.addAll(getViewUrls(nextPageHref));// 递规获取每一页中的播放地址
			}
		} catch (IOException e) {
			log.error("获取文档对象错误:[" + href + "]", e);
		}

		return viewUrls;
	}

	/**
	 * 获取当前页中每个的播放地址
	 * 
	 * @param doc
	 * @return
	 */
	public List<String> getPageUrls(Document doc) {
		//Elements links = doc.select("div#content > div.mozaique > div div.thumb > a");
		Elements insides = doc.select("div#content > div.mozaique > div div.thumb-inside");
		if(insides == null || insides.size() == 0){
			return null;
		}
		List<String> urls = new ArrayList<>();
		for(Element inside : insides){
			Element hdElement = inside.selectFirst("span.video-hd-mark");//获取高清(720p)视频
			if(hdElement != null){
				Element linkElement = inside.selectFirst("div.thumb > a");
				String link = linkElement.attr("href");
				urls.add(BASE_URL + link);
			}
		}
		return urls;
	}

	@Override
	public Map<String, String> getCategories() {
		Map<String,String> categories = new HashMap<>();
		categories.put("丝袜",BASE_URL+"/c/Stockings-28");
		categories.put("喷水",BASE_URL+"/c/Squirting-56");
		categories.put("女同",BASE_URL+"/?k=lesbian");

		return categories;
	}
}
