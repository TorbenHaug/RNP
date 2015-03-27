package utils.buffer;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BufferImpl<E extends Object> implements Buffer<E>{

	private final ConcurrentLinkedQueue<E> inQueue;
	private final ConcurrentLinkedQueue<E> outQueue;
	private final Lock inQueueReadLock;
	private final Lock outQueueReadLock;
	private final Condition inQueueNotEmpty;
	private final Condition outQueueNotEmpty;
	
	public BufferImpl() {
		super();
		this.inQueue = new ConcurrentLinkedQueue<E>();
		this.outQueue = new ConcurrentLinkedQueue<E>();
		this.inQueueReadLock = new ReentrantLock();
		this.outQueueReadLock = new ReentrantLock();
		this.inQueueNotEmpty = inQueueReadLock.newCondition();
		this.outQueueNotEmpty = outQueueReadLock.newCondition();
	}
	
	
	@Override
	public boolean addMessageIntoInput(E input) {
		boolean retVal = inQueue.offer(input);
		inQueueNotEmpty.signal();
		return retVal;
	}
	
	
	@Override
	public E getMessageFromOutput() {
		outQueueReadLock.lock();
			while (outQueue.isEmpty()){
				try {
					outQueueNotEmpty.await();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			E retVal = outQueue.poll();
		outQueueReadLock.unlock();
		return retVal;
	}
	
	
	@Override
	public boolean addMessageIntoOutput(E output) {
		boolean retVal = outQueue.offer(output);
		outQueueNotEmpty.signal();
		return retVal;
	}
	
	
	@Override
	public E getMessageFromInput() {
		inQueueReadLock.lock();
			while (inQueue.isEmpty()){
				try {
					inQueueNotEmpty.await();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			E retVal = inQueue.poll();
		inQueueReadLock.unlock();
		return retVal;
	}	
	
	

}
