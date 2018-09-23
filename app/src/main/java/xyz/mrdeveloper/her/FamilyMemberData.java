package xyz.mrdeveloper.her;

/**
 * Created by Lakshay Raj on 24-11-2017.
 */

public class FamilyMemberData {
    private String name;
    private int countryCode;
    private String number;

    FamilyMemberData(String name, int countryCode, String number) {
        this.name = name;
        this.countryCode = countryCode;
        this.number = number;
    }

    FamilyMemberData() {
    }

    public String getName() {
        return name;
    }

    public int getCountryCode() {
        return countryCode;
    }

    public String getNumber() {
        return number;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCountryCode(int countryCode) {
        this.countryCode = countryCode;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
