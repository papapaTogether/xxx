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
public class FileItemInfo implements Comparable<FileItemInfo> {

	private FileInfo fileInfo;

	private String url;// 下载地址

	private String filePath;// 文件名

	private int partIndex;// 分片位置

	private long startIndex;// 文件开始下载位置

	private long endIndex;// 文件结束位置
	
	private boolean downloadSuccess;//是否下载成功

	private boolean firstSharding;//是否首片
	
	private boolean lastSharding;//是否最后一片
	
	@Override
	public int compareTo(FileItemInfo o) {
		int partIndex1 = this.getPartIndex();
		int partIndex2 = o.getPartIndex();
		if (partIndex1 < partIndex2) {
			return -1;
		} else if (partIndex1 > partIndex2) {
			return 1;
		} else {
			return 0;
		}
	}
}
