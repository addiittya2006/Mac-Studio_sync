package se.popcorn_time.base.utils;

import android.content.res.Resources;

import java.util.Locale;

import se.popcorn_time.base.R;
import se.popcorn_time.base.prefs.PopcornPrefs;
import se.popcorn_time.base.prefs.Prefs;

public class InterfaceUtil {

    public static final String DEFAULT_INTERFACE_LOCALE = "en";

    private static String[] interfaceNative;
    private static String[] interfaceLocale;

    private static Locale appLocale;

    public static void init(Resources resources) {
        interfaceNative = resources.getStringArray(R.array.interface_native);
        interfaceLocale = resources.getStringArray(R.array.interface_locale);

        if (Prefs.getPopcornPrefs().contains(PopcornPrefs.APP_LOCALE)) {
            appLocale = createLocale(Prefs.getPopcornPrefs().get(PopcornPrefs.APP_LOCALE, DEFAULT_INTERFACE_LOCALE));
        } else {
            String locale = getInterfaceSupportedLocale();
            Prefs.getPopcornPrefs().put(PopcornPrefs.APP_LOCALE, locale);
            appLocale = createLocale(locale);
        }
    }

    public static String[] getInterfaceNative() {
        return interfaceNative;
    }

    public static String[] getInterfaceLocale() {
        return interfaceLocale;
    }

    public static Locale getAppLocale() {
        return appLocale;
    }

    public static void changeAppLocale(String locale) {
        if (appLocale.getLanguage().equals(locale)) {
            return;
        }
        appLocale = createLocale(locale);
        Prefs.getPopcornPrefs().put(PopcornPrefs.APP_LOCALE, locale);
        Logger.debug("Change locale to: " + appLocale.toString());
    }

    private static Locale createLocale(String locale) {
        String[] args = locale.split("_");
        if (args.length == 2) {
            return new Locale(args[0], args[1]);
        }
        return new Locale(locale);
    }

    private static String getInterfaceSupportedLocale() {
        String language = Locale.getDefault().getLanguage();
        String locale = Locale.getDefault().toString();
        for (String _locale : interfaceLocale) {
            if (_locale.equals(language) || _locale.equals(locale)) {
                return _locale;
            }
        }
        return DEFAULT_INTERFACE_LOCALE;
    }
}