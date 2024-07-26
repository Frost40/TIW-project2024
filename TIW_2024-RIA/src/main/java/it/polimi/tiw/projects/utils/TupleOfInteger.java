package it.polimi.tiw.projects.utils;

public class TupleOfInteger {
    private int key;
    private int value;
    private long valueLong;
    
    public TupleOfInteger() {}

    public TupleOfInteger(int key, int value) {
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public int getValue() {
        return value;
    }
    
    public void setKey(int key) {
    	this.key = key;
    }
    
    public void setValue(int value) {
    	this.value = value;
    }

	public long getValueLong() {
		return valueLong;
	}

	public void setValueLong(long valueLong) {
		this.valueLong = valueLong;
	}
}

