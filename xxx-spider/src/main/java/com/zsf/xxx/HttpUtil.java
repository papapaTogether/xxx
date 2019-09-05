package com.zsf.xxx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * @author papapa
 *
 */
public class HttpUtil {

	private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);
	
	private static final String PROXY_URL = "https://cors-anywhere.herokuapp.com/"; 
	
	private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:68.0) Gecko/20100101 Firefox/68.0";
	
	private static Headers getDownloadHeaders(){
		Headers.Builder builder = new Headers.Builder();
		builder.add("x-requested-with","XMLHttpRequest");
		builder.add("User-Agent",USER_AGENT);
		builder.add("Connection", "keep-alive");
		builder.add("Content-Type", "application/octet-stream");
		return builder.build();
	}
	
	/**
	 * 获取url响应后的html内容
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static String getResponesStr(String url) throws IOException{
		log.info("获取页面:[{}]内容",url);
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
				.url(PROXY_URL+url)
				.header("x-requested-with","XMLHttpRequest")
				.header("User-Agent", USER_AGENT)
				.get()
				.build();
		Call call = client.newCall(request);
		okhttp3.Response response = call.execute();
		int code = response.code();
		if(code == 200){
			return response.body().string();
		}
		return null;
	}
	
	/**
	 * 获取url响应后的Document
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static Document getDocument(String url) throws IOException{
		String responseStr = getResponesStr(url);
		if(responseStr != null){
			return Jsoup.parse(responseStr);			
		}
		return null;
	}
	
	/**
	 * 下载文件
	 * @param url
	 * @param output
	 * @return
	 * @throws IOException
	 */
	public static boolean downloadFile(String url,OutputStream output) throws IOException{
		boolean flag = false;
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url(PROXY_URL+url).headers(getDownloadHeaders()).get().build();
		Call call = client.newCall(request);
		okhttp3.Response response = call.execute();
		int code = response.code();
		if(code == 200){
			InputStream in = response.body().byteStream();
			IOUtils.copyLarge(in,output);
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(output);
			flag = true;
		}
		return flag;
	}
	
	/**
	 * 直接下载文件，不经过代理
	 * @param url
	 * @param output
	 * @return
	 * @throws IOException
	 */
	public static boolean downloadDirect(String url,OutputStream output) throws IOException{
		boolean flag = false;
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url(url).get().build();
		Call call = client.newCall(request);
		okhttp3.Response response = call.execute();
		int code = response.code();
		if(code == 200){
			InputStream in = response.body().byteStream();
			IOUtils.copyLarge(in,output);
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(output);
			flag = true;
		}
		return flag;
	}
}
