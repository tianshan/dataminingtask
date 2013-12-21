package org.tianshan.dm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CompareResult {
	static String[] paths = {
//		"test.1.rate",
//		"test.2.rate"
		};
	
	public static void main(String[] args) {
		
		combine();
		
//		compare2();
	}
	
	public static void combine() {
		File root = new File("xx/");
		File[] files = root.listFiles();
		String[] paths = new String[files.length];
		
		for (int i=0; i<files.length; i++) {
			paths[i] = files[i].toString();
		}
		
		double[] weight = new double[paths.length];
		for (int i=0; i<paths.length; i++) {
			String[] attr = paths[i].split("\\.");
			if (attr.length<3) {
				System.err.println("file name error");
				return;
			}
			weight[i] = Integer.parseInt(attr[1]);
			weight[i] = 1 / weight[i];
			
			weight[i] = 1;
		}
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("xx/test.rate"));
			BufferedReader[] reads = new BufferedReader[paths.length];
			for (int i=0; i<paths.length; i++) {
				reads[i] = new BufferedReader(new FileReader(paths[i]));
			}
			
			int[] results = new int[paths.length];
			String line;
			int diffNum = 0;
			double[] nums = new double[6];
			while ((line = reads[0].readLine())!=null) {
				for (int i=1; i<6; i++) nums[i] = 0;
				results[0] = Integer.parseInt(line);
				nums[results[0]] += weight[0];
				for (int i=1; i<paths.length; i++) {
					line = reads[i].readLine();
					results[i] = Integer.parseInt(line);
					nums[results[i]] += weight[i];
				}
				double maxNum = 0;
				int maxGrade=-1;
				for (int i=1; i<6; i++) {
					if (nums[i] > maxNum) {
						maxNum = nums[i];
						maxGrade = i;
					}
				}
				writer.write(String.valueOf(maxGrade));
				writer.newLine();
//				if (results[0] != maxGrade && nums[results[0]] != nums[maxGrade]) {
//					diffNum++;
//					System.out.print(results[0]);
//					for (int i=1; i<6;i++) 
//						System.out.print("\t"+nums[i]);
//					System.out.println();
//				}
			}
//			System.out.println("diffNum:" + diffNum);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void compare2() {
		try {
			BufferedReader[] reads = new BufferedReader[paths.length];
			for (int i=0; i<paths.length; i++) {
				reads[i] = new BufferedReader(new FileReader("svdfeature/"+paths[i]));
			}
			
			int[] results = new int[paths.length];
			String line;
			int diffNum = 0;
			while ((line = reads[0].readLine())!=null) {
				results[0] = Integer.parseInt(line);
				for (int i=1; i<paths.length; i++) {
					line = reads[i].readLine();
					results[i] = Integer.parseInt(line);
				}
				if (results[0] != results[1]) {
					diffNum++;
				}
			}
			System.out.println("diffNum :"+diffNum);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void compare() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("xx/test.rate"));
			BufferedReader[] reads = new BufferedReader[paths.length];
			for (int i=0; i<paths.length; i++) {
				reads[i] = new BufferedReader(new FileReader("xx/"+paths[i]));
			}
			
			int[] results = new int[paths.length];
			String line;
			int diffNum = 0;
			while ((line = reads[0].readLine())!=null) {
				results[0] = Integer.parseInt(line);
				for (int i=1; i<paths.length; i++) {
					line = reads[i].readLine();
					results[i] = Integer.parseInt(line);
				}
				int result = results[0];
				if (results[0] != results[1]) {
					
					if (results[0] != results[2]){
						if(Math.random()>0.4){
							result = results[1];
							diffNum++;
						}
					}
//					System.out.println(results[0]+" "+results[1]+" "+results[2]);
				}
				writer.write(String.valueOf(result));
				writer.newLine();
			}
			System.out.println("diffNum 1015:"+diffNum);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
