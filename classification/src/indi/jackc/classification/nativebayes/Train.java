package indi.jackc.classification.nativebayes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 对训练数据集进行训练
 * 
 * @author JackLab
 *         参考文献：http://blog.csdn.net/jameshadoop/article/details/35276083
 */
public class Train extends ReadARFF {

	// 计算在不同的分类结果条件下，每个属性的取值发生的概率
	private void countPorbability() {
		try {
			String src = "src/indi/jackc/relev/nativebayes/trainresult.arff";
			File file = new File(src);
			if (file.exists()) {
				file.delete();
				file.createNewFile();
			}
			FileOutputStream out = new FileOutputStream(file);
			// 用于记录原始数据集中属于各分类结果的频数
			HashMap<String, Integer> map = new HashMap<String, Integer>();

			// 分别计算各分类结果的概率，并写入文件保存
			for (String resultItem : cResult) {

				// 当前分类结果的统计个数。为避免0频，默认为1
				int resultCount = 1;

				Double probability = 0.0;

				for (int i = 0; i < dataList.size(); i++) {
					if (dataList.get(i).get(dataList.get(i).size() - 1)
							.equals(resultItem)) {
						resultCount++;
					}
				}
				map.put(resultItem, resultCount);
				probability = Double.valueOf(resultCount)
						/ Double.valueOf(dataList.size());

				// 写入文件
				StringBuffer sb = new StringBuffer();
				sb.append("@decision P(" + resultItem + ") {" + probability
						+ "}\n");
				out.write(sb.toString().getBytes("UTF-8"));
			}

			out.write("@data\n".getBytes("UTF-8"));

			// 遍历属性集合
			for (int i = 0; i < attrList.size(); i++) {

				// 遍历属性元素集合
				for (int j = 0; j < attrItemList.get(i).size(); j++) {

					// 遍历分类结果集合
					for (String resultItem : cResult) {

						int itemCount = 1;
						Double probability = 0.0;
						for (ArrayList<String> data : dataList) {

							// 若当前数据与当前循环的属性元素和分类结果相等，计数加1
							if (data.get(data.size() - 1).equals(resultItem)
									&& data.get(i).equals(
											attrItemList.get(i).get(j))) {
								itemCount++;
							}

						}
						probability = Double.valueOf(itemCount)
								/ Double.valueOf(map.get(resultItem));

						// 写入文件
						StringBuffer sb = new StringBuffer();
						sb.append("P(" + attrList.get(i) + "="
								+ attrItemList.get(i).get(j) + "|" + resultItem
								+ ")," + probability + "\n");
						out.write(sb.toString().getBytes("UTF-8"));
					}
				}
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Train train = new Train();
		train.readData(new File(
				"src/indi/jackc/relev/nativebayes/traindata.arff"));
		train.countPorbability();
	}
}
