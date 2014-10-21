package indi.jackc.relev.nativebayes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author JackLab
 *         参考文献：http://blog.csdn.net/jameshadoop/article/details/35276083
 */
public class NativeBayes extends ReadARFF {

	private HashMap<String, Double> cMap = new HashMap<String, Double>();// 存储训练集中分类结果的概率
	private HashMap<String, Double> attrItemMap = new HashMap<String, Double>();// 存储训练集中各属性元素的概率

	private static String ITEM_SPLIT = ",";// 元素分隔符
	private static final String regex = "@decision(.*?)[{](.*?)[}]";// 模式匹配字符串

	private void readTrainResult(File trainresult) {
		try {
			FileReader fReader = new FileReader(trainresult);
			BufferedReader bReader = new BufferedReader(fReader);
			String line;
			Pattern pattern = Pattern.compile(regex);
			while ((line = bReader.readLine()) != null) {
				if (line.trim().equals(null)) {
					continue;
				}

				// 读取各分类结果的概率
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					String resultName = matcher.group(1).trim();
					Double probabilityValue = Double.valueOf(matcher.group(2)
							.trim());
					cMap.put(resultName, probabilityValue);
				} else if (line.trim().startsWith("@data")) {

					// 循环读取每条data
					while ((line = bReader.readLine()) != null) {
						if (line.trim().equals(null)) {
							continue;
						}
						String[] temp = line.split(ITEM_SPLIT);
						String itemName = temp[0].trim();
						Double probabilityValue = Double
								.valueOf(temp[1].trim());
						attrItemMap.put(itemName, probabilityValue);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void calculateResult() {

		for (ArrayList<String> data : dataList) {

			Double sumProbability = 1.0;// 记录P(F1|C)P(F2|C)P(F3|C)...P(Fn|C)P(C)
			Double maxProbability = 0.0;// 记录各分类结果中最高sumProbability
			int index = -1;// 记录最高sumProbability所属的分类结果

			// 遍历分类结果集
			for (int i = 0; i < cResult.size(); i++) {

				// 遍历data中每个元素
				for (int j = 0; j < data.size(); j++) {
					String keyStr = "P(" + attrList.get(j) + "=" + data.get(j)
							+ "|" + cResult.get(i) + ")";
					// 计算P(F1|C)P(F2|C)P(F3|C)...P(Fn|C)
					sumProbability *= attrItemMap.get(keyStr);
				}

				// 计算P(F1|C)P(F2|C)P(F3|C)...P(Fn|C)*P(C)
				sumProbability *= cMap.get("P(" + cResult.get(i) + ")");
				if (sumProbability > maxProbability) {
					maxProbability = sumProbability;
					index = i;
				}

				// 重置，进入下一个分类结果的P(F1|C)P(F2|C)P(F3|C)...P(Fn|C)*P(C)的计算
				sumProbability = 1.0;
			}
			System.out.println(data.toString() + " 判断结果是：" + cResult.get(index)
					+ "\t参考的概率值为：" + maxProbability.toString().substring(0, 5));
		}
	}

	public static void main(String[] args) {
		NativeBayes nBayes = new NativeBayes();

		// 读取需要进行预测的data
		nBayes.readData(new File("src/indi/jackc/relev/nativebayes/data.arff"));

		// 读取训练结果集
		nBayes.readTrainResult(new File(
				"src/indi/jackc/relev/nativebayes/trainresult.arff"));

		// 计算并输出预测结果
		nBayes.calculateResult();
	}
}
