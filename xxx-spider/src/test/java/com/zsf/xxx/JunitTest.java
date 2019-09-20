package com.zsf.xxx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import com.zsf.xxx.http.HttpDebugerViewer;
import com.zsf.xxx.http.HttpViewer;
import com.zsf.xxx.http.JsonpAfeldViewer;
import com.zsf.xxx.util.HttpDownloader;

import lombok.extern.slf4j.Slf4j;

/**
 * @author papapa
 *
 */
@Slf4j
public class JunitTest {

	@Test
	public void testGetVideoUrl() throws IOException {
		String viewUrl = "https://cn.pornhub.com/view_video.php?viewkey=ph5d56b96c279c8";
		Document doc = HttpViewer.getRandomInstance().getResponseDoc(viewUrl);
		String scriptStr = doc.selectFirst("div#player > script").toString();
		scriptStr = "{" + StringUtils.substringBetween(scriptStr, "{", "};") + "}";

		// JSONObject json = JSON.parseObject(scriptStr,JSONObject.class);
		String videoUrl = (String) JSONPath.eval(scriptStr,
				"$.mediaDefinitions[quality='720'][format='mp4'][0].videoUrl");// 获取720p且为mp4格式的地址

		System.out.println(videoUrl);
	}

	public static void main(String[] args) throws IOException {
		//String url = "https://vid3-l3.xvideos-cdn.com/videos/mp4/2/1/3/xvideos.com_213d9cdeb355e88c4ac217af62911445.mp4?e=1568258415&ri=1024&rs=85&h=b6b50269fb96d71fb8577e0ac76ca7bb";
		String url = "https://vid1-l3.xvideos-cdn.com/videos/mp4/9/7/0/xvideos.com_9707facedd27ab26bfed74db83328504.mp4?e=1568822783&ri=1024&rs=85&h=42d19ed678f5cf1fa3be0594e63a54d1";
		//String url = "https://ardownload2.adobe.com/pub/adobe/reader/mac/AcrobatDC/1901220034/AcroRdrDC_1901220034_MUI.dmg";
		String filePath = "/Users/zsf/git/xvidoes/mp4/fontawesome-free-5.9.0-web.zip";
		long startTime = System.currentTimeMillis();
		new HttpDownloader().download(url, filePath);
		log.info("共耗时:{}秒",System.currentTimeMillis() - startTime);
	}
	
	@Test
	public void test2() throws IOException{
		String url = "https://pcs.baidu.com/rest/2.0/pcs/file?method=download&path=%2F%E7%94%B5%E5%BD%B1%2F%E4%BE%8F%E7%BD%97%E7%BA%AA%E5%85%AC%E5%9B%AD%E4%B8%89%E9%83%A8%E6%9B%B2%2FJurassic.Park.1.1993.mkv&random=0.7139769215592586&app_id=498065";
		String filePath = "/Users/zsf/git/xvidoes/mp4/test.mkv";
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setConnectTimeout(6 * 1000);// 超时6秒重连
		connection.setReadTimeout(6 * 1000);
		
		HttpURLConnection.setFollowRedirects(true);
		connection.setRequestProperty("range", "bytes=10-");
		connection.connect();
		// 获取响应吗
		int responseCode = connection.getResponseCode();
		if (responseCode != 206 && responseCode != 200) {
			return;
		}
		InputStream input = connection.getInputStream();
		IOUtils.copy(input, new FileOutputStream(new File(filePath)),1024);
		connection.disconnect();
	}
	
	@Test
	public void testHttpDebugerViewer() throws IOException{
		Document doc = new HttpDebugerViewer().getResponseDoc("https://www.xvideos.com/c/Squirting-56?quality=hd");
		Elements insides = doc.select("div#content > div.mozaique > div div.thumb-inside");
		log.info(insides.html());
	}
	
	@Test
	public void testJsonpAfeldViewer() throws IOException{
		Document doc = new JsonpAfeldViewer().getResponseDoc("https://www.xvideos.com/c/Squirting-56?quality=hd");
		Elements insides = doc.select("div#content > div.mozaique > div div.thumb-inside");
		log.info(insides.html());
	}
	
	@Test
	public void testTika() throws FileNotFoundException {
		InputStream input = new FileInputStream(new File("/Users/zsf/Desktop/2019年秋学籍模板.xls"));
		Map<String,String> metaData = getMetaData(input);
		log.info(JSON.toJSONString(metaData, true));
	}
	
	private Map<String,String> getMetaData(InputStream input){
		Map<String,String> metadataMap = null;
		BodyContentHandler handler = new BodyContentHandler();
		Metadata metadata = new Metadata();
		AutoDetectParser parser = new AutoDetectParser();
		try {
			parser.parse(new FileInputStream(new File("/Users/zsf/Desktop/2019年秋学籍模板.xls")), handler, metadata);
			metadata.add("downloadUrl", "https://www.baidu.com");
			metadataMap = new HashMap<>();
			String[] metadataNames = metadata.names();
			for(String metaName : metadataNames){
				metadataMap.put(metaName,metadata.get(metaName));
			}
		} catch (IOException | SAXException | TikaException e) {
			e.printStackTrace();
		}
		return metadataMap;
	}
}
