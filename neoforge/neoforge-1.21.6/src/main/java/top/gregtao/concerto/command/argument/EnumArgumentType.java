package top.gregtao.concerto.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import top.gregtao.concerto.core.api.SimpleStringIdentifiable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class EnumArgumentType<T extends SimpleStringIdentifiable> implements ArgumentType<T> {
    private static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType(
            (object) -> Component.translatableEscape("argument.enum.invalid", object)
    );
    private final Supplier<T[]> values;
    private final Function<String, T> nameLookup;

    public EnumArgumentType(Supplier<T[]> values, Function<String, T> nameLookup) {
        this.values = values;
        this.nameLookup = nameLookup;
    }

    @Override
    public T parse(StringReader reader) throws CommandSyntaxException {
        String s = reader.readUnquotedString();
        Optional<T> valueOpt = Optional.ofNullable(this.nameLookup.apply(s));
        return valueOpt.orElseThrow(() -> ERROR_INVALID_VALUE.createWithContext(reader, s));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(
                Arrays.stream(this.values.get())
                        .map(SimpleStringIdentifiable::getSerializedName)
                        .collect(Collectors.toList()),
                builder
        );
    }

    @Override
    public Collection<String> getExamples() {
        return Arrays.stream(this.values.get())
                .map(SimpleStringIdentifiable::getSerializedName)
                .collect(Collectors.toList());
    }

    public static <E extends Enum<E> & SimpleStringIdentifiable> EnumArgumentType<E> fromEnum(Supplier<E[]> values) {
        Map<String, E> map = Arrays.stream(values.get())
                .collect(Collectors.toMap(SimpleStringIdentifiable::getSerializedName, Function.identity()));
        return new EnumArgumentType<>(values, map::get);
    }
}
