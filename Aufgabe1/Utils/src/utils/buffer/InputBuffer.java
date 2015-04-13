package utils.buffer;

public interface InputBuffer<E extends Object>{
	
	public boolean addMessageIntoInput(E input);
	
	public E getMessageFromOutput();

}
