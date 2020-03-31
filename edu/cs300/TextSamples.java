package edu.cs300;

import java.io.*;
import java.lang.*;
import java.util.Scanner;

public class TextSamples extends Thread {
	public static void main(String[] args) {
		TextSamples ts = new TextSamples();
		ts.start();
		PassageProcessor.ProcessPassages();
		try {
			ts.join();
		} catch(Exception e) {}
			
		System.out.println("Terminating ...");
	}

	public void run() {
		int passageCount = 0;
		TextSamples ts = new TextSamples();
		try {
			Scanner reader = new Scanner(new File("passages.txt"));
	
			for (;reader.hasNextLine();passageCount++)
				reader.nextLine();
			reader.close();
	
			Process proc;
			Runtime runtime = Runtime.getRuntime();
			
			proc = runtime.exec("./msgsnd con 1");
			for (int i=0;i<passageCount;i++)
				proc = runtime.exec("./msgrcv");
	
			proc = runtime.exec("./msgsnd pre 2");
			for (int i=0;i<passageCount;i++)
				proc = runtime.exec("./msgrcv");
			
			proc = runtime.exec("./msgsnd wor 3");
			for (int i=0;i<passageCount;i++)
				proc = runtime.exec("./msgrcv");
		
			proc = runtime.exec("./msgsnd no 0");
			for (int i=0;i<passageCount;i++)
				proc = runtime.exec("./msgrcv");
		} catch(Exception e) {}
	}
}
