package avox.weather_notifier;

import avox.weather_notifier.config.WeatherConfig;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionTypes;
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

		Registry.register(Registries.SOUND_EVENT, NOTIFICATION_SOUND_ID, NOTIFICATION_SOUND_EVENT);

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
        if (lastToast + WeatherConfig.CONFIG.instance().cooldown > System.currentTimeMillis()) return lastWeather;
        if (world.getDimensionEntry().getKey().isEmpty() || !List.of(DimensionTypes.OVERWORLD, DimensionTypes.OVERWORLD_CAVES).contains(world.getDimensionEntry().getKey().get())) return lastWeather;

        if (world.isThundering()) return WeatherTypes.THUNDER;
		if (world.isRaining()) {
            BlockPos location = player.getBlockPos();
            if (WeatherConfig.CONFIG.instance().useTopHeight) {
                location = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, location);
            }
			if (WeatherConfig.CONFIG.instance().snowNotification && world.getPrecipitation(location) == Biome.Precipitation.SNOW) {
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
            (weather.equals(WeatherTypes.THUNDER) && config.thunderNotification) ||
            (weather.equals(WeatherTypes.SNOW) && config.snowNotification)
		) {
            lastToast = System.currentTimeMillis();
			WeatherToast toast = new WeatherToast(client, weather);
			client.getToastManager().add(toast);
		}
	}
}