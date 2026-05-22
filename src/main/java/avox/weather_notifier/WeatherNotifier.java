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

	private WeatherTypes lastWeather;
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
			}
		});
	}

	private WeatherTypes detectWeather(ClientLevel world, LocalPlayer player) {
        if (lastToast + WeatherConfig.CONFIG.instance().cooldown > System.currentTimeMillis()) return lastWeather;
        if (world.dimensionTypeRegistration().unwrapKey().isEmpty() || !List.of(BuiltinDimensionTypes.OVERWORLD, BuiltinDimensionTypes.OVERWORLD_CAVES).contains(world.dimensionTypeRegistration().unwrapKey().get())) return lastWeather;

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

	private void addToast(Minecraft client, WeatherTypes weather) {
		WeatherConfig config = WeatherConfig.CONFIG.instance();
		if (
			(weather.equals(WeatherTypes.CLEAR) && config.clearNotification) ||
			(weather.equals(WeatherTypes.RAIN) && config.rainNotification) ||
            (weather.equals(WeatherTypes.THUNDER) && config.thunderNotification) ||
            (weather.equals(WeatherTypes.SNOW) && config.snowNotification)
		) {
            lastToast = System.currentTimeMillis();
			WeatherToast toast = new WeatherToast(client, weather);
			client.getToastManager().addToast(toast);
		}
	}
}