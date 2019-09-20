package com.zsf.xxx.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author papapa
 *
 */
@Slf4j
public class DownloadBatchRecursiveTask extends BatchRecursiveTask {

	private static final long serialVersionUID = -6577872267278165509L;

	private static final int RETRY_TIMES = 10;// 重试次数
	
	private static final int CONNECTION_TIMEOUT = 10 * 1000;//连接超时时间10秒
	
	private static final int READ_TIMEOUT = 10 * 1000;//读超时时间10秒

	protected DownloadBatchRecursiveTask(List<?> items, Object ext) {
		super(items, ext);
	}

	@Override
	public Object computeItem(Object item) {
		FileItemInfo itemInfo = (FileItemInfo) item;
		String filePath = itemInfo.getFilePath();
		File file = new File(filePath);
		long downloadLength = getDownloadLength(itemInfo);
		if (file.exists()) {
			if (file.length() == downloadLength) {
				log.info("分片[{}]已下载成功",itemInfo.getPartIndex());
				itemInfo.setDownloadSuccess(true);
				return itemInfo;
			}
		}
		downloadPart(itemInfo, RETRY_TIMES);
		return itemInfo;
	}

	private FileItemInfo downloadPart(FileItemInfo itemInfo, int retryTimes) {
		String filePath = itemInfo.getFilePath();
		int partIndex = itemInfo.getPartIndex();
		
		String url = itemInfo.getUrl();
		String range = getRange(itemInfo);
		InputStream input = null;
		OutputStream output = null;
		if(range == null){
			itemInfo.setDownloadSuccess(true);
			log.info("分片[{}]下存在，无需下载",itemInfo.getPartIndex());
			return itemInfo;
		}
		if (retryTimes == 0) {
			log.error("分片[{}]重试5次后依旧下载失败",partIndex);
			return null;
		}
		log.info("分片[{}],range:{},文件:{}",partIndex, range,filePath);
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setConnectTimeout(CONNECTION_TIMEOUT);
			connection.setReadTimeout(READ_TIMEOUT);

			HttpURLConnection.setFollowRedirects(true);
			connection.setRequestProperty("range", "bytes=" + range);
			connection.connect();
			// 获取响应吗
			int responseCode = connection.getResponseCode();
			if (responseCode != 206 && responseCode != 200) {
				return null;
			}
			input = connection.getInputStream();
			output = new FileOutputStream(filePath, true);
			IOUtils.copy(input, output, 1024);
			output.flush();
			connection.disconnect();
			itemInfo.setDownloadSuccess(true);
			log.info("分片[{}]下载成功",partIndex);
		} catch (Exception e) {
			if(!(e instanceof SocketTimeoutException) && !(e instanceof SSLException)){
				log.error("分片["+partIndex+"]下载失败:",e);				
			}
			retryTimes--;
			log.error("分片[{}]开始重试第[{}]次",partIndex,RETRY_TIMES - retryTimes);
			try {
				TimeUnit.SECONDS.sleep(1L);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			if(connection != null){
				connection.disconnect();
			}
			IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(output);
			
			downloadPart(itemInfo, retryTimes);
		}
		return itemInfo;
	}
	
	/**
	 * 要下载的长度
	 * @param itemInfo
	 * @return
	 */
	private long getDownloadLength(FileItemInfo itemInfo){
		long downloadLength = itemInfo.getEndIndex() - itemInfo.getStartIndex() + (itemInfo.isLastSharding() ? 0 : 1);
		return downloadLength;
	}
	
	/**
	 * 获取range   
	 * @param itemInfo
	 * @return 当返回null时表示不需要再重新下载
	 */
	private String getRange(FileItemInfo itemInfo){
		String filePath = itemInfo.getFilePath();
		File tempFile = new File(filePath);
		FileInfo fileInfo = itemInfo.getFileInfo();
		long contentLength = fileInfo.getContentLength();

		long tempContentLength = tempFile.exists() ? FileUtils.sizeOf(tempFile) : 0;
		log.info("临时文件[{}]大小{}Bytes",tempFile.getAbsolutePath(),tempContentLength);
		String range = itemInfo.getStartIndex() + "-";
		if(tempContentLength == 0){
			if (itemInfo.getEndIndex() != contentLength) {
				 range += itemInfo.getEndIndex();
			}
		}else{
			if (getDownloadLength(itemInfo) == tempContentLength) {
				range = null;
			}else{
				range = itemInfo.getStartIndex() + tempContentLength + "-"+ itemInfo.getEndIndex();
			}
		}
		return range;
	}
}
