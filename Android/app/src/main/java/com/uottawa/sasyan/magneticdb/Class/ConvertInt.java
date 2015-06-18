package com.uottawa.sasyan.magneticdb.Class;

/**
 * Created by valentin on 18/06/2015.
 */
public class ConvertInt {
    int value;

    public ConvertInt(String number, int defaultVal) {
        try {
            value = Integer.parseInt(number);
        } catch (NumberFormatException e) {
            value = defaultVal;
        }
    }

    public ConvertInt(int value) {
        setValue(value);
    }

    public int getValue() {
        return value;
    }

    public String toString() {
        return String.valueOf(getValue());
    }

    public void setValue(int value) {
        this.value = value;
    }
}
