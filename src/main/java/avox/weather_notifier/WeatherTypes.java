package avox.weather_notifier;

public enum WeatherTypes {
    CLEAR("clear"),
    RAIN("rain"),
    SNOW("snow"),
    THUNDER("thunder"),
    DAY("morning"),
    NIGHT("night");

    private final String iconName;

    WeatherTypes(String iconName) {
        this.iconName = iconName;
    }

    public String getIconName() {
        return iconName;
    }

    public boolean isTimeOfDay() {
        return this == DAY || this == NIGHT;
    }
}
