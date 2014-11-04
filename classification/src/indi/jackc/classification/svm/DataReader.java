package indi.jackc.classification.svm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class DataReader {

	public static String ITEM_SPIT = " ";//数据分隔符（数据与分类结果）
	public static String ITEM_SPIT2=":";//数据分隔符（数据与标号）

	public SVMData getData(File file, int lineNum) {
		double x[][] = new double[lineNum][];
		int y[] = new int[lineNum];
		try {
			FileReader fr = new FileReader(file);
			BufferedReader bf = new BufferedReader(fr);
			String line;

			int i = 0;
			while ((line = bf.readLine().trim()) != null && i++ < lineNum) {
				String[] tempData = line.split(ITEM_SPIT);
				if(tempData[0].trim().equals("-1")){
					y[i-1]=-1;
				}else{
					y[i-1]=1;
				}
				
				//只取x的前10个值
				int count =10;
				x[i-1]=new double [count];
				for(int j=1;j<count;j++){
					//分开数据与数据的序列标号，如：“1:0.32”
					String [] temp=tempData[j].split(ITEM_SPIT2);
					
					x[i-1][j-1]=Double.parseDouble(temp[1].trim());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new SVMData(x,y);
	}

}
