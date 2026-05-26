package avox.weather_notifier;

import avox.weather_notifier.config.WeatherConfig;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static avox.weather_notifier.WeatherToast.NOTIFICATION_SOUND_EVENT;
import static avox.weather_notifier.WeatherToast.NOTIFICATION_SOUND_ID;

public class WeatherNotifier implements ModInitializer {
	public static final String MOD_ID = "weather_notifier";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final long DAY_LENGTH = 24000L;
    private static final long NIGHT_START = 13000L;
    private static final long DAY_START = 23000L;

	private WeatherTypes lastWeather;
    private WeatherTypes lastTimeOfDay;
    private long lastToast = 0;

	@Override
	public void onInitialize() {
		WeatherConfig.CONFIG.load();

		Registry.register(BuiltInRegistries.SOUND_EVENT, NOTIFICATION_SOUND_ID, NOTIFICATION_SOUND_EVENT);

		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			ClientLevel world = client.level;
			if (world != null && client.player != null) {
				WeatherTypes currentWeather = detectWeather(world, client.player);

				if (lastWeather == null || !lastWeather.equals(currentWeather)) {
					if (lastWeather != null) {
						addToast(client, currentWeather);
					}
					lastWeather = currentWeather;
				}

                WeatherTypes currentTimeOfDay = detectTimeOfDay(world);
                if (lastTimeOfDay == null || !lastTimeOfDay.equals(currentTimeOfDay)) {
                    if (lastTimeOfDay != null) {
                        addToast(client, currentTimeOfDay);
                    }
                    lastTimeOfDay = currentTimeOfDay;
                }
			}
		});
	}

	private WeatherTypes detectWeather(ClientLevel world, LocalPlayer player) {
        if (!canNotify(world)) return lastWeather;

        if (world.isThundering()) return WeatherTypes.THUNDER;
		if (world.isRaining()) {
            BlockPos location = player.blockPosition();
            if (WeatherConfig.CONFIG.instance().useTopHeight) {
                location = world.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, location);
            }
			if (WeatherConfig.CONFIG.instance().snowNotification && world.precipitationAt(location) == Biome.Precipitation.SNOW) {
                return WeatherTypes.SNOW;
			}
			return WeatherTypes.RAIN;
		}
		return WeatherTypes.CLEAR;
	}

    private WeatherTypes detectTimeOfDay(ClientLevel world) {
        if (!canNotify(world)) return lastTimeOfDay;

        long timeOfDay = Math.floorMod(world.getDefaultClockTime(), DAY_LENGTH);
        return timeOfDay >= NIGHT_START && timeOfDay < DAY_START ? WeatherTypes.NIGHT : WeatherTypes.DAY;
    }

    private boolean canNotify(ClientLevel world) {
        if (lastToast + WeatherConfig.CONFIG.instance().cooldown > System.currentTimeMillis()) return false;
        return world.dimensionTypeRegistration().unwrapKey().isPresent()
            && List.of(BuiltinDimensionTypes.OVERWORLD, BuiltinDimensionTypes.OVERWORLD_CAVES).contains(world.dimensionTypeRegistration().unwrapKey().get());
    }

	private void addToast(Minecraft client, WeatherTypes weather) {
		WeatherConfig config = WeatherConfig.CONFIG.instance();
		if (
			(weather.equals(WeatherTypes.CLEAR) && config.clearNotification) ||
			(weather.equals(WeatherTypes.RAIN) && config.rainNotification) ||
            (weather.equals(WeatherTypes.THUNDER) && config.thunderNotification) ||
            (weather.equals(WeatherTypes.SNOW) && config.snowNotification) ||
            (weather.equals(WeatherTypes.DAY) && config.dayNotification) ||
            (weather.equals(WeatherTypes.NIGHT) && config.nightNotification)
		) {
            lastToast = System.currentTimeMillis();
			WeatherToast toast = new WeatherToast(client, weather);
			client.getToastManager().addToast(toast);
		}
	}
}
