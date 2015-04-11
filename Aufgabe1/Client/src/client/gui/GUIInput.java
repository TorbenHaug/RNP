package client.gui;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.rmi.server.UID;

import client.controller.Controller;
import utils.adt.NetworkToken;
import utils.buffer.InputBuffer;

public class GUIInput implements Runnable{

	private final BufferedReader br;
	private boolean isStopped = false;
	private final InputBuffer<NetworkToken> buffer;
	private UID connection = null;

	public GUIInput(InputBuffer<NetworkToken> buffer) {
		br = new BufferedReader(new InputStreamReader(System.in));
		this.buffer = buffer;
	}
	@Override
	public void run() {
		String cmd = "";
		while(!isStopped){
			try {
				cmd = br.readLine();
			} catch (IOException e) {
				Controller.shutdown();
			}
			if(cmd != null && !isStopped){
				String splitedCmd[] = cmd.split("\\s+");
				if(splitedCmd[0].equals("CONNECT") && connection == null){
					if(splitedCmd.length == 3){
						try {
							connection = Controller.connect(splitedCmd[1], Integer.valueOf(splitedCmd[2]));
						} catch (NumberFormatException e) {
							System.out.println("ERROR Please enter a correct port");
						} catch (UnknownHostException e) {
							System.out.println("ERROR Unknown host");
						} catch (IOException e) {
							System.out.println("ERROR Unknown host");
						}
					}
					else{
						System.out.println("ERROR Please use CONNECT <Adress> <Port>");
					}
				}else{
					if(connection != null && !cmd.equals("EXIT")){
						buffer.addMessageIntoInput(new NetworkToken(cmd, connection, "127.0.0.1"));
					}else if(connection != null && cmd.equals("EXIT")){
						System.out.println("ERROR Please disconnect from server befor exiting client");
					}else{
						if(cmd.equals("EXIT")){
							Controller.shutdown();
							stop();
						}else{
							System.out.println("ERROR Please use CONNECT <Adress> <Port>");
						}
					}
				}
			}
		}
		System.out.println("Ausgabe gestoppt");
		
	}
	
	public void stop(){
		isStopped = true;
		/*Robot r;
		try {
			r = new Robot();
			r.keyPress(KeyEvent.VK_ENTER);
			r.keyRelease(KeyEvent.VK_ENTER);
		} catch (AWTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			System.in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	public void connectionStopped(UID currentConnection) {
		connection = null;
		
	}
}
