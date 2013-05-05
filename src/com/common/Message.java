package com.common;

import java.io.Serializable;

public class Message implements Serializable {
	public String type;
	public String[] values;

	public Message(String type, String[] values) {
		this.type = type;
		this.values = values;
	}

	public String toString() {
		return "Type: " + type + "\n" + values;
	}
}
