package com.zsf.xxx.http;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

/**
 * @author papapa
 *
 */
public class JsonpAfeldViewer extends  AbstractHttpViewer {

	private static final Logger log = LoggerFactory.getLogger(JsonpAfeldViewer.class);

	private static final String PROXY_URL = "https://jsonp.afeld.me/?url=";

	@Override
	public Document getResponseDoc(String url) throws IOException {
		log.info("获取页面:[{}]内容", url);
		OkHttpClient client = super.getOkHttpClientBuilder().build();
		Request request = new Request.Builder().url(PROXY_URL + url).header("User-Agent", USER_AGENT).get().build();
		Call call = client.newCall(request);
		okhttp3.Response response = call.execute();
		int code = response.code();
		if (code == 200) {
			ResponseBody body = response.body();
			Document doc = Jsoup.parse(body.string());
			body.close();
			response.close();
			return doc; 
		}
		return null;
	}

}
