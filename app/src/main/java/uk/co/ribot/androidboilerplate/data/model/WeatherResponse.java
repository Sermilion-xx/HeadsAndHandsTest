package uk.co.ribot.androidboilerplate.data.model;

import java.util.List;

/**
 * ---------------------------------------------------
 * Created by Sermilion on 23/06/2017.
 * Project: android-boilerplate-master
 * ---------------------------------------------------
 * <a href="http://www.ucomplex.org">www.ucomplex.org</a>
 * <a href="http://www.github.com/sermilion>github</a>
 * ---------------------------------------------------
 */

public final class WeatherResponse {
    private final Main main;

    public WeatherResponse() {
        this.main = null;
    }

    public int getWeather() {
        return main.getTemp();
    }

    public static final class Main {
        private final int temp;

        public Main() {
            this.temp = -1;
        }

        public int getTemp() {
            return temp;
        }
    }



}
