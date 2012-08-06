package client.authenticated;

import update.Update;

public final class UpdateQueue
{
	private final int capacity;
	private int size;
	private UpdateNode head;
	private UpdateNode tail;
	
	public UpdateQueue(int capacity) throws IllegalArgumentException
	{
		if(capacity < 1)
		{
			throw new IllegalArgumentException("Capacity cannot be less than 1");
		}
		
		this.capacity = capacity;
	}
	
	private static class UpdateNode
	{
		private Update update;
		private UpdateNode next;
		
		public UpdateNode(Update update, UpdateNode next)
		{
			this.update = update;
			this.next = next;
		}
		
		public void setNext(UpdateNode next)
		{
			this.next = next;
		}
		
		public Update getUpdate()
		{
			return update;
		}
		
		public UpdateNode getNext()
		{
			return next;
		}
	}
	
	public synchronized boolean isEmpty()
	{
		return(size == 0);
	}
	
	public synchronized boolean isFull()
	{
		return(size == capacity);
	}
	
	public synchronized boolean putUpdate(Update update)
	{
		if(!isFull())
		{
			UpdateNode newNode = new UpdateNode(update, null);
			
			if(isEmpty())
			{
				head = newNode;
				tail = newNode;
			}
			else
			{
				tail.setNext(newNode);
				tail = newNode;
			}
			
			size++;
			
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public synchronized Update takeUpdate()
	{
		if(!isEmpty())
		{
			UpdateNode current = head;
			head = head.getNext();
			
			if(head == null)
			{
				tail = null;
			}
			
			size--;
			
			return current.getUpdate();
		}
		else
		{
			return null;
		}
	}
	
	//Not synchronized since it is declared to be final
	public int getCapcity()
	{
		return capacity;
	}
	
	public synchronized int getSize()
	{
		return size;
	}
	
	public synchronized int getRemainingCapacity()
	{
		return capacity - size;
	}
}
