package org.tianshan.dm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SVDFeature {
	
	static DataPreDo pre;
	
	public static void main(String[] args) {
//		makeRound();
		
		pre = new DataPreDo(true);
		System.out.println("user num:"+pre.getUserNum());
		System.out.println("movie num:"+pre.getMovieNum());
		out_train();
		out_test();
	}
	
	public static void out_train() {
		try {
			BufferedReader reader_train = new BufferedReader(new FileReader("train.txt"));
			BufferedWriter writer = new BufferedWriter(new FileWriter("ua.base"));
			String line;
			while ((line = reader_train.readLine())!=null) {
				String[] attr = line.split("\t");
				int userId = Integer.parseInt(attr[0]);
				int movieId = Integer.parseInt(attr[1]);
				int grade = Integer.parseInt(attr[2]);
				
				userId = pre.getUserIndex(userId)+1;
				movieId = pre.getMovieIndex(movieId)+1;
				
				writer.write(userId+"\t"+movieId+"\t"+grade);
				writer.newLine();
			}
			reader_train.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void out_test() {
		
		try {
			BufferedReader reader_test = new BufferedReader(new FileReader("test.txt"));
			BufferedReader reader_rate = new BufferedReader(new FileReader("test.rate"));
			BufferedWriter writer = new BufferedWriter(new FileWriter("ua.test"));
			
			String line, lineRate;
			int numU=0, numM=0;
			while ((line = reader_test.readLine())!=null) {
				lineRate = reader_rate.readLine();
				String[] attr = line.split("\t");
				int userId = Integer.parseInt(attr[0]);
				int movieId = Integer.parseInt(attr[1]);
				
				userId = pre.getUserIndex(userId)+1;
				movieId = pre.getMovieIndex(movieId)+1;
				
				if (userId == -1){
					userId = 9619;
					numU++;
				}
				
				if (movieId == -1){
					movieId = 7890;
					numM++;
				}
				
				writer.write(userId+"\t"+movieId+"\t"+lineRate);
				writer.newLine();
			}
			System.out.println("numU:"+numU+" numM:"+numM);
			
			reader_test.close();
			reader_rate.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void makeRound() {
		try {
			BufferedReader reader_test = new BufferedReader(new FileReader("pred.txt"));
			BufferedWriter writer = new BufferedWriter(new FileWriter("test.rate"));
			String line;
			while ((line = reader_test.readLine())!=null) {
				double result = Double.parseDouble(line);
				
				writer.write(String.valueOf(getRound(result)));
				writer.newLine();
			}
			writer.close();
			reader_test.close();
		} catch (IOException e) {
			e.printStackTrace();
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
