package avox.weather_notifier;

import avox.weather_notifier.config.WeatherConfig;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.biome.Biome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeatherNotifier implements ModInitializer {
	public static final String MOD_ID = "weather_notifier";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private WeatherTypes lastWeather;

	@Override
	public void onInitialize() {
		WeatherConfig.CONFIG.load();

		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			ClientWorld world = client.world;
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

	private WeatherTypes detectWeather(ClientWorld world, ClientPlayerEntity player) {
		if (world.isThundering()) return WeatherTypes.THUNDER;
		if (world.isRaining()) {
			if (world.getBiome(player.getBlockPos()).value().getPrecipitation(player.getBlockPos(), 63) == Biome.Precipitation.SNOW) {
				return WeatherTypes.SNOW;
			}
			return WeatherTypes.RAIN;
		}
		return WeatherTypes.CLEAR;
	}

	private void addToast(MinecraftClient client, WeatherTypes weather) {
		WeatherConfig config = WeatherConfig.CONFIG.instance();
		if (
			(weather.equals(WeatherTypes.CLEAR) && config.clearNotification) ||
			(weather.equals(WeatherTypes.RAIN) && config.rainNotification) ||
			(weather.equals(WeatherTypes.SNOW) && config.snowNotification) ||
			(weather.equals(WeatherTypes.THUNDER) && config.thunderNotification)
		) {
			WeatherToast toast = new WeatherToast(weather);
			client.getToastManager().add(toast);
		}
	}
}