package com.munchkin.db;

public class Cardimg {

	
	private long id;
	private String name;
	
	public Cardimg()
	{
		id = -1;
		name = "-2";
	}
	
	public long getID()
	{
		return id;
	}
	
	public String getImg()
	{
		// TODO change string name to an image
		return name;
	}
	
	public void setID(long id)
	{
		this.id = id;
	}
	
	public void setImg(String name)
	{
		this.name = name;
	}
	
}
