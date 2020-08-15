import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;


public class Solution {
	public static void main(String[] args) throws IOException {
		/*
		 * BufferedReader bufferedReader = new BufferedReader(new
		 * InputStreamReader(System.in)); BufferedWriter bufferedWriter = new
		 * BufferedWriter(new FileWriter(System.getenv("OUTPUT_PATH")));
		 * 
		 * int userId = Integer.parseInt(bufferedReader.readLine().trim());
		 * 
		 * int locationId = Integer.parseInt(bufferedReader.readLine().trim());
		 * 
		 * int netStart = Integer.parseInt(bufferedReader.readLine().trim());
		 * 
		 * int netEnd = Integer.parseInt(bufferedReader.readLine().trim());
		 */

		int userId = 2;
		int locationId = 8;
		int netStart = 5;
		int netEnd = 50;

		int result;
		long startTime = System.currentTimeMillis();
		result = Result.getTransactions(userId, locationId, netStart, netEnd);
		long stopTime = System.currentTimeMillis();
		System.out.println("Output :" + result);
		
		
		
		
		System.out.println("Time elapsed : " + (stopTime - startTime)/1000.0);
		
		/*
		 * bufferedWriter.write(String.valueOf(result)); bufferedWriter.newLine();
		 * 
		 * bufferedReader.close(); bufferedWriter.close();
		 */
	}
}
