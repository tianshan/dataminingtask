package org.tianshan.dm;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CollaborativeFilter {
	private String path;
	
	private int numInstnaces;
	
	// private HashMap<Integer, MovieScore> movie;
	
	private HashMap<Integer, HashMap<Integer, Integer>> userRelation;
	private HashMap<Integer, HashMap<Integer, Integer>> userScoreMap;
	private HashMap<Integer, MovieScore> movieScoreMap;
	// private HashMap<Integer, HashMap<Integer, DValue>> movieRelation;
	
	public static final int TYPE_PEARSON = 1;
	public static final int TYPE_SLOPEONE = 2;
	public static final int TYPE_SLOPEONE2 = 3;
	public static final int TYPE_SLOPEONEUPGRADE = 4;
	public static final int TYPE_COSINE = 5;
	public static final int TYPE_TANIMOTO = 6;
	
	class DValue {
		double sum;
		int num;
		double peasonSum;
		public DValue() {
			sum = 0;
			num = 0;
			peasonSum = 0;
		}
	}
	
	private boolean isTest;
	private final double testRate = 0.99;
	private int testNum;
	private final int maxTestNum = 15000;
	
	private DValue[][] movieRelation;
	private DataPreDo pre;
	
	private int movieNum;
	
	public int PredictType;
	
	public CollaborativeFilter(String path, boolean isTest) {
		this.path = path;
		this.isTest = isTest;
		numInstnaces = 0;
		userScoreMap = new HashMap<Integer, HashMap<Integer, Integer>>();
		movieScoreMap = new HashMap<Integer, MovieScore>();
		// movieRelation = new HashMap<Integer, HashMap<Integer, DValue>>();
		
	}
	
	class TestData{
		int userId;
		int movieId;
		int grade;
	}
	private TestData[] testData;
	
	public void readData() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			
			testData = new TestData[maxTestNum];
			
			String line;
			testNum = 0;
			
			HashMap<Integer, Integer> map;
			MovieScore movieScore;
			while ((line = reader.readLine())!=null) {
				String[] attr = line.split("\t");
				int userId = Integer.parseInt(attr[0]);
				int movieId = Integer.parseInt(attr[1]);
				int grade = Integer.parseInt(attr[2]);
				
				if (isTest) {
					if (Math.random() > testRate && testNum < maxTestNum) {
						testData[testNum] = new TestData();
						testData[testNum].userId = userId;
						testData[testNum].movieId = movieId;
						testData[testNum].grade = grade;
						testNum ++;
						continue;
					}
				}
				
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
				
				numInstnaces++;
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void checkData() {
		int movieSize = movieScoreMap.size();
		System.out.println(movieSize);
		for (int userId : userScoreMap.keySet()) {
			double size = userScoreMap.get(userId).size();
			if ( size/movieSize > 0.3)
				System.out.println(userId+" "+size);
		}
	}
	
	public void analysMovie() {
		
		pre = new DataPreDo();

		movieNum = pre.getMovieNum();
		movieRelation = new DValue[movieNum][movieNum];
		
		int xx=0;
		for (Map.Entry<Integer, MovieScore> movieEntry : movieScoreMap.entrySet()) {
			int movieId = movieEntry.getKey();
			System.out.println(xx++);
			
			MovieScore movieScore = movieEntry.getValue();
			
			for (int j=1; j<6; j++) {
				ArrayList<Integer> userIds = movieScore.getGradeUser(j);
				for (Integer userId : userIds) {
					HashMap<Integer, Integer> scoreMap = userScoreMap.get(userId);
					for (Map.Entry<Integer, Integer> entry : scoreMap.entrySet()) {
						int relateMovieId = entry.getKey();
						if (relateMovieId == movieId) continue;
						int u = pre.getMovieIndex(movieId);
						int v = pre.getMovieIndex(relateMovieId);
						if (movieRelation[u][v] == null) {
							movieRelation[u][v] = new DValue();
						}
						movieRelation[u][v].sum += j - entry.getValue();
						movieRelation[u][v].num ++;
					}
				}
			}
		}
	}
	
	public void analysUser() {
		userRelation = new HashMap<Integer, HashMap<Integer, Integer>>();
		/*
		 * HashMap<Integer, Integer> relationMap;
		for (int userId : userScoreMap.keySet()) {
			HashMap<Integer, Integer> map = userScoreMap.get(userId);
			relationMap = new HashMap<Integer, Integer>();
			
			// as to each movie
			for (Map.Entry<Integer, Integer> scoreEntry : map.entrySet()) {
				int movieId = scoreEntry.getKey();
				int grade = scoreEntry.getValue();
				// get the movie's has grade userid
				HashMap<Integer, Integer> movieMap = movieScoreMap.get(movieId);
				// as to each 
				for (Map.Entry<Integer, Integer> userEntry : movieMap.entrySet()) {
					// add the relation, if two user has the same grade to one movie
					int relateUserId = userEntry.getKey();
					int relateUserGrage = userEntry.getValue();
					int num;
					if (relateUserId != userId && relateUserGrage == grade) {
						if (relationMap.containsKey(relateUserId)) num = relationMap.get(relateUserId);
						else num = 0;
						relationMap.put(relateUserId, num+1);
					}
				}
			}
			
			userRelation.put(userId, relationMap);
		}*/
		for (Map.Entry<Integer, MovieScore> movieScoreEntry : movieScoreMap.entrySet()) {
			MovieScore movieScore = movieScoreEntry.getValue();
			for (int j=1; j<6; j++) {
				ArrayList<Integer> gradeUser = movieScore.getGradeUser(j);
				for (int p=0; p<gradeUser.size(); p++) {
					for (int q=p+1; q<gradeUser.size(); q++) {
						int userId = gradeUser.get(p);
						int relateUserId = gradeUser.get(q);
						//
						int num;
						if (!userRelation.containsKey(userId)) {
							HashMap<Integer, Integer> relationMap = new HashMap<Integer, Integer>();
							userRelation.put(userId, relationMap);
						}
						if (userRelation.get(userId).containsKey(relateUserId))
							num = userRelation.get(userId).get(relateUserId);
						else num = 0;
						userRelation.get(userId).put(relateUserId, num+1);
						// 
						if (!userRelation.containsKey(relateUserId)) {
							HashMap<Integer, Integer> relationMap = new HashMap<Integer, Integer>();
							userRelation.put(relateUserId, relationMap);
						}
						if (userRelation.get(relateUserId).containsKey(userId))
							num = userRelation.get(relateUserId).get(userId);
						else num = 0;
						userRelation.get(relateUserId).put(userId, num+1);
					}
				}
				
			}
		}
	}
	
	public double predict(int userId, int movieId, int type) {

		this.PredictType = type;
		// if a new user
		if (!userScoreMap.containsKey(userId)) {
			return movieScoreMap.get(movieId).getMaxGrade();
		}
		// if a new movie
		if (movieScoreMap.get(movieId) == null) {
			System.out.println("movieScoreMap null");
			return 4;
		}	
		// if already has grade
		if (userScoreMap.get(userId).containsKey(movieId)) {
			return userScoreMap.get(userId).get(movieId);
		}
		
		switch(type) {
		case TYPE_COSINE:
		case TYPE_PEARSON:
		case TYPE_TANIMOTO:
			return predictByRelation(userId, movieId, type);
		case TYPE_SLOPEONE:
			return predictBySlopeone(userId, movieId);
		case TYPE_SLOPEONE2:
			return predictBySlopeone2(userId, movieId);
		case TYPE_SLOPEONEUPGRADE :
			return predictBySlopeoneUpgrade(userId, movieId);
		default:
			return -1;
		}
	}
	
	private int predictBySlopeone2(int userId, int movieId) {
		
		int totalNum = 0;
		double result = 0;
		int index = pre.getMovieIndex(movieId);
		for (int i=0; i<movieNum; i++) {
			int relateMovieId = pre.getMovieId(i);
			if (!userScoreMap.get(userId).containsKey(relateMovieId))
				continue;
			DValue dValue = movieRelation[index][i];
			if (dValue == null) continue;
			
//			double grade = userScoreMap.get(userId).get(relateMovieId) + (double)dValue.sum/dValue.num;
//			result += grade * dValue.num;
//			totalNum += dValue.num;
			double grade = userScoreMap.get(userId).get(relateMovieId) + (double)dValue.sum/dValue.peasonSum;
			result += grade;
			totalNum ++;
		}
		result /= totalNum;
		
		double intPart = Math.floor(result);
		if (result-intPart >= 0.5) {
			result = intPart+1;
		}else result = intPart;
		
		if (result < 1) result = 1;
		else if (result > 5) result = 5;
		return (int)result;
	}
	
	private double predictBySlopeone(int userId, int movieId) {
		
		// cal movie relation map
		HashMap<Integer, DValue> relationMap = null;//= movieRelation.get(movieId);
		if (relationMap == null) {
			relationMap	= new HashMap<Integer, DValue>();
			MovieScore movieScore = movieScoreMap.get(movieId);
			// get the user already graded movieId
			Set<Integer> movieKey = userScoreMap.get(userId).keySet();
			
			for (int j=1; j<6; j++) {
				ArrayList<Integer> userIds = movieScore.getGradeUser(j);
				for (int id : userIds) {
					HashMap<Integer, Integer> scoreMap = userScoreMap.get(id);
					for (Map.Entry<Integer, Integer> entry : scoreMap.entrySet()) {
						// skip the to predict movieId
						if (entry.getKey().equals(movieId)) continue;
						// skip the movie the userId didn't grade
						if (!movieKey.contains(entry.getKey())) continue;
						
						DValue dValue = relationMap.get(entry.getKey());
						if (dValue == null) {
							dValue = new DValue();
						}
						dValue.sum += (j - entry.getValue());
						dValue.num ++;
						relationMap.put(entry.getKey(), dValue);
					}
				}
			}
		}
		
		return predictBySlopeone(userId, movieId, relationMap);
	}
	
	private double predictBySlopeone(int userId, int movieId, HashMap<Integer, DValue> relationMap) {
		// 
		int totalNum = 0;
		double result = 0;
		for (Map.Entry<Integer, DValue> entry : relationMap.entrySet()) {
			int relateMovieId = entry.getKey();
			// if without the relate movie
			if (!userScoreMap.get(userId).containsKey(relateMovieId)) continue;
			DValue dValue = entry.getValue();
			double grade = userScoreMap.get(userId).get(relateMovieId) + (double)dValue.sum/dValue.num;
			result += grade * dValue.num;
			totalNum += dValue.num;
		}
		result /= totalNum;
		
		return result;
	}
	
	private double predictByRelation(int userId, int movieId, int type) {
		HashMap<Integer, Double> relationMap = getUserRelation(userId, movieId, type);
		return predictByUserRelation(userId, movieId, relationMap);
	}
	
	private HashMap<Integer, Double> getUserRelation(int userId, int movieId, int type) {
		// cal relation map
		HashMap<Integer, Double> relationMap = new HashMap<Integer, Double>();
		HashMap<Integer, Integer> movie = userScoreMap.get(userId);
		
		for (int relateUserId : userScoreMap.keySet()) {
			if (relateUserId == userId)
				continue;
			// 
			if (!userScoreMap.get(relateUserId).containsKey(movieId))
				continue;
			HashMap<Integer, Integer> movieScore = userScoreMap.get(relateUserId);
			
			double coefficient = 0;
			
			switch (type) {
			case TYPE_PEARSON:
				ArrayList<Double> alu = new ArrayList<Double>();
				ArrayList<Double> alv = new ArrayList<Double>();
				double meanu = 0, meanv = 0;
				boolean firstFlag = true;
				for (int relateMovieId : movieScore.keySet()) {
					if (movie.containsKey(relateMovieId)) {
						if (firstFlag) {
							alu.add(movie.get(relateMovieId)-0.1);
							meanu += movie.get(relateMovieId)-0.1;
							alv.add(movieScore.get(relateMovieId)-0.1);
							meanv += movieScore.get(relateMovieId)-0.1;
							firstFlag = false;
							continue;
						}
						alu.add((double)movie.get(relateMovieId));
						meanu += movie.get(relateMovieId);
						alv.add((double)movieScore.get(relateMovieId));
						meanv += movieScore.get(relateMovieId);
					}
				}
				// if without same movie
				if (alu.size() == 0) continue;
				meanu /= alu.size();
				meanv /= alv.size();
				double elementUp = 0, elementDownu = 0, elementDownv = 0;
				for (int i=0; i<alu.size(); i++) {
					elementUp += ((double)alu.get(i) - meanu) * ((double)alv.get(i) - meanv);
					elementDownu += ((double)alu.get(i) - meanu) * ((double)alu.get(i) - meanu);
					elementDownv += ((double)alv.get(i) - meanv) * ((double)alv.get(i) - meanv);
				}
				if (elementUp == 0 || elementDownu == 0 || elementDownv == 0) continue;
				coefficient = elementUp / Math.sqrt(elementDownu * elementDownv);
				
				// 12-2 new add
				coefficient = coefficient * alu.size() / (alu.size() + 100);
				
				break;
			case TYPE_COSINE:
				int num=0;
				double up=0, downLeft=0, downRight=0;
				for (int relateMovieId : movieScore.keySet()) {
					if (!movie.containsKey(relateMovieId))  continue;
					
					up += movie.get(relateMovieId) * movieScore.get(relateMovieId);
					downLeft += movie.get(relateMovieId)*movie.get(relateMovieId);
					downRight += movieScore.get(relateMovieId)*movieScore.get(relateMovieId);
					num++;
				}
				// if without same movie
				if (num == 0) continue;
				coefficient = up / Math.sqrt(downLeft * downRight);
				break;
			case TYPE_TANIMOTO:
				int movieSize = movie.size();
				int relateMovieSize = movieScore.size();
				int sameSize=0;
				for (int relateMovieId : movieScore.keySet()) {
					if (movie.containsKey(relateMovieId)
							&& movie.get(relateMovieId) == movieScore
									.get(relateMovieId)) {
						sameSize ++;
					}
				}
				coefficient = (double)sameSize / (movieSize + relateMovieSize - sameSize);
				break;
			}
			
			relationMap.put(relateUserId, coefficient);
		}
		return relationMap;
	}
	
	private double predictByUserRelation(int userId, int movieId,
			HashMap<Integer, Double> relationMap) {
		//
		if (relationMap.size() == 0) {
			return movieScoreMap.get(movieId).getMaxGrade();
		}

		double result = 0;
		double totalCoefficient = 0;
		for (Map.Entry<Integer, Double> entry : relationMap.entrySet()) {
			int relateUserId = entry.getKey();
			
			switch (PredictType) {
			case TYPE_PEARSON:
				if (entry.getValue() <= 0) continue;
				result += userScoreMap.get(relateUserId).get(movieId) * entry.getValue();
				totalCoefficient += entry.getValue();
				break;
			case TYPE_COSINE:
			case TYPE_TANIMOTO:
				result += userScoreMap.get(relateUserId).get(movieId) * entry.getValue();
				totalCoefficient += entry.getValue();
				break;
			}
		}
			
		result /= totalCoefficient;

		return result;
	}
	
	// predict of upgrade slopne one
	private double predictBySlopeoneUpgrade(int userId, int movieId) {
		
		HashMap<Integer, Double> userRelationMap = getUserRelation(userId, movieId, CollaborativeFilter.TYPE_PEARSON);
		
		// cal movie relation map
		HashMap<Integer, DValue> relationMap = null;
		if (relationMap == null) {
			relationMap = new HashMap<Integer, DValue>();
			MovieScore movieScore = movieScoreMap.get(movieId);
			// get the userId already graded movieId
			Set<Integer> movieKey = userScoreMap.get(userId).keySet();

			for (int j = 1; j < 6; j++) {
				ArrayList<Integer> userIds = movieScore.getGradeUser(j);
				for (int relateUserId : userIds) {
					HashMap<Integer, Integer> scoreMap = userScoreMap.get(relateUserId);
					for (Map.Entry<Integer, Integer> entry : scoreMap
							.entrySet()) {
						
						int relateMovieId = entry.getKey();
						
						// skip the to predict movieId
						if (relateMovieId == movieId)
							continue;
						// skip the movie the userId didn't grade
						if (!movieKey.contains(relateMovieId))
							continue;

						DValue dValue = relationMap.get(relateMovieId);
						if (dValue == null) {
							dValue = new DValue();
						}
						
						if (!userRelationMap.containsKey(relateUserId))
							continue;
						dValue.sum += (j - entry.getValue()) * userRelationMap.get(relateUserId);
						dValue.peasonSum += Math.abs(userRelationMap.get(relateUserId));
						dValue.num ++;
						relationMap.put(entry.getKey(), dValue);
					}
				}
			}
		}

		if (relationMap.size() == 0)
			return movieScoreMap.get(movieId).getMaxGrade();
		return predictBySlopeoneUpgrde(userId, movieId, relationMap);
	}
	
	private double predictBySlopeoneUpgrde(int userId, int movieId,
			HashMap<Integer, DValue> relationMap) {
		//
		int totalNum = 0;
		double result = 0;
		for (Map.Entry<Integer, DValue> entry : relationMap.entrySet()) {
			int relateMovieId = entry.getKey();
			// if without the relate movie
			if (!userScoreMap.get(userId).containsKey(relateMovieId))
				continue;
			DValue dValue = entry.getValue();
			double grade = userScoreMap.get(userId).get(relateMovieId)
					+ (double) dValue.sum / dValue.peasonSum;
			// 1
			result += grade * dValue.num;
			totalNum += dValue.num;
			// 2 bad than 1
//			result += grade;
//			totalNum ++;
			// 3 bad one
//			result += grade * dValue.peasonSum / dValue.num;
//			totalNum += dValue.peasonSum / dValue.num;
		}
		result /= totalNum;
		return result;
	}
	
	public TestData[] getTestData() {
		return testData;
	}
	public int getTestNum() {
		return testNum;
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
}
