package indi.jackc.relev.nativebayes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author JackLab
 *         参考文献：http://blog.csdn.net/jameshadoop/article/details/35276083
 */
public class ReadARFF {

	public ArrayList<String> attrList = new ArrayList<String>();// 属性信息
	public ArrayList<ArrayList<String>> attrItemList = new ArrayList<ArrayList<String>>();// 每个属性的取值
	public ArrayList<ArrayList<String>> dataList = new ArrayList<ArrayList<String>>();// 原始数据集
	public ArrayList<String> cResult = new ArrayList<String>();// 分类结果，如：预测打喷嚏的建筑工人是否感冒的“是”和“否”

	private static String ITEM_SPLIT = ",";// 元素分隔符

	private static final String regex = "@attribute(.*?)[{](.*?)[}]";// 模式匹配字符串

	public void readData(File file) {
		try {
			FileReader fReader = new FileReader(file);
			BufferedReader bReader = new BufferedReader(fReader);
			String line;
			Pattern pattern = Pattern.compile(regex);
			while ((line = bReader.readLine()) != null) {

				// 获取分类结果，存入cResult
				if (line.trim().startsWith("@decision")) {
					line = bReader.readLine();
					if (line.equals(null) || line == "") {
						continue;
					}
					String[] attrArray = line.split(ITEM_SPLIT);
					for (int i = 0; i < attrArray.length; i++) {
						attrArray[i] = attrArray[i].trim();
					}
					Collections.addAll(cResult, attrArray);
				}

				// 获取属性、属性的取值，分别存入attrList、attrItemList
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					attrList.add(matcher.group(1).trim());
					String[] attrItemArray = matcher.group(2).split(ITEM_SPLIT);
					for (int i = 0; i < attrItemArray.length; i++) {
						attrItemArray[i] = attrItemArray[i].trim();
					}
					ArrayList<String> temp = new ArrayList<String>();
					Collections.addAll(temp, attrItemArray);
					attrItemList.add(temp);
				} else if (line.startsWith("@data")) {

					// 获取数据，存入dataList
					while ((line = bReader.readLine()) != null) {
						if (line.trim() == "") {
							continue;
						}
						String[] dataArray = line.split(ITEM_SPLIT);

						/*
						 * 错误，数组元素不会更改
						 */
						/*
						 * for (int i = 0; i < dataArray.length; i++) {
						 * dataArray[i].trim(); }
						 */

						for (int i = 0; i < dataArray.length; i++) {
							dataArray[i] = dataArray[i].trim();
						}
						ArrayList<String> temp = new ArrayList<String>(
								dataArray.length);
						Collections.addAll(temp, dataArray);
						dataList.add(temp);
					}
				} else {
					continue;
				}
			}
			bReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
