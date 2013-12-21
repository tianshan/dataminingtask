package org.tianshan.dm;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class DataSet {
	
	/** data path */
	private String path;
	/** wheather extract sample from the train */
	public boolean isTest;
	
	/** extract the rate of all sample as test */
	private final double testRate = 0.95;
	/** test num */
	public int testNum;
	private final int maxTestNum = 55000;
	/** test data */
	public Data[] testData;
	
	/** save the user to movie grade */
	public HashMap<Integer, HashMap<Integer, Integer>> userScoreMap;
	/** save the movie to user grade */
	public HashMap<Integer, MovieScore> movieScoreMap;
	
	/** the total train lines */
	public int numInstnaces;
	
	public DataPreDo pre;
	
	public Data[] trainData;
	
//	private int readType;
	public static final int Read_HashMap = 0;
	public static final int Read_Array = 1;
	
	public double allMean;
	
	class Data{
		int userId;
		int movieId;
		int grade;
	}
	
	class DataSort implements Comparator<Data> {

		@Override
		public int compare(Data o1, Data o2) {
			if (o1.userId < o2.userId) return -1;
			if (o1.userId > o2.userId) return 1;
			return 0;
		}

	}
	
	class MovieScore {
		private int[] score = new int[6];
		private int sum;
		private int num;
		private ArrayList<Integer>[] gradeUser = new ArrayList[6];
		
		public MovieScore() {
			for (int i=1; i<6; i++) {
				score[i] = 0;
				gradeUser[i] = new ArrayList<Integer>();
			}
			sum = 0;
			num = 0;
		}
		
		public void update(int grade) {
			score[grade]++;
			sum += grade;
			num ++;
		}
		
		public void putUser(int userId, int grade) {
			gradeUser[grade].add(userId);
			update(grade);
		}
		
		public double getMean() {
			return (double)sum / num;
		}
		
		public int getMaxGrade() {
			int maxNum=-1, maxGrade = -1;
			for (int i=1; i<6; i++) {
				if (score[i] > maxNum) {
					maxNum = score[i];
					maxGrade = i;
				}
			}
			return maxGrade;
		}
		
		public ArrayList<Integer> getGradeUser(int grade) {
			return gradeUser[grade];
		}
		
	}
	
	public DataSet(String path, boolean isTest) {
		this.path = path;
		this.isTest = isTest;
		// get the users array & movie array
		pre = new DataPreDo();
		// get the train lines
		numInstnaces = pre.getNuminstance();
		
	}
	
	protected void readData(int readType, boolean isBoth) {
		
		switch (readType){
		case Read_HashMap:
			userScoreMap = new HashMap<Integer, HashMap<Integer, Integer>>();
			movieScoreMap = new HashMap<Integer, MovieScore>();
			if (!isBoth) break;
		case Read_Array:
			trainData = new Data[numInstnaces];
			break;
		}
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			
			testData = new Data[maxTestNum];
			
			String line;
			testNum = 0;
			
			int index=0;
			
			HashMap<Integer, Integer> map;
			MovieScore movieScore;
			
//			Set<Integer> testKey = new HashSet<Integer>();
			
			while ((line = reader.readLine())!=null) {
				String[] attr = line.split("\t");
				int userId = Integer.parseInt(attr[0]);
				int movieId = Integer.parseInt(attr[1]);
				int grade = Integer.parseInt(attr[2]);
				
				userId = pre.getUserIndex(userId);
				movieId = pre.getMovieIndex(movieId);
				
				if (Math.random() > testRate && testNum < maxTestNum) {
//					if (testKey.contains(userId)) continue;
//					testKey.add(userId);
					
					testData[testNum] = new Data();
					testData[testNum].userId = userId;
					testData[testNum].movieId = movieId;
					testData[testNum].grade = grade;
					testNum ++;
					// if is test, skip the extract test set
					// if not, keep it 
					if (isTest)
						continue;
				}
				allMean += grade;
				
				switch (readType) {
				case Read_HashMap:
					// put user data into hashmap
					if (!userScoreMap.containsKey(userId))
						map = new HashMap<Integer, Integer>();
					else map = userScoreMap.get(userId);
					map.put(movieId, grade);
					userScoreMap.put(userId, map);
					
					// put movie data into hashmap
					if (!movieScoreMap.containsKey(movieId))
						movieScore = new MovieScore();
					else movieScore = movieScoreMap.get(movieId);
					movieScore.putUser(userId, grade);
					
					movieScoreMap.put(movieId, movieScore);
					if (!isBoth) break;
				case Read_Array:
					trainData[index] = new Data();
					trainData[index].userId = userId;
					trainData[index].movieId = movieId;
					trainData[index].grade = grade;
					break;
				}
				index++;
			}
			numInstnaces = index;
			allMean /= numInstnaces;
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Data[] getTestData(){
		return testData;
	}
	
	public int getTestNum() {
		return testNum;
	}
	
	public void sortTrainData() {
		DataSort dataSort = new DataSort();
		Arrays.sort(trainData, 0, numInstnaces, dataSort);
	}
}
