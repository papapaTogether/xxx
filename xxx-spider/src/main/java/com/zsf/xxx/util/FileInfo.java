package com.zsf.xxx.util;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author papapa
 *
 */
@Getter
@Setter
@Accessors(chain = true)
public class FileInfo {

	private String url;//文件原始下载地址
	
	private String filePath;//文件保存路径
	
	private long contentLength;// 内容长度

	private String contentType;// 内容类型

	private long lastModified;// 最后修改时间
	
	private boolean suportSharding;//是否支持分片

}
