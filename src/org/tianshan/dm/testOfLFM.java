package org.tianshan.dm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.tianshan.dm.DataSet.Data;

public class testOfLFM {
	
	private static  boolean isTest = true;
	private static int threadNum = 8;
	
	private static LatentFactorModel lfm;
	
	private static double[][] params = {
//		{100, 0.008, 0.004},
//		{100, 0.008, 0.012},
//		{100, 0.008, 0.019},
//		{100, 0.009, 0.004},
//		{100, 0.009, 0.012},
//		{100, 0.009, 0.019},
//		{100, 0.010, 0.004},
//		{100, 0.010, 0.012},
//		{100, 0.010, 0.019},
//		{100, 0.011, 0.004},
//		{100, 0.011, 0.012},
//		{100, 0.011, 0.019},
//		{100, 0.012, 0.004},
//		{100, 0.012, 0.012},
//		{100, 0.012, 0.019}
		{100, 0.015, 0.019}
			};
	
	public static int[][] predictData;
	public static double[] predictResult;
	
	public static final int modelTimes = 3;
	public static int models=params.length*modelTimes;
	
	
	public static void main(String[] args){
		
		long start, stop;
		start  = System.currentTimeMillis();
		
		lfm = new LatentFactorModel("train.txt", isTest);
		lfm.readData();
		
		predictInit();
		
		for (int i=0; i<params.length; i++) {
			for (int j=0; j<modelTimes; j++) {
				int factorNum = 100;
				int round = 500;
				double alpha = params[i][1];
				double lambda = params[i][2];
				System.out.printf("%d %.3f %.3f", factorNum, alpha, lambda);
				lfm.setParam(factorNum, round, alpha, lambda);
				lfm.learn();
				predict();
			}
			System.out.println();
		}
		lfm.saveModel("model");
		predictWrite();
		
		
		
//		for (int j=0; j<9; j++) {
//			for (int i=0; i<params.length; i++) {
////				System.out.println(params[i][0]+" "+params[i][1]+" "+params[i][2]);
////				lfm.setParam((int)params[i][0], (int)params[i][0], params[i][1], params[i][2]);
//				System.out.printf("%d %.3f %.3f", 100, 0.007+0.001*j, params[i][2]);
//				lfm.setParam(100, 100, 0.007+0.001*j, params[i][2]);
//				lfm.learn();
//			}
//			System.out.println();
//		}
		
//		stop  = System.currentTimeMillis();
//		System.out.println("time:"+(stop-start));
//		start  = System.currentTimeMillis();
		
//		if (isTest) {
//			test();
//		}else {
//			lfm.saveModel("model");
//			predict();
//		}
		stop  = System.currentTimeMillis();
		System.out.println("time:"+(stop-start));
	}
	
	public static void test() {
		Data[] testData = lfm.getTestData();
		
		System.out.println("test num : "+lfm.getTestNum());
		
		double rmse=0;
		int errNum=0;
		int[] errNums = new int[6];
		for (int i=0; i<6; i++) errNums[i] = 0;
		
		for (int i=0; i<lfm.getTestNum(); i++) {
			double predict = lfm.predict(testData[i].userId, testData[i].movieId);
			predict = getRound(predict);
			if (predict != testData[i].grade) {
				errNum++;
				// errNums[testData[i].grade]++;
				errNums[(int)Math.abs(predict-testData[i].grade)]++;
				rmse += (predict - testData[i].grade) * (predict - testData[i].grade);
				// System.out.println(testData[i].grade+"\t"+predict+"\t"+(predict - testData[i].grade));
			}
		}
		rmse = Math.sqrt(rmse / lfm.getTestNum());
		double errRate = (double)errNum / lfm.getTestNum();
		System.out.println("test rmse: "+rmse);
		System.out.println("test errRate: "+errRate);
		
		for (int i=1; i<6; i++) System.out.println(i+" : "+errNums[i]);
	}
	
	public static void predictInit() {
		predictData = new int[250000][2];
		predictResult = new double[250000];
		for (int i=0; i<predictResult.length; i++) {
			predictResult[i] = 0;
		}
		
		// read
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					"test.txt"));
			String line;
			int index = 0;
			while ((line = reader.readLine()) != null) {
				String[] attr = line.split("\t");
				predictData[index][0] = lfm.pre.getUserIndex(Integer
						.parseInt(attr[0]));
				predictData[index][1] = lfm.pre.getMovieIndex(Integer
						.parseInt(attr[1]));
				index++;
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void predictWrite() {
		// write the result
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					"test.rate"));
			for (double grade : predictResult) {
				String result = String.valueOf(getRound(grade / models));
				writer.write(result);
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void predict() {
		
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
//				if (i%1000==0) System.out.println(i);
				double predict = lfm.predict2(predictData[i][0], predictData[i][1]);
				predictResult[i] += predict;
			}
		}
	}
	
	public static int getRound(double in) {
		double intPart = Math.floor(in);
		if (in - intPart >= 0.5) 
			in = intPart+1;
		else in = intPart;
		if (in>5) in = 5;
		else if (in < 1) in = 1;
		return (int)in;
	}
}
