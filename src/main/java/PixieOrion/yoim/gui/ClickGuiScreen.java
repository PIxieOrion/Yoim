package PixieOrion.yoim.gui;

import PixieOrion.yoim.Yoim;
import PixieOrion.yoim.module.Category;
import PixieOrion.yoim.module.Module;
import PixieOrion.yoim.module.client.ClickGuiModule;
import PixieOrion.yoim.module.client.ColorsModule;
import PixieOrion.yoim.settings.Setting;
import PixieOrion.yoim.settings.impl.BindSetting;
import PixieOrion.yoim.settings.impl.BooleanSetting;
import PixieOrion.yoim.settings.impl.CategorySetting;
import PixieOrion.yoim.settings.impl.ColorSetting;
import PixieOrion.yoim.settings.impl.ModeSetting;
import PixieOrion.yoim.settings.impl.NumberSetting;
import PixieOrion.yoim.settings.impl.StringSetting;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public final class ClickGuiScreen extends Screen {
    private static final int FRAME_WIDTH = 100;
    private static final int HEADER_HEIGHT = 13;
    private static final int FRAME_GAP = 4;
    private static final int PADDING = 2;
    private static final int TOOLTIP_MAX_WIDTH = 220;
    private final List<Frame> frames = new ArrayList<>();

    public ClickGuiScreen() {
        super(Text.literal("yoim-click-gui"));
        int x = 6;
        for (Category category : Category.values()) {
            frames.add(new Frame(category, x, 3, FRAME_WIDTH, HEADER_HEIGHT));
            x += FRAME_WIDTH + FRAME_GAP;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x64000000);
        String description = null;
        for (Frame frame : frames) {
            frame.render(context, mouseX, mouseY, delta);
            if (description == null) description = frame.hoveredDescription(mouseX, mouseY);
        }
        if (description != null && !description.isBlank()) renderTooltip(context, description, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        for (Frame frame : frames) if (frame.mouseClicked(click.x(), click.y(), click.button())) return true;
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        for (Frame frame : frames) frame.mouseReleased(click.x(), click.y(), click.button());
        return true;
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        for (Frame frame : frames) frame.mouseDragged(click, offsetX, offsetY);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (Frame frame : frames) frame.mouseScrolled(mouseX, mouseY, verticalAmount);
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.isEscape()) {
            close();
            return true;
        }
        for (Frame frame : frames) frame.keyPressed(input);
        return true;
    }

    @Override
    public boolean charTyped(CharInput input) {
        for (Frame frame : frames) frame.charTyped(input);
        return true;
    }

    @Override
    public void close() {
        if (client != null) {
            ClickGuiModule module = Yoim.MODULES.get(ClickGuiModule.class);
            if (module != null) module.setEnabled(false);
            client.setScreen(null);
        }
    }

    @Override
    public boolean shouldPause() { return false; }

    public static int getButtonColor(int y, int alpha) {
        Color base = ColorsModule.isSyncEnabled() ? ColorsModule.getGlobalColor() : new Color(255, 215, 0, 255);
        int a = Math.max(0, Math.min(255, alpha));
        return (a << 24) | (base.getRed() << 16) | (base.getGreen() << 8) | base.getBlue();
    }

    private static int textWidth(String value) { return Yoim.mc().textRenderer.getWidth(value); }

    private static void text(DrawContext context, String value, int x, int y, int color) {
        context.drawTextWithShadow(Yoim.mc().textRenderer, Text.literal(value), x, y, color);
    }

    private static int darkBodyColor() {
        Color base = ColorsModule.isSyncEnabled() ? ColorsModule.getGlobalColor() : new Color(255, 215, 0, 255);
        return (100 << 24) | ((int) (base.getRed() * .3) << 16) | ((int) (base.getGreen() * .3) << 8) | (int) (base.getBlue() * .3);
    }

    private static String keyName(int key) {
        if (key == 0) return "None";
        if (key < 0) {
            return switch (-key - 1) {
                case 0 -> "M1";
                case 1 -> "M2";
                case 2 -> "M3";
                case 3 -> "M4";
                case 4 -> "M5";
                default -> "Mouse";
            };
        }
        String named = GLFW.glfwGetKeyName(key, 0);
        if (named != null) return named.toUpperCase();
        return switch (key) {
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "L-SHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "R-SHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "L-CTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "R-CTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "L-ALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "R-ALT";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_ESCAPE -> "ESC";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            default -> "KEY_" + key;
        };
    }

    private static boolean inside(double mx, double my, double left, double top, double right, double bottom) {
        return mx >= left && my >= top && mx < right && my < bottom;
    }

    private static void renderTooltip(DrawContext context, String value, int mouseX, int mouseY) {
        List<String> lines = wrap(value, TOOLTIP_MAX_WIDTH - 8);
        int textWidth = 0;
        for (String line : lines) textWidth = Math.max(textWidth, textWidth(line));
        int x = mouseX + 8;
        int y = mouseY + 8;
        if (x + textWidth > Yoim.mc().getWindow().getScaledWidth()) x = mouseX - textWidth - 8;
        if (y + lines.size() * 9 > Yoim.mc().getWindow().getScaledHeight()) y = mouseY - lines.size() * 9 - 8;
        x = Math.max(2, x);
        y = Math.max(2, y);
        int lineY = y;
        for (String line : lines) {
            text(context, line, x, lineY, 0xFFFFFFFF);
            lineY += 9;
        }
    }

    private static List<String> wrap(String value, int maxWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : value.split(" ")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (textWidth(candidate) <= maxWidth) {
                line.setLength(0);
                line.append(candidate);
            } else if (!line.isEmpty()) {
                lines.add(line.toString());
                line.setLength(0);
                line.append(word);
            } else {
                lines.add(word);
            }
        }
        if (!line.isEmpty()) lines.add(line.toString());
        return lines;
    }

    private abstract static class Button {
        int x;
        int y;
        int height;
        final Frame parent;
        final String description;
        final Setting setting;

        Button(Frame parent, int height, String description) { this(parent, height, description, null); }

        Button(Frame parent, int height, String description, Setting setting) {
            this.parent = parent;
            this.height = height;
            this.description = description;
            this.setting = setting;
        }

        int width() { return parent.width; }
        int parentHeight() { return parent.height; }
        int padding() { return 2; }
        int textPadding() { return 5; }
        int getHeight() { return height; }
        Setting setting() { return setting; }
        boolean visible() { return setting == null || setting.isVisible(); }
        boolean hover(double mx, double my) { return inside(mx, my, x + padding(), y, x + width() - padding(), y + parentHeight()); }
        boolean containsFull(double mx, double my) { return inside(mx, my, x + padding(), y, x + width() - padding(), y + getHeight()); }
        void render(DrawContext c, int mx, int my, float d) {}
        void click(double mx, double my, int button) {}
        void release(double mx, double my, int button) {}
        void drag(Click click, double dx, double dy) {}
        void key(KeyInput input) {}
        void charTyped(CharInput input) {}

        void playClickSound() {
            ClickGuiModule gui = Yoim.MODULES.get(ClickGuiModule.class);
            if (gui != null && gui.sounds.getValue()) {
                Yoim.mc().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            }
        }
    }

    private static final class Frame {
        final Category category;
        final List<ModuleButton> buttons = new ArrayList<>();
        int x;
        int y;
        int width;
        int height;
        int totalHeight;
        int dragX;
        int dragY;
        boolean open = true;
        boolean dragging;

        Frame(Category category, int x, int y, int width, int height) {
            this.category = category;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            for (Module module : Yoim.MODULES.byCategory(category)) {
                if (module instanceof ClickGuiModule) continue;
                buttons.add(new ModuleButton(module, this, height));
            }
        }

        void layout() {
            totalHeight = height;
            if (!open) return;
            totalHeight += 1;
            for (ModuleButton button : buttons) {
                button.layout(x, y + totalHeight);
                totalHeight += button.getHeight();
            }
        }

        void render(DrawContext context, int mouseX, int mouseY, float delta) {
            if (dragging) {
                x = mouseX - dragX;
                y = mouseY - dragY;
                clamp();
            }
            layout();
            context.fill(x, y, x + width, y + height, getButtonColor(y, 100));
            text(context, category.getName(), x + 3, y + 2, 0xFFFFFFFF);
            if (!open) return;
            context.fill(x, y + height, x + width, y + totalHeight + 1, darkBodyColor());
            for (ModuleButton button : buttons) button.render(context, mouseX, mouseY, delta);
        }

        boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (inside(mouseX, mouseY, x, y, x + width, y + height)) {
                if (button == 0) {
                    dragging = true;
                    dragX = (int) (mouseX - x);
                    dragY = (int) (mouseY - y);
                    return true;
                }
                if (button == 1) {
                    open = !open;
                    return true;
                }
            }
            if (open) for (ModuleButton moduleButton : buttons) if (moduleButton.clickAt(mouseX, mouseY, button)) return true;
            return false;
        }

        void mouseReleased(double mouseX, double mouseY, int button) {
            if (button == 0) dragging = false;
            for (ModuleButton moduleButton : buttons) moduleButton.release(mouseX, mouseY, button);
        }

        void mouseDragged(Click click, double dx, double dy) {
            for (ModuleButton moduleButton : buttons) moduleButton.drag(click, dx, dy);
        }

        void mouseScrolled(double mouseX, double mouseY, double amount) {
            if (x <= mouseX && mouseX < x + width) {
                ClickGuiModule gui = Yoim.MODULES.get(ClickGuiModule.class);
                int speed = gui == null ? 8 : gui.scrollSpeed.getValue().intValue();
                y += amount > 0 ? speed : -speed;
                clamp();
            }
        }

        void keyPressed(KeyInput input) {
            if (open) for (ModuleButton button : buttons) button.key(input);
        }

        void charTyped(CharInput input) {
            if (open) for (ModuleButton button : buttons) button.charTyped(input);
        }

        String hoveredDescription(double mouseX, double mouseY) {
            if (!open) return null;
            for (ModuleButton button : buttons) {
                String description = button.hoveredDescription(mouseX, mouseY);
                if (description != null) return description;
            }
            return null;
        }

        void clamp() {
            int screenWidth = Yoim.mc().getWindow().getScaledWidth();
            int screenHeight = Yoim.mc().getWindow().getScaledHeight();
            x = Math.max(2, Math.min(x, Math.max(2, screenWidth - width - 2)));
            y = Math.max(2, Math.min(y, Math.max(2, screenHeight - 20)));
        }
    }

    private static final class ModuleButton {
        final Module module;
        final Frame parent;
        final List<Button> buttons = new ArrayList<>();
        int x;
        int y;
        boolean open;

        ModuleButton(Module module, Frame parent, int height) {
            this.module = module;
            this.parent = parent;
            for (Setting setting : module.getSettings()) {
                if (setting instanceof BooleanSetting value) buttons.add(new BooleanButton(value, parent, height));
                else if (setting instanceof NumberSetting value) buttons.add(new NumberButton(value, parent, height));
                else if (setting instanceof ModeSetting value) buttons.add(new ModeButton(value, parent, height));
                else if (setting instanceof ColorSetting value) buttons.add(new ColorButton(value, parent, height));
                else if (setting instanceof StringSetting value) buttons.add(new StringButton(value, parent, height));
                else if (setting instanceof BindSetting value) buttons.add(new BindButton(value, parent, height, module));
                else if (setting instanceof CategorySetting value) buttons.add(new CategoryButton(value, parent, height));
            }
        }

        void layout(int x, int y) {
            this.x = x;
            this.y = y;
            if (!open) return;
            int cursor = y + parent.height;
            for (Button button : buttons) {
                if (button.setting() != null) button.setting().updateVisibility();
                if (!button.visible()) continue;
                button.x = x;
                button.y = cursor;
                cursor += button.getHeight();
            }
        }

        int getHeight() {
            int height = parent.height;
            if (open) for (Button button : buttons) if (button.visible()) height += button.getHeight();
            return height;
        }

        void render(DrawContext context, int mouseX, int mouseY, float delta) {
            int alpha = module.isEnabled() ? 80 : (hover(mouseX, mouseY) ? 45 : 30);
            context.fill(x + PADDING, y, x + parent.width - PADDING, y + parent.height - 1, getButtonColor(y, alpha));
            text(context, module.getName(), x + 5, y + 2, module.isEnabled() ? 0xFFFFFFFF : 0xFFAAAAAA);
            if (!open) return;
            for (Button button : buttons) {
                if (!button.visible()) continue;
                button.render(context, mouseX, mouseY, delta);
            }
        }

        boolean hover(double mouseX, double mouseY) {
            return inside(mouseX, mouseY, x + PADDING, y, x + parent.width - PADDING, y + parent.height);
        }

        boolean clickAt(double mouseX, double mouseY, int button) {
            if (open) {
                for (Button child : buttons) {
                    if (child.visible() && child.containsFull(mouseX, mouseY)) {
                        child.click(mouseX, mouseY, button);
                        return true;
                    }
                }
            }
            if (hover(mouseX, mouseY)) {
                if (button == 0) {
                    module.toggle();
                    return true;
                }
                if (button == 1) {
                    open = !open;
                    return true;
                }
            }
            return false;
        }

        void release(double mouseX, double mouseY, int button) {
            for (Button child : buttons) child.release(mouseX, mouseY, button);
        }

        void drag(Click click, double dx, double dy) {
            for (Button child : buttons) child.drag(click, dx, dy);
        }

        void key(KeyInput input) {
            if (open) for (Button child : buttons) if (child.visible()) child.key(input);
        }

        void charTyped(CharInput input) {
            if (open) for (Button child : buttons) if (child.visible()) child.charTyped(input);
        }

        String hoveredDescription(double mouseX, double mouseY) {
            if (hover(mouseX, mouseY)) return module.getDescription();
            if (open) for (Button child : buttons) if (child.visible() && child.containsFull(mouseX, mouseY)) return child.description;
            return null;
        }
    }

    private static final class BooleanButton extends Button {
        final BooleanSetting setting;

        BooleanButton(BooleanSetting setting, Frame parent, int height) {
            super(parent, height, setting.getDescription(), setting);
            this.setting = setting;
        }

        void render(DrawContext context, int mouseX, int mouseY, float delta) {
            if (setting.getValue()) context.fill(x + padding() + 1, y, x + width() - padding() - 1, y + height - 1, getButtonColor(y, 100));
            text(context, setting.getTag(), x + textPadding() + 1, y + 2, setting.getValue() ? 0xFFFFFFFF : 0xFFAAAAAA);
        }

        void click(double mouseX, double mouseY, int button) {
            if (button == 0 && hover(mouseX, mouseY)) {
                setting.toggle();
                playClickSound();
            }
        }
    }

    private static final class NumberButton extends Button {
        final NumberSetting setting;
        boolean dragging;
        boolean listening;
        boolean selecting;
        String current = "";

        NumberButton(NumberSetting setting, Frame parent, int height) {
            super(parent, height, setting.getDescription(), setting);
            this.setting = setting;
        }

        void render(DrawContext context, int mouseX, int mouseY, float delta) {
            double max = width() - 2 - padding() * 2.0;
            double pos = mouseX - x - 1 - padding();
            double ratio = switch (setting.getType()) {
                case LONG -> (setting.getValue().longValue() - setting.getMinimum().longValue()) / (double) (setting.getMaximum().longValue() - setting.getMinimum().longValue());
                case DOUBLE -> (setting.getValue().doubleValue() - setting.getMinimum().doubleValue()) / (setting.getMaximum().doubleValue() - setting.getMinimum().doubleValue());
                case FLOAT -> (setting.getValue().floatValue() - setting.getMinimum().floatValue()) / (setting.getMaximum().floatValue() - setting.getMinimum().floatValue());
                default -> (setting.getValue().intValue() - setting.getMinimum().intValue()) / (double) (setting.getMaximum().intValue() - setting.getMinimum().intValue());
            };
            if (dragging) {
                double t = Math.max(0, Math.min(max, pos)) / max;
                switch (setting.getType()) {
                    case LONG -> setting.setValue(Math.round(setting.getMinimum().longValue() + t * (setting.getMaximum().longValue() - setting.getMinimum().longValue())));
                    case DOUBLE -> setting.setValue(setting.getMinimum().doubleValue() + t * (setting.getMaximum().doubleValue() - setting.getMinimum().doubleValue()));
                    case FLOAT -> setting.setValue((float) (setting.getMinimum().floatValue() + t * (setting.getMaximum().floatValue() - setting.getMinimum().floatValue())));
                    default -> setting.setValue((int) Math.round(setting.getMinimum().intValue() + t * (setting.getMaximum().intValue() - setting.getMinimum().intValue())));
                }
                ratio = t;
            }
            context.fill(x + padding() + 1, y, x + padding() + 1 + (int) (max * ratio), y + height - 1, getButtonColor(y, 100));
            context.fill(x + padding() + 1, y, x + padding() + 2, y + height - 1, getButtonColor(y, 255));
            String left = listening ? current + (blink() ? "|" : "") : setting.getTag();
            text(context, left, x + textPadding() + 1, y + 2, 0xFFFFFFFF);
            if (!listening) {
                String value = String.valueOf(setting.getValue());
                text(context, value, x + width() - textPadding() - 1 - textWidth(value), y + 2, 0xFFAAAAAA);
            }
        }

        void click(double mouseX, double mouseY, int button) {
            if (button == 0 && hover(mouseX, mouseY)) dragging = true;
            if (button == 1 && hover(mouseX, mouseY) && !listening) {
                listening = true;
                current = "";
                selecting = false;
                playClickSound();
            } else if (button == 1 && !hover(mouseX, mouseY)) {
                listening = false;
                selecting = false;
            }
        }

        void release(double mouseX, double mouseY, int button) {
            if (button == 0) dragging = false;
        }

        void key(KeyInput input) {
            if (!listening) return;
            int key = input.getKeycode();
            if (input.isEscape()) {
                listening = false;
                selecting = false;
                return;
            }
            if (input.isEnter()) {
                try {
                    switch (setting.getType()) {
                        case LONG -> setting.setValue(Long.parseLong(current));
                        case DOUBLE -> setting.setValue(Double.parseDouble(current));
                        case FLOAT -> setting.setValue(Float.parseFloat(current));
                        default -> setting.setValue(Integer.parseInt(current));
                    }
                } catch (NumberFormatException ignored) {}
                listening = false;
                selecting = false;
                return;
            }
            if (key == GLFW.GLFW_KEY_BACKSPACE) {
                current = selecting ? "" : current.isEmpty() ? current : current.substring(0, current.length() - 1);
                selecting = false;
            }
            if (input.hasCtrlOrCmd() && key == GLFW.GLFW_KEY_A) selecting = true;
        }

        void charTyped(CharInput input) {
            if (!listening || !input.isValidChar()) return;
            String value = input.asString();
            current = selecting ? value : current + value;
            selecting = false;
        }

        boolean blink() { return (System.currentTimeMillis() / 450L) % 2 == 0; }
    }

    private static final class ModeButton extends Button {
        final ModeSetting setting;
        boolean open;

        ModeButton(ModeSetting setting, Frame parent, int height) {
            super(parent, height, setting.getDescription(), setting);
            this.setting = setting;
        }

        void render(DrawContext context, int mouseX, int mouseY, float delta) {
            context.fill(x + padding() + 1, y, x + width() - padding() - 1, y + parentHeight() - 1, getButtonColor(y, 100));
            context.fill(x + padding() + 1, y, x + padding() + 2, y + getHeight() - 1, getButtonColor(y, 255));
            text(context, setting.getTag(), x + textPadding() + 1, y + 2, 0xFFFFFFFF);
            String value = setting.getValue();
            text(context, value, x + width() - textPadding() - 1 - textWidth(value), y + 2, 0xFFAAAAAA);
            if (open) {
                int currentY = y + parentHeight();
                for (String mode : setting.getModes()) {
                    text(context, mode, x + textPadding() + 2, currentY + 2, setting.getValue().equals(mode) ? 0xFFFFFFFF : 0xFFAAAAAA);
                    currentY += parentHeight();
                }
            }
        }

        void click(double mouseX, double mouseY, int button) {
            if (hover(mouseX, mouseY)) {
                if (button == 0) {
                    int index = setting.getModes().indexOf(setting.getValue()) + 1;
                    if (index >= setting.getModes().size()) index = 0;
                    setting.setValue(setting.getModes().get(index));
                    playClickSound();
                } else if (button == 1) {
                    open = !open;
                    playClickSound();
                }
                return;
            }
            if (open && isModesHover(mouseX, mouseY) && button == 0) {
                int index = Math.max(0, Math.min(setting.getModes().size() - 1, (int) ((mouseY - y - parentHeight()) / parentHeight())));
                setting.setValue(setting.getModes().get(index));
            }
        }

        int getHeight() { return parentHeight() + (open ? parentHeight() * setting.getModes().size() : 0); }

        boolean hover(double mouseX, double mouseY) { return inside(mouseX, mouseY, x + padding(), y, x + width() - padding(), y + parentHeight()); }

        boolean isModesHover(double mouseX, double mouseY) {
            return inside(mouseX, mouseY, x + padding(), y + parentHeight(), x + width() - padding(), y + parentHeight() + parentHeight() * setting.getModes().size());
        }
    }

    private static final class ColorButton extends Button {
        final ColorSetting setting;
        boolean open;
        boolean hoveringHue;
        boolean hoveringColor;
        boolean hoveringAlpha;
        boolean hoveringSync;
        boolean hoveringRainbow;
        boolean draggingHue;
        boolean draggingColor;
        boolean draggingAlpha;
        float[] hsb;

        ColorButton(ColorSetting setting, Frame parent, int height) {
            super(parent, height, setting.getDescription(), setting);
            this.setting = setting;
            this.hsb = Color.RGBtoHSB(setting.getColor().getRed(), setting.getColor().getGreen(), setting.getColor().getBlue(), null);
        }

        void render(DrawContext context, int mouseX, int mouseY, float delta) {
            text(context, setting.getTag(), x + textPadding() + 1, y + 2, 0xFFFFFFFF);
            Color color = setting.getColor();
            context.fill(x + width() - padding() - 9, y + 2, x + width() - padding() - 1, y + parentHeight() - 3, 0xFF000000 | (color.getRGB() & 0xFFFFFF));
            if (!open) return;
            int offset = parentHeight();
            int size = 84;
            int dragX = clamp(mouseX - x - padding() - 1, 0, size);
            int dragY = clamp(mouseY - y - offset, 0, size);
            float dragHue = size * hsb[0];
            for (int i = 0; i < size; i++) {
                context.fill(x + width() - padding() - 9, y + offset + i, x + width() - padding() - 1, y + offset + i + 1, 0xFF000000 | (Color.HSBtoRGB(i / (float) size, 1f, 1f) & 0xFFFFFF));
            }
            hoveringHue = inside(mouseX, mouseY, x + width() - padding() - 9, y + offset, x + width() - padding() - 1, y + offset + size);
            context.fill(x + width() - padding() - 10, y + offset + (int) dragHue - 1, x + width() - padding(), y + offset + (int) dragHue + 2, 0xFF000000);
            if (draggingHue) {
                hsb[0] = dragY / (float) size;
                setColor(hsb, setting.getAlpha());
            }
            Color real = Color.getHSBColor(hsb[0], 1, 1);
            for (int i = 0; i < size; i++) {
                float saturation = i / (float) size;
                Color line = new Color(
                    (int) (255 * (1 - saturation) + real.getRed() * saturation),
                    (int) (255 * (1 - saturation) + real.getGreen() * saturation),
                    (int) (255 * (1 - saturation) + real.getBlue() * saturation)
                );
                context.fill(x + padding() + 1, y + offset + i, x + padding() + 1 + size, y + offset + i + 1, 0xFF000000 | (line.getRGB() & 0xFFFFFF));
            }
            for (int i = 0; i < size; i++) {
                int alpha = (int) (255 * (i / (float) size));
                context.fill(x + padding() + 1, y + offset + i, x + padding() + 1 + size, y + offset + i + 1, alpha << 24);
            }
            hoveringColor = inside(mouseX, mouseY, x + padding() + 1, y + offset, x + padding() + 1 + size, y + offset + size);
            if (draggingColor) {
                hsb[1] = dragX / (float) size;
                hsb[2] = 1f - dragY / (float) size;
                setColor(hsb, setting.getAlpha());
            }
            offset += size + 2;
            for (int i = 0; i < size; i++) {
                context.fill(x + padding() + 1 + i, y + offset, x + padding() + 2 + i, y + offset + 8, (0xFF << 24) | (color.getRGB() & 0xFFFFFF));
            }
            hoveringAlpha = inside(mouseX, mouseY, x + padding() + 1, y + offset, x + padding() + 1 + size, y + offset + 8);
            if (draggingAlpha) setColor(hsb, (int) (255 * dragX / (float) size));
            offset += 10;
            renderToggle(context, "Sync", x + padding() + 1, x + width() - padding() - 1, y + offset, setting.isSync());
            hoveringSync = inside(mouseX, mouseY, x + padding() + 1, y + offset, x + width() - padding() - 1, y + offset + parentHeight());
            offset += parentHeight() + 1;
            renderToggle(context, "Rainbow", x + padding() + 1, x + width() - padding() - 1, y + offset, setting.isRainbow());
            hoveringRainbow = inside(mouseX, mouseY, x + padding() + 1, y + offset, x + width() - padding() - 1, y + offset + parentHeight());
        }

        void renderToggle(DrawContext context, String label, float left, float right, int top, boolean active) {
            if (active) context.fill((int) left, top, (int) right, top + parentHeight(), getButtonColor(y, 100));
            text(context, label, (int) (x + width() / 2f - textWidth(label) / 2f), top + 2, 0xFFFFFFFF);
        }

        void click(double mouseX, double mouseY, int button) {
            if (hover(mouseX, mouseY) && button == 1) {
                open = !open;
                playClickSound();
                return;
            }
            if (button != 0 || !open) return;
            if (hoveringHue) draggingHue = true;
            if (hoveringColor) draggingColor = true;
            if (hoveringAlpha) draggingAlpha = true;
            if (hoveringSync) {
                setting.setSync(!setting.isSync());
                playClickSound();
            }
            if (hoveringRainbow) {
                setting.setRainbow(!setting.isRainbow());
                playClickSound();
            }
        }

        void release(double mouseX, double mouseY, int button) {
            draggingHue = false;
            draggingColor = false;
            draggingAlpha = false;
        }

        int getHeight() { return open ? 136 : parentHeight(); }

        boolean hover(double mouseX, double mouseY) {
            return inside(mouseX, mouseY, x + padding(), y, x + width() - padding(), y + parentHeight());
        }

        private void setColor(float[] hsb, int alpha) {
            Color color = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
            setting.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        }

        private int clamp(int value, int min, int max) {
            return Math.max(min, Math.min(max, value));
        }
    }

    private static final class StringButton extends Button {
        final StringSetting setting;
        boolean listening;
        boolean selecting;
        int cursor;
        String current = "";

        StringButton(StringSetting setting, Frame parent, int height) {
            super(parent, height, setting.getDescription(), setting);
            this.setting = setting;
        }

        void render(DrawContext context, int mouseX, int mouseY, float delta) {
            context.fill(x + padding() + 1, y, x + width() - padding() - 1, y + getHeight() - 1, getButtonColor(y, 100));
            String output;
            if (listening) {
                int index = Math.max(0, Math.min(cursor, current.length()));
                output = current.substring(0, index) + (blink() ? "|" : " ") + current.substring(index);
            } else {
                output = setting.getTag() + " §7" + setting.getValue();
            }
            text(context, output, x + textPadding() + 1, y + 2, 0xFFFFFFFF);
        }

        void click(double mouseX, double mouseY, int button) {
            if (button != 0) return;
            if (hover(mouseX, mouseY) && !listening) {
                listening = true;
                current = setting.getValue();
                cursor = current.length();
                selecting = false;
            } else if (!hover(mouseX, mouseY)) {
                listening = false;
                selecting = false;
            }
        }

        void key(KeyInput input) {
            if (!listening) return;
            int key = input.getKeycode();
            if (input.isEscape()) {
                listening = false;
                return;
            }
            if (input.isEnter()) {
                setting.setValue(current);
                listening = false;
                selecting = false;
                return;
            }
            if (key == GLFW.GLFW_KEY_LEFT) {
                if (selecting) {
                    selecting = false;
                    cursor = 0;
                } else cursor = Math.max(0, cursor - 1);
                return;
            }
            if (key == GLFW.GLFW_KEY_RIGHT) {
                if (selecting) {
                    selecting = false;
                    cursor = current.length();
                } else cursor = Math.min(current.length(), cursor + 1);
                return;
            }
            if (key == GLFW.GLFW_KEY_BACKSPACE) {
                if (selecting) {
                    current = "";
                    cursor = 0;
                    selecting = false;
                } else if (cursor > 0) {
                    current = current.substring(0, cursor - 1) + current.substring(cursor);
                    cursor--;
                }
                return;
            }
            if (key == GLFW.GLFW_KEY_DELETE) {
                if (selecting) {
                    current = "";
                    cursor = 0;
                    selecting = false;
                } else if (cursor < current.length()) {
                    current = current.substring(0, cursor) + current.substring(cursor + 1);
                }
                return;
            }
            if (input.hasCtrlOrCmd() && key == GLFW.GLFW_KEY_V) {
                String clipboard = Yoim.mc().keyboard.getClipboard();
                if (selecting) {
                    current = clipboard;
                    cursor = current.length();
                    selecting = false;
                } else {
                    current = current.substring(0, cursor) + clipboard + current.substring(cursor);
                    cursor += clipboard.length();
                }
                return;
            }
            if (input.hasCtrlOrCmd() && key == GLFW.GLFW_KEY_C && selecting) {
                Yoim.mc().keyboard.setClipboard(current);
                return;
            }
            if (input.hasCtrlOrCmd() && key == GLFW.GLFW_KEY_A) selecting = !current.isEmpty();
        }

        void charTyped(CharInput input) {
            if (!listening || !input.isValidChar()) return;
            String value = input.asString();
            if (selecting) {
                current = value;
                cursor = current.length();
                selecting = false;
            } else {
                current = current.substring(0, cursor) + value + current.substring(cursor);
                cursor += value.length();
            }
        }

        boolean blink() { return (System.currentTimeMillis() / 450L) % 2 == 0; }
    }

    private static final class BindButton extends Button {
        final BindSetting setting;
        final Module module;
        boolean listening;

        BindButton(BindSetting setting, Frame parent, int height, Module module) {
            super(parent, height, setting.getDescription(), setting);
            this.setting = setting;
            this.module = module;
        }

        void render(DrawContext context, int mouseX, int mouseY, float delta) {
            text(context, setting.getTag(), x + textPadding() + 1, y + 2, 0xFFFFFFFF);
            String bind = listening ? "..." : keyName(setting.getValue());
            text(context, bind, x + width() - textPadding() - 1 - textWidth(bind), y + 2, 0xFFAAAAAA);
        }

        void click(double mouseX, double mouseY, int button) {
            if (!hover(mouseX, mouseY)) return;
            if (!listening) {
                if (button == 0) {
                    listening = true;
                    playClickSound();
                } else if (button == 1) {
                    setting.setValue(0);
                    module.setBind(0);
                    playClickSound();
                }
            } else if (button >= 1) {
                int value = -button - 1;
                setting.setValue(value);
                module.setBind(value);
                listening = false;
            }
        }

        void key(KeyInput input) {
            if (!listening) return;
            int key = input.getKeycode();
            if (input.isEscape() || key == GLFW.GLFW_KEY_DELETE) {
                setting.setValue(0);
                module.setBind(0);
            } else {
                setting.setValue(key);
                module.setBind(key);
            }
            listening = false;
        }
    }

    private static final class CategoryButton extends Button {
        final CategorySetting setting;

        CategoryButton(CategorySetting setting, Frame parent, int height) {
            super(parent, height, setting.getDescription(), setting);
            this.setting = setting;
        }

        void render(DrawContext context, int mouseX, int mouseY, float delta) {
            text(context, setting.getTag(), x + textPadding() + 1, y + 2, 0xFFFFFFFF);
            String sign = setting.isOpen() ? "-" : "+";
            text(context, sign, x + width() - textPadding() - 1 - textWidth(sign), y + 2, 0xFFFFFFFF);
        }

        void click(double mouseX, double mouseY, int button) {
            if (hover(mouseX, mouseY) && button == 1) setting.setOpen(!setting.isOpen());
        }
    }
}
