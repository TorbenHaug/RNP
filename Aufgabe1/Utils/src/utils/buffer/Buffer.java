package utils.buffer;

public interface Buffer<E extends Object> extends InputBuffer<E>, OutputBuffer<E>{
	public void stop();
}
