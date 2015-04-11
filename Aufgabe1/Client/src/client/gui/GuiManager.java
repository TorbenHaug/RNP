package client.gui;

import java.util.concurrent.ExecutorService;

import utils.adt.NetworkToken;
import utils.buffer.InputBuffer;

public class GuiManager{
	private final ExecutorService executor;
	private InputBuffer<NetworkToken> buffer;
	private final GUIInput guiInput;
	private final GUIOutput guiOutput;
	
	public GuiManager(InputBuffer<NetworkToken> buffer, ExecutorService executor) {
		this.executor = executor;
		this.buffer = buffer;
		this.guiInput = new GUIInput(buffer);
		this.guiOutput = new GUIOutput(buffer);
		executor.execute(guiInput);
		executor.execute(guiOutput);
		
	}
	public void stop(){
		System.out.println("GIUInput Herunterfahren");
		guiInput.stop();
		System.out.println("GIUOutPut Herunterfahren");
		guiOutput.stop();
	}
}
