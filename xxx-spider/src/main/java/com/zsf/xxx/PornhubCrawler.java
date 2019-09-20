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

import com.alibaba.fastjson.JSONPath;
import com.zsf.xxx.http.HttpViewer;

/**
 * @author papapa
 *
 */
public class PornhubCrawler extends AbstractCrawler {

	private static final Logger log = LoggerFactory.getLogger(PornhubCrawler.class);

	private static final String BASE_URL = "https://cn.pornhub.com";// 中文pornhub

	private static final String PARAMS = "hd=1";// 高清

	/**
	 * 下载视频
	 * 
	 * @param dir
	 * @param videoUrl
	 */
	@Override
	public boolean downloadVideo(String dir, String videoUrl) {
		return false;
	}

	/**
	 * 获取视频地址(720p mp4格式)
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
			doc = HttpViewer.getRandomInstance().getResponseDoc(viewUrl);
			if (doc == null) {
				return null;
			}
			String scriptStr = doc.selectFirst("div#player > script").toString();
			scriptStr = "{" + StringUtils.substringBetween(scriptStr, "{", "};") + "}";

			// 获取720p且为mp4格式的地址
			videoUrl = (String) JSONPath.eval(scriptStr, "$.mediaDefinitions[quality='720'][format='mp4'][0].videoUrl");
		} catch (IOException e) {
			log.error("获取视频地址错误:[" + videoUrl + "]", e);
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
			doc = HttpViewer.getRandomInstance().getResponseDoc(href);
			if (doc == null) {
				return null;
			}
			List<String> pageUrls = getPageUrls(doc);
			if (pageUrls != null) {
				viewUrls.addAll(pageUrls);
			}

			Element nextPageElement = doc.selectFirst("div.pagination3 > ul > li.page_next > a");
			if (nextPageElement != null) {
				String nextPageHref = BASE_URL + "/" + nextPageElement.attr("href");// 下一页地址
				if (nextPageHref.endsWith("page=2")) {// 为了测试只获取1页数据
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
		List<String> urls = null;
		Elements lis = doc.select("ul#videoCategory > li.js-pop");
		if (lis != null && lis.size() > 0) {
			urls = new ArrayList<>();
			for (Element li : lis) {
				urls.add(BASE_URL + "/view_video.php?viewkey=" + li.attr("_vkey"));
			}
		}
		return urls;
	}

	/**
	 * 获取分类集合
	 * 
	 * @return
	 * @throws IOException
	 */
	@Override
	public Map<String, String> getCategories() {
		Map<String, String> result = null;
		Document doc;
		try {
			doc = HttpViewer.getRandomInstance().getResponseDoc(BASE_URL + "/categories");
			if (doc == null) {
				return null;
			}
			result = new HashMap<>();
			Elements lis = doc.select("ul#categoriesListSection > li");
			for (Element li : lis) {
				String href = BASE_URL + li.selectFirst("div.category-wrapper > a").attr("href");
				if (href.contains("?")) {// 只获取高清视频
					href += "&" + PARAMS;
				} else {
					href += "?" + PARAMS;
				}
				String title = li.selectFirst("div.category-wrapper > h5 > a").attr("data-mxptext");
				result.put(title, href);
				log.info("分类:{},地址:{}", title, href);
				if (result.size() > 2)
					break;// 只获取3个
			}
		} catch (IOException e) {
			log.error("获取分类错误:", e);
		}
		return result;
	}
}
