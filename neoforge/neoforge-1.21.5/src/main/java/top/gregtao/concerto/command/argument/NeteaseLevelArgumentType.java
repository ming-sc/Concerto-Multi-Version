package top.gregtao.concerto.command.argument;//package top.gregtao.concerto.command.argument;
//
//import com.mojang.brigadier.context.CommandContext;
//import net.minecraft.commands.CommandSource;
//import net.minecraft.commands.arguments.StringRepresentableArgument;
//import top.gregtao.concerto.core.music.NeteaseCloudMusic;
//
//public class NeteaseLevelArgumentType extends StringRepresentableArgument<NeteaseCloudMusic.Level> {
//    private NeteaseLevelArgumentType() {
//        super(NeteaseCloudMusic.Level.CODEC, NeteaseCloudMusic.Level::values);
//    }
//
//    public static NeteaseLevelArgumentType level() {
//        return new NeteaseLevelArgumentType();
//    }
//
//    public static NeteaseCloudMusic.Level getOrderType(CommandContext<CommandSource> context, String id) {
//        try {
//            return context.getArgument(id, NeteaseCloudMusic.Level.class);
//        } catch (IllegalArgumentException e) {
//            return NeteaseCloudMusic.Level.HIRES;
//        }
//    }
//}
