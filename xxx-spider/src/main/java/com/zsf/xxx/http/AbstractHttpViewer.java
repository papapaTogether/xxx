package com.zsf.xxx.http;

import java.io.IOException;
import java.time.Duration;

import org.jsoup.nodes.Document;

import okhttp3.OkHttpClient;

/**
 * @author papapa
 *
 */
public abstract class AbstractHttpViewer implements HttpViewer {

	public static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36";
	
	@Override
	public String getResponesStr(String url) throws IOException {
		Document doc = getResponseDoc(url);
		if(doc != null){
			return doc.html();
		}
		return null;
	}
	
	public OkHttpClient.Builder getOkHttpClientBuilder(){
		OkHttpClient.Builder builder = new OkHttpClient.Builder()
				.followRedirects(true)
				.followSslRedirects(true)
				.retryOnConnectionFailure(true)
				.readTimeout(Duration.ofSeconds(10L))
				.connectTimeout(Duration.ofSeconds(10L))
				.readTimeout(Duration.ofSeconds(10L))
				.callTimeout(Duration.ofSeconds(10L));
		return builder;
	}
}
