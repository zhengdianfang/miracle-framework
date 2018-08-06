package com.zhengdianfang.miracleframework.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringExtension {

    public static boolean isValidateEmail(String email) {
        String regex = "^([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{1,6}))?$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isValidatePhone(String username) {
        return Phone.isPhone(username);
    }
}
