package com.zsf.xxx.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import lombok.extern.slf4j.Slf4j;

/**
 * @author papapa
 *
 */
@Slf4j
public class HttpDownloader {
	
	private static final String TMP_PATH = FileUtils.getTempDirectoryPath();// 临时文件保存目录
	
	private static final int CONNECTION_TIMEOUT = 10 * 1000;//连接超时时间10秒
	
	private static final int READ_TIMEOUT = 10 * 1000;//读超时时间10秒
	
	
	public void download(String url, String filePath) throws IOException {
		FileInfo fileInfo = getFileInfo(url);
		if (fileInfo == null) {
			throw new IOException("获取文件信息失败:["+url+"]");
		}
		fileInfo.setFilePath(filePath);
		log.info("文件信息:{}",ToStringBuilder.reflectionToString(fileInfo, ToStringStyle.JSON_STYLE));
		
		File savedFile = new File(filePath);
		if (savedFile.exists()) {
			if (FileUtils.sizeOf(savedFile) == fileInfo.getContentLength()) {
				log.info("文件已下载完成:[{}]", filePath);
				return;
			}
			FileUtils.forceDelete(savedFile);
		}
		savedFile.createNewFile();
		if(fileInfo.isSuportSharding()){//支持分片下载
			long contentLength = fileInfo.getContentLength(); 
			int threadNum = Runtime.getRuntime().availableProcessors() * 2;
			if(contentLength <= threadNum){
				threadNum = 1;
			}
			multiThreadsDownload(fileInfo, threadNum);
		}else{
			singleDownload(fileInfo);
		}
	}
	
	/**
	 * 单线程下载
	 * @param fileInfo
	 */
	private void singleDownload(FileInfo fileInfo) {
		
	}

	/**
	 * 开始下载文件
	 * @param fileInfo
	 * @param threadNum
	 * @throws IOException
	 */
	private void multiThreadsDownload(FileInfo fileInfo, int threadNum) throws IOException {
		long contentLength = fileInfo.getContentLength();
		if(contentLength <= threadNum){//内容长度小于等于线程数时，只需要一个线程下载
			threadNum = 1;
		}
		List<FileItemInfo> fileItemInfos = new ArrayList<>();
		
		long partSize = contentLength / threadNum + 1;

		log.info("每片大小[{}]Bytes",partSize);
		for (int i = 0; i < threadNum; i++) {
			long startIndex = i * partSize;
			startIndex = i == 0 ? 0 : fileItemInfos.get(i-1).getEndIndex()+1L;
			
			long endIndex = startIndex + partSize -1 ;

			if (i == threadNum - 1) {
				endIndex = fileInfo.getContentLength();
			}
			log.info("分片[{}]区间:{}-{}，大小:{}",i+1,startIndex,endIndex,endIndex-startIndex+1);
			FileItemInfo fileItemInfo = new FileItemInfo();
			fileItemInfo.setPartIndex(i+1);
			fileItemInfo.setUrl(fileInfo.getUrl());
			fileItemInfo.setStartIndex(startIndex);
			fileItemInfo.setFilePath(getTmpFilePath(fileInfo, i+1));
			fileItemInfo.setEndIndex(endIndex);
			fileItemInfo.setFileInfo(fileInfo);
			fileItemInfo.setFirstSharding(i==0);
			fileItemInfo.setLastSharding(i == threadNum - 1);
			fileItemInfos.add(fileItemInfo);
		}

		@SuppressWarnings("unchecked")
		List<FileItemInfo> results = (List<FileItemInfo>) ParallelComputeUtil.compute(new DownloadBatchRecursiveTask(fileItemInfos, null));
		mergeFile(results);

	}

	/**
	 * 合并文件
	 * @param results
	 * @throws IOException
	 */
	private void mergeFile(List<FileItemInfo> results) throws IOException {
		if(results == null){
			log.info("下载失败");
			return;
		}
		Collections.sort(results);
		for(FileItemInfo itemInfo : results){
			if(!itemInfo.isDownloadSuccess()){
				log.error("分片[{}]下载失败，取消合并文件",itemInfo.getPartIndex());
				return;
			}
		}
		addPartIntoFile(results);
		String filePath = results.get(0).getFileInfo().getFilePath();
		log.info("文件[{}]下载完成,大小:{}Bytes",filePath,FileUtils.sizeOf(new File(filePath)));
		deleteTempFile(results);
	}

	/**
	 * 删除临时文件
	 * @param items
	 */
	private void deleteTempFile(List<FileItemInfo> items){
		for(FileItemInfo item : items){
			boolean flag = FileUtils.deleteQuietly(new File(item.getFilePath()));
			log.info("临时文件[{}]删除{}",item.getFilePath(),flag ? "成功" : "失败");
		}
	}
	/**
	 * 将分片下载的文件写入主文件
	 * @param itemInfo
	 * @throws IOException
	 */
	private void addPartIntoFile(List<FileItemInfo> itemInfos) throws IOException{
		String filePath = itemInfos.get(0).getFileInfo().getFilePath();
		RandomAccessFile file = new RandomAccessFile(new File(filePath), "rw");
		for(FileItemInfo itemInfo : itemInfos){
			file.seek(itemInfo.getStartIndex());
			File tempFile = new File(itemInfo.getFilePath());
			file.write(FileUtils.readFileToByteArray(tempFile));
		}
		file.close();
	}
	
	/**
	 * 获取文件信息
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private FileInfo getFileInfo(String url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setConnectTimeout(CONNECTION_TIMEOUT);
		connection.setReadTimeout(READ_TIMEOUT);
		HttpURLConnection.setFollowRedirects(true);
		connection.connect();
		// 获取响应吗
		int responseCode = connection.getResponseCode();
		log.info("[{}]响应码为:{}", url, responseCode);
		if (responseCode != 200) {
			return null;
		}
		boolean suportSharding = false;
		if("bytes".equals(connection.getHeaderField("Accept-Ranges"))){
			suportSharding = true;
		}

		String contentType = connection.getContentType();
		long contentLength = connection.getContentLengthLong();
		long lastModified = connection.getLastModified();
		connection.disconnect();
		
		FileInfo fileInfo = new FileInfo().setUrl(url).setContentLength(contentLength).setContentType(contentType)
				.setLastModified(lastModified).setSuportSharding(suportSharding);

		return fileInfo;
	}

	/**
	 * 获取临时文件路径
	 * 
	 * @param url
	 * @return
	 */
	private String getTmpFilePath(FileInfo fileInfo, int partIndex) {
		String contentType = fileInfo.getContentType();
		long contentLength = fileInfo.getContentLength();
		long lastModified = fileInfo.getLastModified();
		return TMP_PATH+DigestUtils.md5Hex(contentType + contentLength + lastModified) + ".tmp_" + partIndex;
		//return StringUtils.substringBeforeLast(filePath, File.separator)+File.separator+DigestUtils.md5Hex(url + filePath + contentType + contentLength + lastModified) + ".tmp_" + partIndex;
	}
}
