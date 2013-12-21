package org.tianshan.dm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LibFM {
	public static void main(String[] args) {
//		inputPre();
		makeRound();
	}
	
	public static void inputPre() {
		try {
			BufferedReader reader_test = new BufferedReader(new FileReader("test.txt"));
			BufferedReader reader_rate = new BufferedReader(new FileReader("test.rate"));
			BufferedWriter writer = new BufferedWriter(new FileWriter("test2.txt"));
			
			String line;
			
			while ((line=reader_test.readLine())!=null) {
				String line_rate = reader_rate.readLine();
				writer.write(line+"\t"+line_rate);
				writer.newLine();
			}
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
