package utils.buffer;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BufferImpl<E extends Object> implements Buffer<E>{

	private final Queue<E> inQueue;
	private final Queue<E> outQueue;
	private final Lock inQueueReadLock;
	private final Lock outQueueReadLock;
	private final Condition inQueueNotEmpty;
	private final Condition outQueueNotEmpty;
	private boolean isStoped = false;
	
	public BufferImpl() {
		this.inQueue = new LinkedList<E>();
		this.outQueue = new LinkedList<E>();
		this.inQueueReadLock = new ReentrantLock();
		this.outQueueReadLock = new ReentrantLock();
		this.inQueueNotEmpty = inQueueReadLock.newCondition();
		this.outQueueNotEmpty = outQueueReadLock.newCondition();
	}
	
	
	@Override
	public boolean addMessageIntoInput(E input) {
		inQueueReadLock.lock();
		boolean retVal = false;
		try{
			retVal = inQueue.offer(input);
			inQueueNotEmpty.signal();
		}finally{
			inQueueReadLock.unlock();
		}
		return retVal;
	}
	
	
	@Override
	public E getMessageFromOutput() {
		outQueueReadLock.lock();
			while (outQueue.isEmpty() && !isStoped){
				try {
					outQueueNotEmpty.await();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			E retVal = outQueue.remove();
		outQueueReadLock.unlock();
		return retVal;
	}
	
	
	@Override
	public boolean addMessageIntoOutput(E output) {
		outQueueReadLock.lock();
		boolean retVal = false;
		try{
			retVal = outQueue.offer(output);
			outQueueNotEmpty.signal();
		}finally{
			outQueueReadLock.unlock();
		}
		return retVal;
	}
	
	
	@Override
	public E getMessageFromInput() {
		inQueueReadLock.lock();
			while (inQueue.isEmpty() && !isStoped){
				try {
					inQueueNotEmpty.await();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			E retVal = inQueue.remove();
		inQueueReadLock.unlock();
		return retVal;
	}


	@Override
	public void stop() {
		isStoped = true;
		inQueueNotEmpty.notifyAll();;
		outQueueNotEmpty.notifyAll();
	}	
	
	

}
