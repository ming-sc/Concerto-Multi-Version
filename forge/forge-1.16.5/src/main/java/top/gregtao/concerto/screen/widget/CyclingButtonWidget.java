package top.gregtao.concerto.screen.widget;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

public class CyclingButtonWidget<T> extends AbstractButton {
    static final BooleanSupplier HAS_ALT_DOWN = Screen::hasAltDown;
    private final ITextComponent optionText;
    private int index;
    private T value;
    private final Values<T> values;
    private final Function<T, ITextComponent> valueToText;
    private final UpdateCallback<T> callback;
    private final boolean optionTextOmitted;

    CyclingButtonWidget(int x, int y, int width, int height, ITextComponent message, ITextComponent optionText, int index, T value, Values<T> values, Function<T, ITextComponent> valueToText, UpdateCallback<T> callback, boolean optionTextOmitted) {
        super(x, y, width, height, message);
        this.optionText = optionText;
        this.index = index;
        this.value = value;
        this.values = values;
        this.valueToText = valueToText;
        this.callback = callback;
        this.optionTextOmitted = optionTextOmitted;
    }

    public void onPress() {
        if (Screen.hasShiftDown()) {
            this.cycle(-1);
        } else {
            this.cycle(1);
        }

    }

    private void cycle(int amount) {
        List<T> list = this.values.getCurrent();
        this.index = MathHelper.positiveModulo(this.index + amount, list.size());
        T object = list.get(this.index);
        this.internalSetValue(object);
        this.callback.onValueChange(this, object);
    }

    private T getValue(int offset) {
        List<T> list = this.values.getCurrent();
        return list.get(MathHelper.positiveModulo(this.index + offset, list.size()));
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (amount > 0.0) {
            this.cycle(-1);
        } else if (amount < 0.0) {
            this.cycle(1);
        }

        return true;
    }

    public void setValue(T value) {
        List<T> list = this.values.getCurrent();
        int i = list.indexOf(value);
        if (i != -1) {
            this.index = i;
        }

        this.internalSetValue(value);
    }

    private void internalSetValue(T value) {
        ITextComponent text = this.composeText(value);
        this.setMessage(text);
        this.value = value;
    }

    private ITextComponent composeText(T value) {
        return this.optionTextOmitted ? this.valueToText.apply(value) : this.composeGenericOptionText(value);
    }

    private IFormattableTextComponent composeGenericOptionText(T value) {
        return new StringTextComponent(this.optionText.getString() + ": " + this.valueToText.apply(value).getString());
    }

    public T getValue() {
        return this.value;
    }

    public static <T> Builder<T> builder(Function<T, ITextComponent> valueToText) {
        return new Builder<>(valueToText);
    }

    @OnlyIn(Dist.CLIENT)
    interface Values<T> {
        List<T> getCurrent();

        List<T> getDefaults();

        static <T> Values<T> of(List<T> values) {
            final List<T> list = ImmutableList.copyOf(values);
            return new Values<>() {
                public List<T> getCurrent() {
                    return list;
                }

                public List<T> getDefaults() {
                    return list;
                }
            };
        }

        static <T> Values<T> of(final BooleanSupplier alternativeToggle, List<T> defaults, List<T> alternatives) {
            final List<T> list = ImmutableList.copyOf(defaults);
            final List<T> list2 = ImmutableList.copyOf(alternatives);
            return new Values<>() {
                public List<T> getCurrent() {
                    return alternativeToggle.getAsBoolean() ? list2 : list;
                }

                public List<T> getDefaults() {
                    return list;
                }
            };
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface UpdateCallback<T> {
        void onValueChange(CyclingButtonWidget<T> button, T value);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder<T> {
        private int initialIndex;
        @Nullable
        private T value;
        private final Function<T, ITextComponent> valueToText;
        private Values<T> values = CyclingButtonWidget.Values.of(ImmutableList.of());
        private boolean optionTextOmitted;

        public Builder(Function<T, ITextComponent> valueToText) {
            this.valueToText = valueToText;
        }

        public Builder<T> values(List<T> values) {
            this.values = CyclingButtonWidget.Values.of(values);
            return this;
        }

        @SafeVarargs
        public final Builder<T> values(T... values) {
            return this.values(ImmutableList.copyOf(values));
        }

        public Builder<T> values(List<T> defaults, List<T> alternatives) {
            this.values = CyclingButtonWidget.Values.of(CyclingButtonWidget.HAS_ALT_DOWN, defaults, alternatives);
            return this;
        }

        public Builder<T> values(BooleanSupplier alternativeToggle, List<T> defaults, List<T> alternatives) {
            this.values = CyclingButtonWidget.Values.of(alternativeToggle, defaults, alternatives);
            return this;
        }

        public Builder<T> initially(T value) {
            this.value = value;
            int i = this.values.getDefaults().indexOf(value);
            if (i != -1) {
                this.initialIndex = i;
            }

            return this;
        }

        public Builder<T> omitKeyText() {
            this.optionTextOmitted = true;
            return this;
        }

        public CyclingButtonWidget<T> build(int x, int y, int width, int height, ITextComponent optionText) {
            return this.build(x, y, width, height, optionText, (button, value) -> {});
        }

        public CyclingButtonWidget<T> build(int x, int y, int width, int height, ITextComponent optionText, UpdateCallback<T> callback) {
            List<T> list = this.values.getDefaults();
            if (list.isEmpty()) {
                throw new IllegalStateException("No values for cycle button");
            } else {
                T object = this.value != null ? this.value : list.get(this.initialIndex);
                ITextComponent text = this.valueToText.apply(object);
                ITextComponent text2 = this.optionTextOmitted ? text : new StringTextComponent(optionText.getString() + ": " + text.getString());
                return new CyclingButtonWidget<>(x, y, width, height, text2, optionText, this.initialIndex, object, this.values, this.valueToText, callback, this.optionTextOmitted);
            }
        }
    }
}