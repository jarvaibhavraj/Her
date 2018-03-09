package xyz.mrdeveloper.her;

/**
 * Created by Lakshay Raj on 24-11-2017.
 */

public class LovedOnes {
    private String name;
    private String number;

    LovedOnes(String name, String number) {
        this.name = name;
        this.number = number;
    }

    LovedOnes() {
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
