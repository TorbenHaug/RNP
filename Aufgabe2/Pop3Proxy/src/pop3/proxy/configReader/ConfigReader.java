package pop3.proxy.configReader;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ConfigReader {
	
	/*
	 * get data json file in current directory 
	 * */

	public static Set<AccountConfig> getFileInput(String dataFolder) throws IOException{
		Set<AccountConfig> dataSet = new HashSet<>();
		
		try {
			FileReader reader = new FileReader(dataFolder + "doc.json");
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
			JSONArray data= (JSONArray) jsonObject.get("data");
			
			/* 
			 * get each element of the JSONArray data element
			 * */
			Iterator iterator = data.iterator();
			while (iterator.hasNext()) {
				JSONObject dataObj = (JSONObject) iterator.next();
				String user = (String) dataObj.get("user");
			    System.out.println("The username is: " + user);
		
				String pass = (String) dataObj.get("pass");
			    System.out.println("The password is: " + pass);
			
			    String server = (String) dataObj.get("server");
			    System.out.println("The server is: " + server);
			 
			    int port =  Integer.parseInt(dataObj.get("port").toString());
			    System.out.println("The port is: " + port);
			    
			    int timeInterval = Integer.parseInt(dataObj.get("timeInterval").toString());
			    System.out.println("The timeInterval is: " + timeInterval);
			    
			    System.out.println("");
				
			    AccountConfig userData = new ConfigsImpl(user, pass, server, port, timeInterval);
			    
			    dataSet.add(userData);
			 }
			
		} catch (FileNotFoundException e) {
			throw new IOException("The File does not exists" + e);
		} catch (IOException e) {
			throw new IOException("Could not read File" + e);
		} catch (ParseException e) {
			throw new IOException("Could not parse the json file" + e);
		}
		
		System.out.println(dataSet.size());
		return dataSet;
	
	}
	
}


/* Quelle: http://examples.javacodegeeks.com/core-java/json/java-json-parser-example/s */

