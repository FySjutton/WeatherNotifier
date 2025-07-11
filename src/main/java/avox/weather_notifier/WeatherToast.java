package avox.weather_notifier;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class WeatherToast implements Toast {
    private static final Identifier TEXTURE = Identifier.ofVanilla("toast/advancement");

    private final String title;
    private final String message;
    private final Identifier icon;

    private final TextRenderer textRenderer;

    public WeatherToast(WeatherTypes weather) {
        this.icon = Identifier.of("weather_notifier", "textures/gui/weather_icons/" + weather.name().toLowerCase() + ".png");
        this.title = (weather.equals(WeatherTypes.CLEAR) ? "§e" : "§b") + Text.translatable("weather_notifier.toast.title").getString();
        this.message = String.format(Text.translatable("weather_notifier.toast.message." + weather.name().toLowerCase()).getString());
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
    }

    @Override
    public Visibility draw(DrawContext context, ToastManager manager, long startTime) {
        context.drawGuiTexture(TEXTURE, 0, 0, getWidth(), getHeight());
        context.drawText(textRenderer, title, 30, 7, -256, false);
        context.drawText(textRenderer, message, 30, 18, -1, false);

        context.drawTexture(icon, 8, 8, 0, 0, 16, 16, 16, 16);

        return startTime >= 5000 * manager.getNotificationDisplayTimeMultiplier()
            ? Visibility.HIDE
            : Visibility.SHOW;
    }

    @Override
    public int getWidth() {
        return 160;
    }

    @Override
    public int getHeight() {
        return 32;
    }
}
