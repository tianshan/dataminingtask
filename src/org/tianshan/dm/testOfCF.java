package org.tianshan.dm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.tianshan.dm.CollaborativeFilter.TestData;

public class testOfCF {
	
	public static CollaborativeFilter slopeone;
	private static final boolean isTest = true;
	// start predict thread
	private static int threadNum = 8;
	
	private static int useType = CollaborativeFilter.TYPE_SLOPEONEUPGRADE;
	
	public static void main(String[] args) {
		long start, stop;
		start  = System.currentTimeMillis();

		slopeone = new CollaborativeFilter("train.txt", isTest);
		slopeone.readData();
		// slopeone.analysMovie();
		// slopeone.checkData();
		
		stop  = System.currentTimeMillis();
		System.out.println((stop-start));
		
		start = System.currentTimeMillis();
		
		if (isTest)
			test();
		else 
			predict();
		
		stop  = System.currentTimeMillis();
		System.out.println((stop-start));
		
	}
	
//	static HashMap<String, ArrayList<TestData>> testData;
	
	public static int[][] predictData;
	public static String[] predictResult;
	
	public static void predict() {
		predictData = new int[250000][2];
		predictResult = new String[250000];
		
		// read
		try {
			BufferedReader reader = new BufferedReader(new FileReader("test.txt"));
			String line;
			int index=0;
			while ((line = reader.readLine())!=null) {
				String[] attr = line.split("\t");
				predictData[index][0] = Integer.parseInt(attr[0]);
				predictData[index][1] = Integer.parseInt(attr[1]);
				index++;
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		PredictThread[] predictThread = new PredictThread[threadNum];
		for (int k=0; k<threadNum; k++) {
			predictThread[k] = new PredictThread(k, threadNum);
			predictThread[k].start();
		}
		for (int k=0; k<threadNum; k++) {
			try {
				predictThread[k].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// write the result
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("test.rate"));
			for (String grade : predictResult) {
				writer.write(grade);
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static class PredictThread extends Thread {
		int k;
		int threadNum;
		public PredictThread(int k, int threadNum) {
			this.k = k;
			this.threadNum = threadNum;
		}
		public int getRound(double in) {
			double intPart = Math.floor(in);
			if (in - intPart >= 0.5) 
				in = intPart+1;
			else in = intPart;
			if (in>5) in = 5;
			else if (in < 1) in = 1;
			return (int)in;
		}
		public void run() {
			for (int i=0; i<predictData.length; i++) {
				if (i%threadNum != k) continue;
				if (i%1000==0) System.out.println(i);
				double predict = slopeone.predict(predictData[i][0], predictData[i][1], useType);
				predictResult[i] = String.valueOf( getRound(predict) );
			}
		}
	}
	
	public static void test() {
		System.out.println("total test : "+slopeone.getTestNum());
		
		double rmse = 0;
		double errorNum = 0;
		
		TestThread[] threads = new TestThread[threadNum];
		for (int k=0; k<threadNum; k++) {
			threads[k] = new TestThread(k, threadNum);
			threads[k].start();
		}
		for (int k=0; k<threadNum; k++) {
			try {
				threads[k].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		for (int k=0; k<threadNum; k++) {
			rmse += threads[k].getRmse();
			errorNum += threads[k].getErrorNum();
		}
		rmse = Math.sqrt(rmse / slopeone.getTestNum());
		System.out.println("total test : "+slopeone.getTestNum());
		System.out.println("test rmse : "+rmse);
		System.out.println("test errRate : "+errorNum/slopeone.getTestNum());
		
		for (int i=1; i<6; i++) {
			int num=0;
			for (int k=0; k<threadNum; k++) {
				num += threads[k].getErrorRate()[i];
			}
			System.out.println(i+" : "+num);
		}
		
		for (int i=0; i<2; i++) {
			int num=0;
			for (int k=0; k<threadNum; k++) {
				num += threads[k].getErrorNums()[i];
			}
			System.out.println("err "+i+" : "+num);
		}
	}
	
	static class TestThread extends Thread{
		int k;
		int threadNum;
		double rmse;
		int errorNum;
		int[] errorRate;
		
		int[] errorNums;
		
		public TestThread(int k, int threadNum) {
			this.k = k;
			this.threadNum = threadNum;
			rmse = 0;
			errorNum = 0;
			
			errorNums = new int[2];
			for (int i=0; i<2; i++) errorNums[i] = 0;
			
			errorRate = new int[6];
			for (int i=1; i<6; i++)
				errorRate[i] = 0;
		}
		
		public double getRmse() {
			return rmse;
		}
		
		public int getErrorNum() {
			return errorNum;
		}
		
		public int[] getErrorNums() {
			return errorNums;
		}
		
		public int[] getErrorRate() {
			return errorRate;
		}
		
		public int getRound(double in) {
			double intPart = Math.floor(in);
			if (in - intPart >= 0.5) 
				in = intPart+1;
			else in = intPart;
			if (in>5) in = 5;
			else if (in < 1) in = 1;
			return (int)in;
		}
		
		public void run() {
			
			TestData[] testData = slopeone.getTestData();
			for (int i=0; i<slopeone.getTestNum(); i++) {
				if (i%threadNum!=k) continue;
				if (i%1000==0) System.out.println(i);
				
				double predict1 = slopeone.predict(testData[i].userId, testData[i].movieId, CollaborativeFilter.TYPE_SLOPEONEUPGRADE);
				predict1 = getRound(predict1);
				
				double predict = predict1;
//				if (testData[i].grade == predict1
//						&& testData[i].grade != predict2) {
//					errorNums[0]++;
//				}
//				if (testData[i].grade != predict1
//						&& testData[i].grade == predict2) {
//					errorNums[1]++;
//				}
				
				if (testData[i].grade != predict) {
					errorNum++;
					errorRate[(int)Math.abs(predict - testData[i].grade)]++;
					rmse += (predict - testData[i].grade) * (predict - testData[i].grade);
					// System.out.println("result:"+testData[i].grade+" predict:"+predict+"\tpredict2:"+predict2);
				}
			}
			
		}
	}
	
}
