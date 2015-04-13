package utils.buffer;

public interface OutputBuffer<E extends Object> {
	
	public boolean addMessageIntoOutput(E output);
	
	public E getMessageFromInput();

}
