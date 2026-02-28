package top.gregtao.concerto.command.argument;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import top.gregtao.concerto.core.enums.OrderType;

public class OrderTypeArgumentType {

    public OrderTypeArgumentType() {
    }

    public static EnumArgumentType<OrderType> orderType() {
        return EnumArgumentType.fromEnum(OrderType::values);
    }

    public static OrderType getOrderType(CommandContext<CommandSourceStack> context, String id) {
        return context.getArgument(id, OrderType.class);
    }
}
