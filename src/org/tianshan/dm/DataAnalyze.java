package org.tianshan.dm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Comparator;

public class DataAnalyze {
	public static void main(String[] args) {
//		ana();
//		ana2();
//		anam(3249);
//		anam2(3287);
		anaUser();
	}
	static TreeMap<Integer, ArrayList<Integer>> mapx;
	static TreeMap<Integer, ArrayList<Integer>> mapy;
	static DataSet2 dataSetx;
	static DataSet2 dataSety;
	
	public static void anaUser() {
		dataSetx = new DataSet2("train.txt", false);
		dataSetx.readData(DataSet.Read_HashMap, false);
		dataSetx.readData2(DataSet.Read_HashMap, false);
		mapx = new TreeMap<Integer, ArrayList<Integer>>(
				new Comparator<Integer>(){
			@Override
			public int compare(Integer o1, Integer o2) {
				if (o1>o2)return -1;
				if (o1<o2)return 1;
				return 0;
			}
		});
		
		for (int id : dataSetx.userScoreMap.keySet()) {
			int size = dataSetx.userScoreMap.get(id).size();
			ArrayList<Integer> al = mapx.get(size);
			if (al == null) {
				al = new ArrayList<Integer>();
			}
			al.add(id);
			mapx.put(size, al);
		}
		//
		dataSety = new DataSet2("training_set.txt", false);
		dataSety.readData(DataSet.Read_HashMap, false);
		mapy = new TreeMap<Integer, ArrayList<Integer>>(
				new Comparator<Integer>(){
			@Override
			public int compare(Integer o1, Integer o2) {
				if (o1>o2)return -1;
				if (o1<o2)return 1;
				return 0;
			}
		});
		for (int id : dataSety.userScoreMap.keySet()) {
			int size = dataSety.userScoreMap.get(id).size();
			ArrayList<Integer> al = mapy.get(size);
			if (al == null) {
				al = new ArrayList<Integer>();
			}
			al.add(id);
			mapy.put(size, al);
		}
		
		//
		int N=1000;
		while (N-->0) {
			if (tempx.size()==0) printxFlag = false;
			if (tempx.size()==0) {
				Map.Entry<Integer, ArrayList<Integer>> entry = 
						mapx.pollFirstEntry();
				sizex = entry.getKey();
				tempx.addAll(entry.getValue());
				if (tempx.size()>1) printxFlag = true;
			}else{
				printxFlag = true;
			}
			if (tempy.size()==0) printyFlag = false;
			if (tempy.size()==0) {
				Map.Entry<Integer, ArrayList<Integer>> entry = 
						mapy.pollFirstEntry();
				sizey = entry.getKey();
				tempy.addAll(entry.getValue());
				if (tempy.size()>1) printyFlag = true;
			}else{
				printyFlag = true;
			}
			printx();
			printy();
		}
		System.out.println("matchNum"+matchNum);
	}
	
	static ArrayList<Integer> tempx = new ArrayList<Integer>();
	static ArrayList<Integer> tempy = new ArrayList<Integer>();
	static boolean printxFlag = false;
	static boolean printyFlag = false;
	static int sizex;
	static int sizey;
	static int matchNum=0;
	
	public static void printx() {
		
		int id = tempx.remove(0);
		if (printxFlag || printyFlag) {
			System.out.print("["+sizex+","+id+"]\t");
			HashMap<Integer, Integer> scoreMap = dataSetx.userScoreMap.get(id);
			print(scoreMap);
		}else {
			System.out.print("["+sizex+","+id+"]\t");
			matchNum++;
		}
	}
	
	public static void printy() {
		int id = tempy.remove(0);
		if (printxFlag || printyFlag) {
			System.out.print("["+sizey+","+id+"]\t");
			HashMap<Integer, Integer> scoreMap = dataSety.userScoreMap.get(id);
			print(scoreMap);
		}else
			System.out.println("["+sizey+","+id+"]");
	}
	
	public static void print(HashMap<Integer, Integer> scoreMap) {
		int[] nums = new int[6];
		for (int i=1; i<6; i++) nums[i]=0;
		for (int grade : scoreMap.values()) {
			nums[grade]++;
		}
		for (int i=1; i<6; i++) {
			System.out.print(i+":"+nums[i]+"\t");
		}
		System.out.println();
	}
	
	public static void ana() {
		DataSet2 dataSet = new DataSet2("train.txt", false);
		dataSet.readData(DataSet.Read_HashMap, false);
		dataSet.readData2(DataSet.Read_HashMap, false);
		
		TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>(new Comparator<Integer>(){
			@Override
			public int compare(Integer o1, Integer o2) {
				if (o1>o2)return -1;
				if (o1<o2)return 1;
				return 0;
			}
		});
		int max=0, maxid=-1;
		for (int id : dataSet.userScoreMap.keySet()) {
			int size = dataSet.userScoreMap.get(id).size();
//			if (size > max) {max = size;maxid=id;}
			if (size == 1461) {maxid=id;break;}
			int num;
			if (map.containsKey(size)) num = map.get(size);
			else num=0;
			map.put(size, num+1);
		}
		System.out.println("size "+max);
		System.out.println("id "+maxid);
		HashMap<Integer, Integer> scoreMap = dataSet.userScoreMap.get(maxid);
		int[] nums = new int[6];
		for (int i=1; i<6; i++) nums[i]=0;
		for (int grade : scoreMap.values()) {
			nums[grade]++;
		}
		for (int i=1; i<6; i++) {
			System.out.println(i+" "+nums[i]);
		}
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("analyze"));
//			for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
////				System.out.println(entry.getKey()+"\t"+entry.getValue());
////				double key = Math.log(entry.getKey());
////				double value = Math.log(entry.getValue());
////				writer.write(String.valueOf(key)+"\t"+String.valueOf(value));
////				writer.newLine();
//			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void ana2() {
		DataSet2 dataSet = new DataSet2("training_set.txt", false);
		dataSet.readData(DataSet.Read_HashMap, false);
		
		TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>(new Comparator<Integer>(){
			@Override
			public int compare(Integer o1, Integer o2) {
				if (o1>o2)return -1;
				if (o1<o2)return 1;
				return 0;
			}
		});
		int max=0, maxid=-1;
		for (int id : dataSet.userScoreMap.keySet()) {
			int size = dataSet.userScoreMap.get(id).size();
//			if (size > max) {max = size;maxid=id;}
			if (size == 1481) {maxid=id;break;}
			int num;
			if (map.containsKey(size)) num = map.get(size);
			else num=0;
			map.put(size, num+1);
		}
		
		System.out.println("size "+max);
		System.out.println("id "+maxid);
		HashMap<Integer, Integer> scoreMap = dataSet.userScoreMap.get(maxid);
		int[] nums = new int[6];
		for (int i=1; i<6; i++) nums[i]=0;
		for (int grade : scoreMap.values()) {
			nums[grade]++;
		}
		for (int i=1; i<6; i++) {
			System.out.println(i+" "+nums[i]);
		}
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("analyze"));
//			for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
//				System.out.println(entry.getKey()+"\t"+entry.getValue());
//				double key = Math.log(entry.getKey());
//				double value = Math.log(entry.getValue());
//				writer.write(String.valueOf(key)+"\t"+String.valueOf(value));
//				writer.newLine();
//			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void anam(int x) {
		DataSet2 dataSet = new DataSet2("train.txt", false);
		dataSet.readData(DataSet.Read_HashMap, false);
		dataSet.readData2(DataSet.Read_HashMap, false);
		
		TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>(new Comparator<Integer>(){
			@Override
			public int compare(Integer o1, Integer o2) {
				if (o1>o2)return -1;
				if (o1<o2)return 1;
				return 0;
			}
		});
		int max=0, maxid=-1;
		for (int id : dataSet.movieScoreMap.keySet()) {
			int size = dataSet.movieScoreMap.get(id).size();
//			if (size > max) {max = size;maxid=id;}
			if (size == x) {System.out.println(id);;break;}
			int num;
			if (map.containsKey(size)) num = map.get(size);
			else num=0;
			map.put(size, num+1);
		}
//		System.out.println("size "+max);
//		System.out.println("id "+maxid);
//		HashMap<Integer, Integer> scoreMap = dataSet.userScoreMap.get(maxid);
//		int[] nums = new int[6];
//		for (int i=1; i<6; i++) nums[i]=0;
//		for (int grade : scoreMap.values()) {
//			nums[grade]++;
//		}
//		for (int i=1; i<6; i++) {
//			System.out.println(i+" "+nums[i]);
//		}
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("analyze"));
			for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
//				System.out.println(entry.getKey()+"\t"+entry.getValue());
//				double key = Math.log(entry.getKey());
//				double value = Math.log(entry.getValue());
//				writer.write(String.valueOf(key)+"\t"+String.valueOf(value));
//				writer.newLine();
			}
			
			
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void anam2(int y) {
		DataSet2 dataSet = new DataSet2("training_set.txt", false);
		dataSet.readData(DataSet.Read_HashMap, false);
		
		TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>(new Comparator<Integer>(){
			@Override
			public int compare(Integer o1, Integer o2) {
				if (o1>o2)return -1;
				if (o1<o2)return 1;
				return 0;
			}
		});
		int max=0, maxid=-1;
		for (int id : dataSet.movieScoreMap.keySet()) {
			int size = dataSet.movieScoreMap.get(id).size();
//			if (size > max) {max = size;maxid=id;}
			if (size == y) {System.out.println(id);;break;}
			int num;
			if (map.containsKey(size)) num = map.get(size);
			else num=0;
			map.put(size, num+1);
		}
		
//		System.out.println("size "+max);
//		System.out.println("id "+maxid);
//		HashMap<Integer, Integer> scoreMap = dataSet.userScoreMap.get(maxid);
//		int[] nums = new int[6];
//		for (int i=1; i<6; i++) nums[i]=0;
//		for (int grade : scoreMap.values()) {
//			nums[grade]++;
//		}
//		for (int i=1; i<6; i++) {
//			System.out.println(i+" "+nums[i]);
//		}
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("analyze"));
			for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
//				System.out.println(entry.getKey()+"\t"+entry.getValue());
//				double key = Math.log(entry.getKey());
//				double value = Math.log(entry.getValue());
//				writer.write(String.valueOf(key)+"\t"+String.valueOf(value));
//				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
