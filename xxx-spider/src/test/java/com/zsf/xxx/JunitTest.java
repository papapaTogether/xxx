package com.zsf.xxx;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.alibaba.fastjson.JSONPath;

/**
 * @author papapa
 *
 */
public class JunitTest {

	@Test
	public void testGetVideoUrl() throws IOException {
		String viewUrl = "https://cn.pornhub.com/view_video.php?viewkey=ph5d56b96c279c8";
		Document doc = HttpUtil.getDocument(viewUrl);
		String scriptStr = doc.selectFirst("div#player > script").toString();
		scriptStr = "{"+StringUtils.substringBetween(scriptStr, "{","};")+"}";
		
		//JSONObject json = JSON.parseObject(scriptStr,JSONObject.class);
		String videoUrl = (String) JSONPath.eval(scriptStr, "$.mediaDefinitions[quality='720'][format='mp4'][0].videoUrl");//获取720p且为mp4格式的地址
		
		
		System.out.println(videoUrl);
	}
}
