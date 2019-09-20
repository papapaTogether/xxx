package com.zsf.xxx.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * @author papapa
 *
 */
public class HttpDebugerViewer extends AbstractHttpViewer {

	private static final Logger log = LoggerFactory.getLogger(HttpDebugerViewer.class);

	private static final String PROXY_URL = "http://www.httpdebugger.com/tools/ViewHttpHeaders.aspx";

	@Override
	public Document getResponseDoc(String url) throws IOException {
		log.info("获取页面:[{}]内容", url);
		OkHttpClient client = super.getOkHttpClientBuilder().build();
		Request request = new Request.Builder().url(PROXY_URL).headers(Headers.of(getHeaders())).post(getRequestBody(url)).build();
		Call call = client.newCall(request);
		okhttp3.Response response = call.execute();
		int code = response.code();
		if (code == 200) {
			ResponseBody body = response.body();
			Document doc = Jsoup.parse(body.string());
			body.close();
			response.close();
			String html = doc.selectFirst("div#ResultData pre").html();
			html = StringEscapeUtils.unescapeHtml4(html);
			return Jsoup.parse(html);
		}
		return null;
	}

	private RequestBody getRequestBody(String url){
		FormBody.Builder builder = new FormBody.Builder();
        builder.add("UrlBox", url);
        builder.add("AgentList", "Google Chrome");
        builder.add("VersionsList", "HTTP/1.1");
        builder.add("MethodList", "GET");
        return builder.build();
	}
	
	private Map<String,String> getHeaders(){
		Map<String,String> headers = new HashMap<>();
		headers.put("Host", "www.httpdebugger.com");
		headers.put("Origin", "http://www.httpdebugger.com");
		headers.put("Pragma", "no-cache");
		headers.put("Referer", "http://www.httpdebugger.com/tools/ViewHttpHeaders.aspx");
		headers.put("Upgrade-Insecure-Requests", "1");
		return headers;
	}
}
