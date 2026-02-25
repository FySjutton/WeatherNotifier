package avox.weather_notifier.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class WeatherConfig {
    public static final ConfigClassHandler<WeatherConfig> CONFIG = ConfigClassHandler.createBuilder(WeatherConfig.class)
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("weather_notifier.json"))
                    .build())
            .build();

    // General
    @SerialEntry public boolean clearNotification = true;
    @SerialEntry public boolean rainNotification = true;
    @SerialEntry public boolean snowNotification = true;
    @SerialEntry public boolean thunderNotification = true;
    @SerialEntry public boolean useNotificationSound = true;

    @SerialEntry public int cooldown = 5000;
    @SerialEntry public boolean useTopHeight = true;

    public static Screen configScreen(Screen parent) {
        return YetAnotherConfigLib.create(CONFIG, ((defaults, config, builder) -> builder
                .title(Text.translatable("weather_notifier.config.title"))
                .category(ConfigCategory.createBuilder()
                    .name(Text.translatable("weather_notifier.config.category.general"))

                    .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("weather_notifier.config.option.clear_notification"))
                        .description(
                            OptionDescription.createBuilder()
                                .text(Text.translatable("weather_notifier.config.option.desc.clear_notification"))
                                .image(Identifier.of("weather_notifier", "textures/gui/weather_preview/clear.png"), 320, 64)
                                .build())
                        .binding(true, () -> config.clearNotification, newVal -> config.clearNotification = newVal)
                        .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                        .build())
                    .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("weather_notifier.config.option.rain_notification"))
                            .description(
                                OptionDescription.createBuilder()
                                    .text(Text.translatable("weather_notifier.config.option.desc.rain_notification"))
                                    .image(Identifier.of("weather_notifier", "textures/gui/weather_preview/rain.png"), 320, 64)
                                    .build())
                        .binding(true, () -> config.rainNotification, newVal -> config.rainNotification = newVal)
                        .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                        .build())
                    .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("weather_notifier.config.option.snow_notification"))
                            .description(
                                OptionDescription.createBuilder()
                                    .text(Text.translatable("weather_notifier.config.option.desc.snow_notification"))
                                    .image(Identifier.of("weather_notifier", "textures/gui/weather_preview/snow.png"), 320, 64)
                                    .build())
                        .binding(true, () -> config.snowNotification, newVal -> config.snowNotification = newVal)
                        .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                        .build())
                    .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("weather_notifier.config.option.thunder_notification"))
                            .description(
                                OptionDescription.createBuilder()
                                    .text(Text.translatable("weather_notifier.config.option.desc.thunder_notification"))
                                    .image(Identifier.of("weather_notifier", "textures/gui/weather_preview/thunder.png"), 320, 64)
                                    .build())
                        .binding(true, () -> config.thunderNotification, newVal -> config.thunderNotification = newVal)
                        .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                        .build())
                    .option(Option.<Boolean>createBuilder()
                            .name(Text.translatable("weather_notifier.config.option.use_notification_sound"))
                            .description(OptionDescription.of(Text.translatable("weather_notifier.config.option.desc.use_notification_sound")))
                            .binding(true, () -> config.useNotificationSound, newVal -> config.useNotificationSound = newVal)
                            .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                            .build())
                    .group(OptionGroup.createBuilder()
                            .name(Text.translatable("weather_notifier.config.group.other"))
                            .option(Option.<Integer>createBuilder()
                                    .name(Text.translatable("weather_notifier.config.option.cooldown"))
                                    .description(OptionDescription.of(Text.translatable("weather_notifier.config.option.desc.cooldown")))
                                    .binding(5000, () -> config.cooldown, newVal -> config.cooldown = newVal)
                                    .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                            .range(0, 20000)
                                            .step(500)
                                            .formatValue(val -> Text.of(val + "ms")))
                                    .build())
                            .option(Option.<Boolean>createBuilder()
                                    .name(Text.translatable("weather_notifier.config.option.use_top_height"))
                                    .description(OptionDescription.of(Text.translatable("weather_notifier.config.option.desc.use_top_height")))
                                    .binding(true, () -> config.useTopHeight, newVal -> config.useTopHeight = newVal)
                                    .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                                    .build())
                            .build())
                    .build())

        )).generateScreen(parent);
    }
}