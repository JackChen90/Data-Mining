package indi.jackc.classification.svm;

import java.io.File;
import java.util.HashSet;

/**
 * 
 * @author JackLab 参考文献：http://blog.csdn.net/techq/article/details/6171688
 */
public class SimplifiedSMO {
	// 拉格朗日乘子
	private double[] a;

	// 表示边界上的点所对应的拉格朗日乘子的集合
	private HashSet<Integer> boundAlpha = new HashSet<Integer>();

	// 核函数结果（K(x1,x2)）
	private double[][] kernel;

	private double C = 0.8;// 惩罚因子
	private double tol = 0.01;// 容忍极限
	private double maxSteps = 5;// 拉格朗日乘子没有变化时最大迭代步数
	private double b = 0.0;

	private double[][] x;// 数据数组
	private int[] y;// 数据结果数组

	// 线性核函数 K=xTx
	private double k(int i, int j) {
		double sum = 0.0;
		for (int k = 0; k < this.x[i].length; k++) {
			sum += this.x[i][k] * this.x[j][k];
		}
		return sum;
	}

	// 初始化核函数结果
	private void initKernel(int length) {
		this.kernel = new double[length][length];
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < length; j++) {
				this.kernel[i][j] = k(i, j);
			}
		}
	}

	// 初始化拉格朗日乘子alpha
	private void initA(int length) {
		this.a = new double[length];
		for (int i = 0; i < length; i++) {
			this.a[i] = 0.0;
		}
	}

	// u=∑ai*yi*K(xi,x)+b
	private double u(int i) {
		double sum = 0.0;
		for (int j = 0; j < this.x.length; j++) {
			sum += a[j] * y[j] * kernel[j][i];
		}
		return sum + this.b;
	}

	// 获取Ei，Ei=ui-yi，表示真实值与预测值之差
	private double getE(int i) {
		return u(i) - y[i];
	}

	private int findMax(double Ei) {
		double max = 0.0;
		int maxIndex = -1;
		for (int j = 0; j < this.x.length; j++) {
			double temp = Math.abs(getE(j) - Ei);
			if (temp > max) {
				max = temp;
				maxIndex = j;
			}
		}
		return maxIndex;
	}

	private SVMModel train(SVMData svmData) {
		this.x = svmData.getX();
		this.y = svmData.getY();

		// 初始化核函数结果
		initKernel(this.x.length);

		// 初始化a（值全赋为0）
		initA(this.x.length);

		int steps = 0;
		while (steps < maxSteps) {
			int curA = 0;// 记录被改变乘子的数量
			for (int i = 0; i < x.length; i++) {

				// Ei=ui-yi，表示真实值与预测值之差
				double Ei = getE(i);

				/*
				 * 选取违背KKT条件的ai作为第一个拉格朗日乘子（a1） 满足KKT条件的情况： yi*u(i) >= 1 and *
				 * alpha == 0 (正确分类) yi*u(i) == 1 and 0<alpha < C (在边界上的支持向量)
				 * yi*u(i) <= 1 and alpha == C (在边界之间)
				 */

				// y[i]*Ei=y[i]*ui-1（y[i]^2=1）
				if ((y[i] * Ei > tol && a[i] > 0)
						|| (y[i] * Ei < -tol && a[i] < C)) {

					// 取max|Ei-Ej|所对应的j
					int j = findMax(Ei);

					double Ej = getE(j);

					// 保存当前ai，aj
					double oldAi = a[i];
					double oldAj = a[j];

					// aj的约束条件。分为两种情况：y[i]与y[j]同号跟异号
					double L, H;
					if (y[i] != y[j]) {
						L = Math.max(0, a[j] - a[i]);
						H = Math.min(C, C - a[i] + a[j]);
					} else {
						L = Math.max(0, a[i] + a[j] - C);
						H = Math.min(C, a[i] + a[j]);
					}

					double eta = k(i, i) + k(j, j) - 2 * k(i, j);
					if (eta <= 0) {
						continue;
					}

					// 计算新的aj
					a[j] = a[j] + y[j] * (Ei - Ej) / eta;

					// 若a[j]满足KKT条件，将其加入boundAlpha
					if (a[j] < C && a[j] > 0) {
						this.boundAlpha.add(j);
					}

					// 根据a的约束条件，修正aj
					if (a[j] < L) {
						a[j] = L;
					} else if (a[j] > H) {
						a[j] = H;
					}

					if (Math.abs(a[j] - oldAj) < 1e-5) {
						continue;
					}

					// 计算新的ai
					a[i] = a[i] + y[i] * y[j] * (a[j] - oldAj);

					// 若a[i]满足KKT条件，将其加入boundAlpha
					if (a[i] < C && a[i] > 0) {
						this.boundAlpha.add(i);
					}

					// 计算b1、b2
					double b1 = b - Ei - y[i] * (a[i] - oldAi) * k(i, i) - y[j]
							* (a[j] - oldAj) * k(i, j);
					double b2 = b - Ej - y[i] * (a[i] - oldAi) * k(i, j) - y[j]
							* (a[j] - oldAj) * k(j, j);

					// 根据约束，更新b
					if (0 < a[i] && a[i] < C) {
						b = b1;
					} else if (0 < a[j] && a[j] < C) {
						b = b2;
					} else {
						b = (b1 + b2) / 2;
					}

					curA++;
				}
			}
			if (curA == 0) {
				steps++;
			} else {
				steps = 0;
			}
		}
		return new SVMModel(a, y, b);
	}

	private double predict(SVMModel svmModel, SVMData svmData) {
		double accuracy;// 预测正确率
		int count = 0;// 预测正确的个数
		int sum = svmData.getY().length;// 预测样本总数

		for (int i = 0; i < svmData.getY().length; i++) {
			int length = svmModel.getY().length;
			double sumTemp = 0.0;

			// u=∑ai*yi*K(xi,x)+b
			for (int j = 0; j < length; j++) {
				sumTemp += svmModel.getA()[j] * svmModel.getY()[j] * k(j, i);
			}
			sumTemp += svmModel.getB();

			// 判断u是否与y同号，若同号，预测正确
			if ((sumTemp > 0 && svmData.getY()[i] > 0)
					|| (sumTemp < 0 && svmData.getY()[i] < 0)) {
				count++;
			}
		}
		accuracy = (double) count / (double) sum;
		return accuracy;
	}

	public static void main(String[] args) {
		SimplifiedSMO simplifiedSmo = new SimplifiedSMO();
		DataReader dataReader = new DataReader();
		SVMData svmData = dataReader.getData(new File(
				"src/indi/jackc/classification/svm/data.txt"), 50);
		System.out.println("开始训练...");
		SVMModel svmModel = simplifiedSmo.train(svmData);
		System.out.println("训练结束\n");
		System.out.println("开始预测...");
		double accuracy = simplifiedSmo.predict(svmModel, svmData);
		System.out.print("正确率为：" + accuracy);
	}
}
