import java.io.*;
import java.math.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import org.json.simple.parser.ParseException;

class Task implements Callable<Double> {

	int page;

	public Task(int page) {
		this.page = page;
	}

	public void run() {
		Result.readDataFromUrl_page(this.page);
	}

	@Override
	public Double call() throws Exception {
		return Result.readDataFromUrl_page(this.page);
	}
}

class Result {
	/*
	 * Complete the 'getTransactions' function below.
	 *
	 * The function is expected to return an INTEGER. The function accepts following
	 * parameters: 1. INTEGER userId 2. INTEGER locationId 3. INTEGER netStart 4.
	 * INTEGER netEnd
	 *
	 * https://jsonmock.hackerrank.com/api/transactions/search?userId=
	 */

	static int userId;
	static int locationId;
	static int netStart;
	static int netEnd;

	private static final int MAX_NUM_OF_THREADS = 20;

	static void setInputValues(int user_Id, int location_Id, int net_Start, int net_End) {
		userId = user_Id;
		locationId = location_Id;
		netStart = net_Start;
		netEnd = net_End;
	}

	public static int getTransactions(int userId, int locationId, int netStart, int netEnd) {

		setInputValues(userId, locationId, netStart, netEnd);

		String url = "https://jsonmock.hackerrank.com/api/transactions/search?userId=" + userId;
		Integer current_page = 0;
		Integer total_page = 0;
		Double sum = 0.0;

		try {
			//read the stream from URL
			InputStream is = new URL(url).openStream();
			JSONParser parser = new JSONParser();

			try {
				BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
				
				//reads everything from buffer reader and put to String Variable
				String jsonText = readAll(rd);
				
				//Parse Json using JSON.simple parser
				JSONObject json = (JSONObject) parser.parse(jsonText);
				
				//fetch current and total pages from 1st page
				current_page = ((Long) json.get("page")).intValue();
				total_page = ((Long) json.get("total_pages")).intValue();

				//save the sum return from this page
				//note that this is not the final sum
				Double sum_part = getSumFromData((JSONArray) json.get("data"));
				sum += sum_part;
				
			} catch (org.json.simple.parser.ParseException | java.text.ParseException e) {
				System.out.println("Error Occurred while Json Parsing" + e.getMessage());
			} finally {
				is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Once we know number of pages, now we can call the API with multiple threads
		//Create a Thread pool and add new tasks to it, each having unique page
		ExecutorService pool = Executors.newFixedThreadPool(MAX_NUM_OF_THREADS);
		List<Task> taskList = new ArrayList<>();
		for (int i = (current_page + 1); i <= total_page; i++) {
			taskList.add(new Task(i));
		}

		try {
			//Invoke all Threads from Thread pool
			List<Future<Double>> results = pool.invokeAll(taskList);
			
			//add up the sum return from each page to final sum
			for (Future<Double> f : results) {
				sum += f.get();
			}
			
			//once work done,shutdown the thread pool
			pool.shutdown();
			
		} catch (InterruptedException | ExecutionException e) {
			System.out.println("Error Occurred : " + e.getMessage());
		}

		return (int) Math.round(sum);
	}

	//This function does same steps as first page, just for other pages
	static Double readDataFromUrl_page(int page) {
		String url = "https://jsonmock.hackerrank.com/api/transactions/search?userId=" + userId + "&page=" + page;
		Double sum_part = 0.0;
		JSONParser parser = new JSONParser();
		try {
			InputStream is = new URL(url).openStream();
			try {
				BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
				String jsonText = readAll(rd);
				JSONObject json = (JSONObject) parser.parse(jsonText);
				sum_part = getSumFromData((JSONArray) json.get("data"));

			} catch (ParseException | java.text.ParseException e) {
				System.out.println("Error Occurred while Json Parsing" + e.getMessage());
			} finally {
				is.close();
			}
		} catch (IOException e) {
			System.out.println("Error Occurred while Reading URL" + e.getMessage());
		}

		return sum_part;
	}

	//This function parse and Validate the inputs like location is match or not 
	static Double getSumFromData(JSONArray arr) throws java.text.ParseException {
		Double sum = 0.0;

		for (int i = 0; i < arr.size(); i++) {
			int location_Id = ((Long) ((JSONObject) ((JSONObject) arr.get(i)).get("location")).get("id")).intValue();

			//if location is not matched
			if (location_Id != locationId)
				continue;

			String ip_address = (String) ((JSONObject) arr.get(i)).get("ip");

			//Check if ip adress is valid and is in range of netStart and netEnd
			if (ip_address != null && isIPAddressValid(ip_address)) {

				Integer ipPrefix = Integer.valueOf((ip_address.split("\\.")[0]));
				boolean inRange = (ipPrefix >= netStart && ipPrefix <= netEnd);
				if (inRange) {
					NumberFormat format = NumberFormat.getCurrencyInstance();
					String amountStr = (String) ((JSONObject) arr.get(i)).get("amount");
					Number number = format.parse(amountStr);
					sum += number.doubleValue();
				}
			}
		}
		return sum;
	}

	public static boolean isIPAddressValid(String ip) {
		//if ip is null, dont do anything
		if (ip == null) {
			return false;
		}

		// Regex Range from 0 to 255.
		String maxIpRange = "(\\d{1,2}|(0|1)\\" + "d{2}|2[0-4]\\d|25[0-5])";

		//  Regex like 111.111.111.111 to validate Ip adress
		String ipRegex = maxIpRange + "\\." + maxIpRange + "\\." + maxIpRange + "\\." + maxIpRange;

		// Regex compilation
		Pattern pattern = Pattern.compile(ipRegex);

		//matches regex with ip
		Matcher matcher = pattern.matcher(ip);

		//if regex matched return true else false
		return matcher.matches();
	}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

}
