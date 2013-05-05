package com.munchkin.network.ui_posts;

public abstract class PostRunnable implements Runnable{
	
	protected int a;
	protected int b;
	
	public PostRunnable(int num){
		a = num;
	}
	
	public PostRunnable(int num, int b){
		a = num;
		this.b = b;
	}
	
	

}
