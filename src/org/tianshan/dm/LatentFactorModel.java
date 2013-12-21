package org.tianshan.dm;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class LatentFactorModel extends DataSet{
	
	
	private int factorNum = 100;
	private int N = 100;
	private double alpha = 0.008;
	private double lambda = 0.019;
	
	private int threadNum = 6;
	
	double[][] userFactor;
	double[][] movieFactor;
	
	double[] userBias;
	double[] movieBias;
	
	/** the temp of userFactor */
	double[][] z;
	/** the movie simility factor */
	double[][] y;
	/** the 1 / sqrt(movieNum) */
	double ru;
	
	public static final int MODEL_SVD = 0;
	public static final int MODEL_BIASSVD = 1;
	public static final int MODEL_SVDPP = 2;
	
	private double lastRmse;
	private double convRmse = 0.000001;
	
	private boolean isSameRand = false;
	
	public LatentFactorModel(String path, boolean isTest) {
		super(path, isTest);
	}
	
	public void setParam(int factorNum, int N, double alpha, double lambda) {
		this.factorNum = factorNum;
		this.N = N;
		this.alpha = alpha;
		this.lambda = lambda;
	}
	
	double[][] randUser;
	double[][] randMovie;
	double[][] randY;
	
	public void readData() {
		readData(DataSet.Read_HashMap, true);
		
		// all init()
		randUser = new double[pre.getUserNum()][factorNum];
		randMovie = new double[pre.getMovieNum()][factorNum];
		randY = new double[pre.getMovieNum()][factorNum];
		
		double base = Math.sqrt(factorNum);
		for (int f=0; f<factorNum; f++) {
			for (int u=0; u<pre.getUserNum(); u++) {
				randUser[u][f] = 0.1*Math.random() / base;
			}
			for (int m = 0; m < pre.getMovieNum(); m++) {
				randMovie[m][f] = 0.1*Math.random() / base;
				randY[m][f] = 0.1*Math.random() / base;
			}
		}
	}
	
	public void learn() {
		initFactor();
		learnTrain();
	}
	
	private void initFactor() {
		// svd
		userFactor = new double[pre.getUserNum()][factorNum];
		movieFactor = new double[pre.getMovieNum()][factorNum];
		// bias svd
		userBias = new double[pre.getUserNum()];
		movieBias = new double[pre.getMovieNum()];
		for (int f=0; f<factorNum; f++) {
			for (int u=0; u<pre.getUserNum(); u++)
				userBias[u] = 0;
			for (int m = 0; m < pre.getMovieNum(); m++)
				movieBias[m] = 0;
		}
		// svd++
		y = new double[pre.getMovieNum()][factorNum];
		z = new double[pre.getUserNum()][factorNum];
		
		// init the arrays
		if (isSameRand) {
			for (int f=0; f<factorNum; f++) {
				for (int u=0; u<pre.getUserNum(); u++) {
					// svd
					userFactor[u][f] = randUser[u][f];
				}
				for (int m = 0; m < pre.getMovieNum(); m++) {
					// svd
					movieFactor[m][f] = randMovie[m][f];
					// svd++
					y[m][f] = randY[m][f];
				}
			}
		}else {
			double base = Math.sqrt(factorNum);
			for (int f=0; f<factorNum; f++) {
				for (int u=0; u<pre.getUserNum(); u++) {
					// svd
					userFactor[u][f] = 0.1*Math.random() / base;
				}
				for (int m = 0; m < pre.getMovieNum(); m++) {
					// svd
					movieFactor[m][f] = 0.1*Math.random() / base;
					// svd++
					y[m][f] = 0.1*Math.random() / base;
				}
			}
		}
		
		// init the initial rms
		lastRmse = 10;
	}
	
	private void learnTrain() {
		// iteration for N times
		int testTime=0;
		double[] sum = new double[factorNum];
		
		while (N-->0 /*|| checkRmse() < convRmse*/) {
//			System.out.print(testTime++);
			testTime++;
			
			for (int userIndex : userScoreMap.keySet()) {
				// init
				for (int f=0; f<factorNum; f++) {
//					z[userIndex][f] = userFactor[userIndex][f];
					z[userIndex][f] = 0;
					sum[f] = 0;
				}
				
				HashMap<Integer, Integer> scoreMap = userScoreMap.get(userIndex);
				// init the param
				ru = 1 / Math.sqrt(1.0 * scoreMap.size());
				for (int movieIndex : scoreMap.keySet()) {
					for (int f=0; f<factorNum; f++) {
						z[userIndex][f] += y[movieIndex][f] * ru;
					}
				}
				
				for (int movieIndex : scoreMap.keySet()) {
					
					double pui = predict(userIndex, movieIndex);
					double eui = (double)scoreMap.get(movieIndex) - pui;
					
					// update the bias param
					userBias[userIndex] += alpha * (eui - lambda * userBias[userIndex]);
					movieBias[movieIndex] += alpha * (eui - lambda * movieBias[movieIndex]);
					
					// update the factor
					for (int f=0; f<factorNum; f++) {
						sum[f] += movieFactor[movieIndex][f] * eui * ru;
						
						double temp = userFactor[userIndex][f];
						
						userFactor[userIndex][f] += alpha * (movieFactor[movieIndex][f] * eui - lambda * userFactor[userIndex][f]);
						movieFactor[movieIndex][f] += alpha * ((z[userIndex][f]+temp) * eui - lambda * movieFactor[movieIndex][f]);
						
					}
					
				}
				for (int movieIndex : scoreMap.keySet()) {
					for (int f=0; f<factorNum; f++) {
						y[movieIndex][f] += alpha * (sum[f] - lambda * y[movieIndex][f]);
					}
				}
				
			}
			// damping the learning param
			alpha *= 0.9 + 0.1*Math.random();
//			alpha *= 0.9;
			
//			LearnThread[] threads = new LearnThread[threadNum];
//			for (int k=0; k<threadNum; k++) {
//				threads[k] = new LearnThread(k);
//				threads[k].start();
//			}
//			for (int k=0; k<threadNum; k++) {
//				try {
//					threads[k].join();
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
			
			// result check
			System.out.print(testTime+" ");
			double tempRmse = checkRmse2();
//			if (lastRmse - tempRmse <0 || lastRmse - tempRmse < convRmse)
			if (Math.abs(lastRmse - tempRmse) < convRmse) {
//				System.out.println(" "+testTime+"\t"+endRmse+" "+endErrRate);
//				System.out.println();
				break;
			}
			lastRmse = tempRmse;
		}
	}
	
//	class LearnThread extends Thread{
//		
//		double[] sum = new double[factorNum];
//		
//		int k;
//		
//		public LearnThread(int k) {
//			this.k = k;
//		}
//		
//		public void run() {
//			for (int userIndex : userScoreMap.keySet()) {
//				if (userIndex % threadNum != k) continue;
//				// init
//				double[] z = new double[factorNum];
//				for (int f=0; f<factorNum; f++) {
//					z[f] = userFactor[userIndex][f];
//					sum[f] = 0;
//				}
//				
//				HashMap<Integer, Integer> scoreMap = userScoreMap.get(userIndex);
//				for (int movieIndex : scoreMap.keySet()) {
//					for (int f=0; f<factorNum; f++) {
//						z[f] += y[movieIndex][f] * ru;
//					}
//				}
//				
//				for (int movieIndex : scoreMap.keySet()) {
//					
//					double pui = predict(userIndex, movieIndex);
//					double eui = (double)scoreMap.get(movieIndex) - pui;
//					
//					// update the bias param
//					userBias[userIndex] += alpha * (eui - lambda * userBias[userIndex]);
//					synchronized (movieBias) {
//						movieBias[movieIndex] += alpha * (eui - lambda * movieBias[movieIndex]);
//					}
//					
//					// update the factor
//					for (int f=0; f<factorNum; f++) {
//						sum[f] += movieFactor[movieIndex][f] * eui * ru;
//						
//						userFactor[userIndex][f] += alpha * (movieFactor[movieIndex][f] * eui - lambda * userFactor[userIndex][f]);
//						synchronized (movieFactor) {
//							movieFactor[movieIndex][f] += alpha * (z[f] * eui - lambda * movieFactor[movieIndex][f]);
//						}
//					}
//					
//				}
//				
//				synchronized (y) {
//					for (int movieIndex : scoreMap.keySet()) {
//						for (int f=0; f<factorNum; f++) {
//							y[movieIndex][f] = alpha * (sum[f] - lambda * y[movieIndex][f]);
//						}
//					}
//				}
//				
//			}
//		}
//	}
	
	public double getRound(double in) {
		double intPart = Math.floor(in);
		if (in - intPart >= 0.5) 
			in = intPart+1;
		else in = intPart;
		if (in>5) in = 5;
		else if (in < 1) in = 1;
		return in;
	}
	
	public double predict(int userIndex, int movieIndex) {
		// no such user
		if (userIndex == -1) {
			return movieScoreMap.get(movieIndex).getMaxGrade();
		}
		// no such movie
		if (movieIndex == -1) {
			return 4;
		}
		// if the user didn't grade any
		if (!userScoreMap.containsKey(userIndex)) {
			return movieScoreMap.get(movieIndex).getMaxGrade();
		}
		// if the user has grade the movie
//		if (userScoreMap.get(userIndex).containsKey(movieIndex)) {
//			// return userScoreMap.get(userIndex).get(movieIndex);
//		}
		
		double result = allMean + userBias[userIndex] + movieBias[movieIndex];
		
//		HashMap<Integer, Integer> scoreMap = userScoreMap.get(userIndex);
//		for (int f=0; f<factorNum; f++) {
//			z[userIndex][f] = userFactor[userIndex][f];
//		}
//		for (int movie : scoreMap.keySet()) {
//			for (int f=0; f<factorNum; f++) {
//				z[userIndex][f] += y[movie][f] * ru;
//			}
//		}
		
		for (int f=0; f<factorNum; f++) {
			result += (z[userIndex][f]+userFactor[userIndex][f]) * movieFactor[movieIndex][f];
		}
		// return getRound(result);
		return result;
	}
	
	public double predict2(int userIndex, int movieIndex) {
		// no such user
		if (userIndex == -1) {
			return movieScoreMap.get(movieIndex).getMaxGrade();
		}
		// no such movie
		if (movieIndex == -1) {
			return 4;
		}
		// if the user didn't grade any
		if (!userScoreMap.containsKey(userIndex)) {
			return movieScoreMap.get(movieIndex).getMaxGrade();
		}
		// if the user has grade the movie
		if (userScoreMap.get(userIndex).containsKey(movieIndex)) {
			return userScoreMap.get(userIndex).get(movieIndex);
		}
		
		double result = allMean + userBias[userIndex] + movieBias[movieIndex];
		
		HashMap<Integer, Integer> scoreMap = userScoreMap.get(userIndex);
		if (scoreMap == null) {
			System.out.println(userIndex+" "+pre.getUserNum());
			System.out.println(userBias[userIndex]);
		}
		
		for (int f=0; f<factorNum; f++) {
			result += (z[userIndex][f]+userFactor[userIndex][f]) * movieFactor[movieIndex][f];
		}
		return result;
	}
	
	private double checkRmse() {
		double rmse = 0;
		int errNum = 0;
		for (int i=0; i<numInstnaces; i++) {
			int userIndex = trainData[i].userId;
			int moveIndex = trainData[i].movieId;
			double predict = predict(userIndex, moveIndex);
			if (predict != trainData[i].grade) {
				errNum++;
				rmse += (predict-trainData[i].grade) * (predict-trainData[i].grade);
			}
		}
		rmse = Math.sqrt(rmse / errNum);
		return rmse;
	}
	
	double endRmse;
	double endErrRate;
	
	private double checkRmse2() {
		double rmse=0;
		int errNum=0;
		CheckThread[] threads = new CheckThread[threadNum];
		for (int k=0; k<threadNum; k++) {
			threads[k] = new CheckThread(k);
			threads[k].start();
		}
		for (int k=0; k<threadNum; k++) {
			try {
				threads[k].join();
				rmse += threads[k].rmse;
				errNum += threads[k].errNum;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		rmse = Math.sqrt(rmse / numInstnaces);
		double errRate = (double)errNum /numInstnaces;
		System.out.println(" "+rmse+" "+errRate);
		endRmse = rmse;
		endErrRate = errRate;
		return rmse;
	}
	
	class CheckThread extends Thread {
		int k;
		double rmse = 0;
		int errNum = 0;
		
		int type = 2;
		
		public CheckThread(int k) {
			this.k = k;
		}
		public void run() {
			switch(type) {
			case 0:
				for (int userIndex : userScoreMap.keySet()) {
					if (userIndex%threadNum != k) continue;
					
					HashMap<Integer, Integer> scoreMap = userScoreMap.get(userIndex);
					for (int movieIndex : scoreMap.keySet()) {
						double predict = predict(userIndex, movieIndex);
						int grade = scoreMap.get(movieIndex);
						if (predict != grade) {
							errNum++;
							rmse += (predict - grade) * (predict - grade);
						}
					}
				}
				break;
			case 1:
				for (int i=0; i<testNum; i++) {
					if (i%threadNum != k) continue;
					int userIndex = testData[i].userId;
					int movieIndex = testData[i].movieId;
					int grade = testData[i].grade;
					
					double predict = predict(userIndex, movieIndex);
					predict = getRound(predict);
					if (predict != grade) {
						errNum++;
						rmse += (predict - grade) * (predict - grade);
					}
				}
				break;
			case 2:
				for (int i=0; i<numInstnaces; i++) {
					if (i%threadNum != k) continue;
					int userIndex = trainData[i].userId;
					int movieIndex = trainData[i].movieId;
					int grade = trainData[i].grade;
					
					double predict = predict(userIndex, movieIndex);
					// predict = getRound(predict);
					rmse += (predict - grade) * (predict - grade);
					if (getRound(predict) != grade) {
						errNum++;
					}
				}
				break;
			}
			
		}
	}
	
	public void saveModel(String path) {
		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(path)));
			// save allmean
			dos.writeDouble(allMean);
			// save user bias
			for (int i=0; i<userBias.length; i++) {
				dos.writeDouble(userBias[i]);
			}
			// save movie bias
			for (int i=0; i<movieBias.length; i++) {
				dos.writeDouble(movieBias[i]);
			}
			// save user factor arrays
			for (int i=0; i<userFactor.length; i++) {
				for (int j=0; j<factorNum; j++) {
					dos.writeDouble(userFactor[i][j]);
				}
			}
			// save movie factor arrays
			for (int i=0; i<movieFactor.length; i++) {
				for (int j=0; j<factorNum; j++) {
					dos.writeDouble(movieFactor[i][j]);
				}
			}
			// save movie simility temp array
			for (int i=0; i<z.length; i++) {
				for (int j=0; j<factorNum; j++) {
					dos.writeDouble(z[i][j]);
				}
			}
			dos.flush();
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
