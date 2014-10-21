package indi.jackc.cluster.kmeans;

import java.util.ArrayList;
import java.util.Random;

public class KMeans {

	// 构造函数，设置k的值；若k<=0，则k=1
	public KMeans(int k) {
		if (k <= 0) {
			k = 1;
		}
		this.k = k;
	}

	private int k;// 分成的簇数
	private int m;// 迭代的次数
	private int dataSetLength;// 数据集的长度
	private static ArrayList<float[]> dataSet;// 数据集集合
	private static ArrayList<float[]> center;// 簇中心点集合
	private static ArrayList<ArrayList<float[]>> cluster;// 簇
	private static ArrayList<Float> errorList;// 误差平方和
	private static Random random;

	// 初始化
	static {
		dataSet = new ArrayList<float[]>();

		dataSet.add(new float[] { 1, 2 });
		dataSet.add(new float[] { 3, 3 });
		dataSet.add(new float[] { 3, 4 });
		dataSet.add(new float[] { 5, 6 });
		dataSet.add(new float[] { 8, 9 });
		dataSet.add(new float[] { 4, 5 });
		dataSet.add(new float[] { 6, 4 });
		dataSet.add(new float[] { 3, 9 });
		dataSet.add(new float[] { 5, 9 });
		dataSet.add(new float[] { 4, 2 });
		dataSet.add(new float[] { 1, 9 });
		dataSet.add(new float[] { 7, 8 });

		center = new ArrayList<float[]>();
		cluster = new ArrayList<ArrayList<float[]>>();
		errorList = new ArrayList<Float>();
		random = new Random();
	}

	// 初始化k个中心点
	private ArrayList<float[]> initCenters() {

		// 随机生成k个不同的随机整数作为中心点
		int[] randoms = new int[this.k];
		int count = 0;
		randoms[count++] = random.nextInt(this.dataSetLength);
		while (count < this.k) {
			int temp = random.nextInt(this.dataSetLength);
			boolean flag = true;
			for (int i = 0; i < count; i++) {
				if (temp == randoms[i]) {
					flag = false;
					break;
				}
			}
			if (flag) {
				randoms[count++] = temp;
			}
		}

		// 将随机抽取的k个中心点赋值给center
		for (int i = 0; i < this.k; i++) {
			center.add(dataSet.get(randoms[i]));
		}
		return center;
	}

	private ArrayList<ArrayList<float[]>> initCluster() {
		ArrayList<float[]> temp;
		for (int i = 0; i < this.k; i++) {
			temp = new ArrayList<float[]>();
			cluster.add(temp);
		}
		return cluster;
	}

	// 计算数据点与中心点的距离
	private float getDistance(float[] center, float[] data) {
		float x = center[0] - data[0];
		float y = center[1] - data[1];
		float distance = (float) Math.sqrt(x * x + y * y);
		return distance;
	}

	// 获取最近的中心点位置
	private int getMinDistanceLocation(float[] distance) {
		float min = distance[0];
		int location = 0;
		for (int i = 1; i < distance.length; i++) {
			if (distance[i] < min) {
				min = distance[i];
				location = i;
			} else if (distance[i] == min) {
				// 若相等，随机返回一个位置
				location = random.nextInt(6) < 3 ? i : location;
			}
		}
		return location;
	}

	private void doCluster() {
		float[] distance = new float[this.k];
		for (int i = 0; i < this.dataSetLength; i++) {
			for (int j = 0; j < this.k; j++) {
				distance[j] = getDistance(center.get(j), dataSet.get(i));
			}

			// 得到距离最近的中心点的位置
			int location = getMinDistanceLocation(distance);

			cluster.get(location).add(dataSet.get(i));
		}
	}

	// 获取簇内点距离簇中心误差的平方值
	private float getErrorSquare(float[] center, float[] clusterItem) {
		float x = center[0] - clusterItem[0];
		float y = center[1] - clusterItem[1];
		return x * x + y * y;
	}

	// 计算所有簇中簇内点距离簇中心误差的平方和
	private void sumOfError() {
		float errorSquare = 0;
		for (int i = 0; i < center.size(); i++) {
			for (float[] clusterItem : cluster.get(i)) {
				errorSquare += getErrorSquare(center.get(i), clusterItem);
			}
		}
		errorList.add(errorSquare);
	}

	private void setNewCenter() {
		for (int i = 0; i < this.k; i++) {
			float tempX = 0;
			float tempY = 0;
			int count = cluster.get(i).size();
			if (count > 0) {
				for (float[] tempItem : cluster.get(i)) {
					tempX += tempItem[0];
					tempY += tempItem[1];
				}
				center.set(i, new float[] { tempX / count, tempY / count });
			}
		}
	}

	// K-Means 算法
	private void kMeans() {
		dataSetLength = dataSet.size();

		// 若k大于数据集长度，置为数据集长度
		if (k > dataSetLength) {
			k = dataSetLength;
		}

		m = 0;
		center = initCenters();
		cluster = initCluster();

		// 进行（以“距中心点最近”为原则聚类-计算所有簇中簇内误差平方和-计算新的中心点）循环，直至误差平方和不变
		while (true) {

			// 以“距中心点最近”为原则进行聚类
			doCluster();

			// 计算所有簇中簇内点距离簇中心误差的平方和
			sumOfError();

			// 若误差不变，则迭代结束
			if (m != 0) {
				if (errorList.get(m) - errorList.get(m - 1) == 0) {
					break;
				}
			}

			// 计算新的中心点
			setNewCenter();

			m++;
			cluster.clear();
			cluster = initCluster();
		}
	}

	private void printCluster() {
		System.out.println("k: " + k);
		System.out.println("m: " + m);
		int i = 0;
		for (ArrayList<float[]> clusterItem : cluster) {
			System.out.print("center["+i+"]:"+" ("+center.get(i)[0]+","+center.get(i)[1]+") ");
			System.out.print("cluster[" + i++ + "]: ");
			if (clusterItem != null && clusterItem.size() > 0) {
				System.out.print("{ ");
				for (float[] dataItem : clusterItem) {
					if (dataItem == clusterItem.get(clusterItem.size() - 1)) {
						System.out.print("(" + dataItem[0] + "," + dataItem[1]
								+ ") ");
					} else {
						System.out.print("(" + dataItem[0] + "," + dataItem[1]
								+ "), ");
					}
				}
				System.out.println("}");
			}
		}
	}

	public static void main(String[] args) {
		KMeans k = new KMeans(3);
		long startTime = System.currentTimeMillis();
		System.out.println("k-means begins!");
		k.kMeans();
		long endTime = System.currentTimeMillis();
		System.out.println("k-means runs " + (endTime - startTime) + "ms");
		System.out.println("k-means end! ");
		k.printCluster();
	}

}
