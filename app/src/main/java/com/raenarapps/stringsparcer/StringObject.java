package com.raenarapps.stringsparcer;

public class StringObject {
    private String key;
    private String value;
//    private String valueNew;
    private String os;
    private boolean isTranslatable;

    public StringObject() {
    }

    public StringObject(String key, String value, String os) {
        this.key = key;
        this.value = value;
        this.os = os;
        this.isTranslatable = true;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

//    public String getValueNew() {
//        return valueNew;
//    }
//
//    public void setValueNew(String valueNew) {
//        this.valueNew = valueNew;
//    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public boolean isTranslatable() {
        return isTranslatable;
    }

    public void setIsTranslatable(boolean isTranslatable) {
        this.isTranslatable = isTranslatable;
    }
}
