package org.tianshan.dm;

import java.io.File;

public class test {
	public static void main(String[] args) {
		File root = new File("xx/");
		File[] files = root.listFiles();
		for (File f : files) {
			System.out.println(f.toString());
		}
	}
}
