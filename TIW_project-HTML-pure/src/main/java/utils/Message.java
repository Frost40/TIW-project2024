package utils;

import java.util.ArrayList;
import java.util.List;

public class Message {
	private boolean isTrue;
	private List<String> info;
	private String result;
	private String text;
	
	public Message () {
		info = new ArrayList<>();
	}
	
	public List<String> getInfo () {
		return info;
	}
	public void setInfo(String message) {
		info.add(message);
	}
	
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public boolean isTrue() {
		return isTrue;
	}
	public void setIsTrue(boolean switcher) {
		this.isTrue = switcher;
	}
}
