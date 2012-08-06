package server;

import client.authenticated.Client;
import exception.DuplicateItemException;
import exception.ItemNotFoundException;

public class ClientList 
{
	private final int capacity;
	private int size;
	private ClientNode root;
	
	public ClientList(int capacity)
	{
		this.capacity = capacity;
	}
	
	private static class ClientNode
	{
		private Client client;
		private ClientNode left;
		private ClientNode right;
		
		public ClientNode(Client client, ClientNode left, ClientNode right)
		{
			this.client = client;
			this.left = left;
			this.right = right;
		}
		
		public void setClient(Client client)
		{
			this.client = client;
		}
		
		public void setLeft(ClientNode left)
		{
			this.left = left;
		}
		
		public void setRight(ClientNode right)
		{
			this.right = right;
		}
		
		public Client getClient()
		{
			return client;
		}
		
		public ClientNode getLeft()
		{
			return left;
		}
		
		public ClientNode getRight()
		{
			return right;
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
	
	public synchronized boolean insertClient(Client client)
	{
		if(!isFull())
		{
			try
			{
				root = internalInsertClient(root, client);
				size++;
				return true;
			}
			catch(DuplicateItemException die)
			{
				System.out.println("Duplicate item");
				return false;
			}
		}
		else
		{
			System.out.println("Full");
			return false;
		}
	}
	
	private ClientNode internalInsertClient(ClientNode root, Client client) throws DuplicateItemException
	{
		if(root == null)
		{
			root = new ClientNode(client, null, null);
		}
		else if(client.getUserId() > root.getClient().getUserId())
		{
			root.setRight(internalInsertClient(root.getRight(), client));
		}
		else if(client.getUserId() < root.getClient().getUserId())
		{
			root.setLeft(internalInsertClient(root.getLeft(), client));
		}
		else
		{
			throw new DuplicateItemException(String.valueOf(client.getUserId()));
		}
		
		return root;
	}
	
	public synchronized boolean removeClient(long userId)
	{
		try
		{
			root = internalRemoveClient(root, userId);
			size--;
			return true;
		}
		catch(ItemNotFoundException infe)
		{
			return false;
		}
	}
	
	private ClientNode internalRemoveClient(ClientNode root, long userId) throws ItemNotFoundException
	{
		if(root == null)
		{
			throw new ItemNotFoundException(String.valueOf(userId));
		}
		else if(userId > root.getClient().getUserId())
		{
			root.setRight(internalRemoveClient(root.getRight(), userId));
		}
		else if(userId < root.getClient().getUserId())
		{
			root.setLeft(internalRemoveClient(root.getLeft(), userId));
		}
		else if(root.getLeft() != null && root.getRight() != null)
		{
			root.setClient(getMinClientNode(root.getRight()).getClient());
			root.setRight(removeMinClientNode(root.getRight()));
		}
		else
		{
			if(root.getLeft() != null)
			{
				root = root.getLeft();
			}
			else
			{
				root = root.getRight();
			}
		}
		
		return root;
	}
	
	private ClientNode getMinClientNode(ClientNode root)
	{
		while(root.getLeft() != null)
		{
			root = root.getLeft();
		}
		
		return root;
	}
	
	private ClientNode removeMinClientNode(ClientNode root)
	{
		if(root.getLeft() != null)
		{
			root.setLeft(removeMinClientNode(root.getLeft()));
			return root;
		}
		else
		{
			return root.getRight();
		}
	}
	
	public synchronized boolean containsClient(int userId)
	{
		if(!isEmpty())
		{
			return internalContainsClient(root, userId);
		}
		else
		{
			return false;
		}
	}
	
	private boolean internalContainsClient(ClientNode root, long userId)
	{
		if(root != null)
		{
			if(userId == root.getClient().getUserId())
			{
				return true;
			}
			else if(userId > root.getClient().getUserId())
			{
				return internalContainsClient(root.getRight(), userId);
			}
			else if(userId < root.getClient().getUserId())
			{
				return internalContainsClient(root.getLeft(), userId);
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	public synchronized Client getClient(long userId)
	{
		if(!isEmpty())
		{
			return internalGetClient(root, userId);
		}
		else
		{
			return null;
		}
	}
	
	private Client internalGetClient(ClientNode root, long userId)
	{
		if(root != null)
		{
			if(userId == root.getClient().getUserId())
			{
				return root.getClient();
			}
			else if(userId > root.getClient().getUserId())
			{
				return internalGetClient(root.getRight(), userId);
			}
			else if(userId < root.getClient().getUserId())
			{
				return internalGetClient(root.getLeft(), userId);
			}
			else
			{
				return null;
			}
		}
		else
		{
			return null;
		}
	}

	public synchronized int getCapacity()
	{
		return capacity;
	}
	
	public synchronized int getSize()
	{
		return size;
	}

	public synchronized void print()
	{
		if(!isEmpty())
		{
			internalPrint(root);
		}
		else
		{
			System.out.println("Error");
		}
	}

	private void internalPrint(ClientNode root)
	{
		if(root != null)
		{
			internalPrint(root.getLeft());
			System.out.println(root.getClient().getUserId());
			internalPrint(root.getRight());
		}
	}
}
