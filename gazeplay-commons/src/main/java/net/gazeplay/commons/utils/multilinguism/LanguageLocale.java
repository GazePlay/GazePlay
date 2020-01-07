package net.gazeplay.commons.utils.multilinguism;

import java.util.Locale;

public class LanguageLocale implements Comparable<LanguageLocale>{

    private Locale locale;

    public LanguageLocale(String language, String country) {
        locale = new Locale(language, country);
    }

    public LanguageLocale(String language) {
        locale = new Locale(language);
    }

    public String getLanguage(){
        return locale.getLanguage();
    }

    public String getCountry(){
        return locale.getCountry();
    }

    @Override
    public int compareTo(LanguageLocale languageLocale) {

        if(languageLocale == null) {return -1;}

        int compareLanguage = this.getLanguage().compareTo(languageLocale.getLanguage());
        if (compareLanguage !=0) {
            return compareLanguage;
        } else {
            return this.getCountry().compareTo(languageLocale.getCountry());
        }
    }

    @Override
    public boolean equals(Object o){
        if(o == null) {
            return false;
        }
        if(o.getClass() == this.getClass()){
            return compareTo((LanguageLocale) o) == 0;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode(){
        return (getLanguage()+getCountry()).hashCode();
    }
}
