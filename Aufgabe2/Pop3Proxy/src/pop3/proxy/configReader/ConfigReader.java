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
	
	
	public static GeneralConfig getFileInputForGeneral(String dataFolder) throws IOException{
		GeneralConfig userData;
		
		try {
			FileReader reader = new FileReader(dataFolder + "doc.json");
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
			JSONArray dataServer = (JSONArray) jsonObject.get("configServer");
			JSONArray dataClient = (JSONArray) jsonObject.get("configClient");
			
			int serverPort = 8070;
			int maxServerConnection = 3;
			int serverTimeout = 30;
			int maxSignsPerLineServer = 512;
			int maxClientConnections = 3;
			int clientTimeout = 30;
			int maxSignsPerLineClient = 512;
			int maxMailSize = 5;
			
			/* 
			 * get each element of the JSONArray data element
			 * */
			Iterator iterator = dataServer.iterator();
			while (iterator.hasNext()) {
				JSONObject dataObj = (JSONObject) iterator.next();
				
				serverPort =  Integer.parseInt(dataObj.get("serverPort").toString());
				System.out.println("The port is: " + serverPort);
				
				maxServerConnection = Integer.parseInt(dataObj.get("maxServerConnection").toString());
			    System.out.println("The port is: " + maxServerConnection);
			    
			    serverTimeout = Integer.parseInt(dataObj.get("serverTimeout").toString());
			    System.out.println("The port is: " + serverTimeout);
			    
			    maxSignsPerLineServer = Integer.parseInt(dataObj.get("maxSignsPerLineServer").toString());
			    System.out.println("The port is: " + maxSignsPerLineServer);

			 }
			
			Iterator iterator1 = dataClient.iterator();
			while (iterator1.hasNext()) {
				JSONObject dataObj = (JSONObject) iterator1.next();
				
				maxClientConnections =  Integer.parseInt(dataObj.get("maxClientConnections").toString());
				System.out.println("The port is: " + maxClientConnections);
				
				clientTimeout = Integer.parseInt(dataObj.get("clientTimeout").toString());
			    System.out.println("The port is: " + clientTimeout);
			    
			    maxSignsPerLineClient = Integer.parseInt(dataObj.get("maxSignsPerLineClient").toString());
			    System.out.println("The port is: " + maxSignsPerLineClient);
			    
			    maxMailSize = Integer.parseInt(dataObj.get("maxMailSize").toString());
			    System.out.println("The port is: " + maxMailSize);

			 }
			
			userData = new GeneralConfigImpl(serverPort, maxServerConnection, serverTimeout, maxSignsPerLineServer, 
					maxClientConnections, clientTimeout, maxSignsPerLineClient, maxMailSize);
		    
		   
		} catch (FileNotFoundException e) {
			throw new IOException("The File does not exists" + e);
		} catch (IOException e) {
			throw new IOException("Could not read File" + e);
		} catch (ParseException e) {
			throw new IOException("Could not parse the json file" + e);
		}
		
		return userData;
	}
	
}


/* Quelle: http://examples.javacodegeeks.com/core-java/json/java-json-parser-example/ */

