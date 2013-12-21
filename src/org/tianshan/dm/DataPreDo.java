package org.tianshan.dm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeSet;

public class DataPreDo {
	
	HashMap<Integer, Integer> movieIdMap = new HashMap<Integer, Integer>();
	HashMap<Integer, Integer> userIdMap = new HashMap<Integer, Integer>();
	int movieTotalNum;
	int userTotalNum;
	int[] idToMovie;
	int[] idToUser;

	int numInstnaces;
	
	public DataPreDo() {
		preDo(false);
	}
	
	public DataPreDo(boolean needTest) {
		preDo(needTest);
	}
	
	private void preDo(boolean needTest) {
		TreeSet<Integer> movieIds = new TreeSet<Integer>();
		TreeSet<Integer> userIds = new TreeSet<Integer>();
		numInstnaces = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader("train.txt"));
			String line;
			while ((line = reader.readLine())!=null) {
				String[] attr = line.split("\t");
				userIds.add(Integer.parseInt(attr[0]));
				movieIds.add(Integer.parseInt(attr[1]));
				numInstnaces++;
			}
			reader.close();
			
			if (needTest) {
				reader = new BufferedReader(new FileReader("test.txt"));
				while ((line = reader.readLine())!=null) {
					String[] attr = line.split("\t");
					userIds.add(Integer.parseInt(attr[0]));
					movieIds.add(Integer.parseInt(attr[1]));
					numInstnaces++;
				}
				reader.close();
			}
			
			idToMovie = new int[movieIds.size()];
			idToUser = new int[userIds.size()];
			
			int index = 0;
			for (int id : movieIds) {
				idToMovie[index] = id;
				movieIdMap.put(id, index);
				index++;
			}
			movieTotalNum = movieIds.size();
			
			index = 0;
			for (int id : userIds) {
				idToUser[index] = id;
				userIdMap.put(id, index);
				index++;
			}
			userTotalNum = userIds.size();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getMovieIndex(int movieId) {
		if (!movieIdMap.containsKey(movieId))
			return -1;
		return movieIdMap.get(movieId);
	}
	
	public int getMovieId(int index) {
		return idToMovie[index];
	}
	public int getMovieNum() {
		return movieTotalNum;
	}
	
	public int getUserIndex(int userId) {
		if (!userIdMap.containsKey(userId))
			return -1;
		return userIdMap.get(userId);
	}
	
	public int getUserId(int index) {
		return idToUser[index];
	}
	
	public int getUserNum() {
		return userTotalNum;
	}
	
	public int getNuminstance() {
		return numInstnaces;
	}
	
	public void testPrint() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("user"));
			for (int i=0; i<userTotalNum; i++) {
				writer.write(i+"\t"+idToUser[i]);
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
