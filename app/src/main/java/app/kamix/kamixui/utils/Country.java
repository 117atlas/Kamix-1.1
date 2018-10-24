package app.kamix.kamixui.utils;

import java.util.ArrayList;

public class Country {
    public String code;
    public int flag;

    public Country (int f,String c){
        this.code=c;
        this.flag=f;
    }

    public int getFlag() {
        return flag;
    }

    public String getCode() {

        return code;
    }

    public static int indexInCountriesList(ArrayList<Country> countries, String code){
        if (countries!=null && countries.size()>0){
            for (int i=0; i<countries.size(); i++){
                if (countries.get(i).code.equals(code)) return i;
            }
        }
        return -1;
    }
}
