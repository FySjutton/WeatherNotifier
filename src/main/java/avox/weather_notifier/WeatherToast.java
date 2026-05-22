package avox.weather_notifier;

import avox.weather_notifier.config.WeatherConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class WeatherToast implements Toast {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("toast/advancement");
    private Toast.Visibility visibility = Visibility.SHOW;

    private final String title;
    private final String message;
    private final Identifier icon;

    public static final Identifier NOTIFICATION_SOUND_ID = Identifier.fromNamespaceAndPath("weather_notifier", "notification_sound");
    public static SoundEvent NOTIFICATION_SOUND_EVENT = SoundEvent.createVariableRangeEvent(NOTIFICATION_SOUND_ID);

    public WeatherToast(Minecraft client, WeatherTypes weather) {
        this.icon = Identifier.fromNamespaceAndPath("weather_notifier", "textures/gui/weather_icons/" + weather.name().toLowerCase() + ".png");
        this.title = (weather.equals(WeatherTypes.CLEAR) ? "§e" : "§b") + Component.translatable("weather_notifier.toast.title").getString();
        this.message = String.format(Component.translatable("weather_notifier.toast.message." + weather.name().toLowerCase()).getString());
        if (client.player != null && WeatherConfig.CONFIG.instance().useNotificationSound) {
            client.player.makeSound(NOTIFICATION_SOUND_EVENT);
        }
    }

    @Override
    public Visibility getWantedVisibility() {
        return visibility;
    }

    @Override
    public void update(ToastManager manager, long time) {
        visibility = time >= 5000 * manager.getNotificationDisplayTimeMultiplier()
            ? Visibility.HIDE
            : Visibility.SHOW;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long fullyVisibleForMs) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, width(), height());
        graphics.text(font, title, 30, 7, -256, false);
        graphics.text(font, message, 30, 18, -1, false);

        graphics.blit(RenderPipelines.GUI_TEXTURED, icon, 8, 8, 0, 0, 16, 16, 16, 16);
    }

    @Override
    public int width() {
        return 160;
    }

    @Override
    public int height() {
        return 32;
    }
}
