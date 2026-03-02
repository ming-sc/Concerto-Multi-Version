package top.gregtao.concerto.network;

import net.minecraft.network.FriendlyByteBuf;

public class ConcertoPayload {
    public String string;
    public Channel channel;

    public ConcertoPayload(Channel channel, String s) {
        this.channel = channel;
        this.string = s;
    }

    public static ConcertoPayload decode(FriendlyByteBuf buf) {
        String s = buf.readUtf(Integer.MAX_VALUE);
        Channel channel1 = Channel.getById(s.charAt(0));
        return new ConcertoPayload(channel1, s.substring(1));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(channel.id + string, Integer.MAX_VALUE);
    }

    public enum Channel {
        MUSIC_DATA('0'),
        HANDSHAKE('1'),
        AUDITION_SYNC('2'),
        MUSIC_ROOM('3'),
        PRESET_RADIOS('4'),
        MUSIC_AGENT('5');

        public static Channel getById(char id) {
            for (Channel channel1 : values()) {
                if (channel1.id == id) {
                    return channel1;
                }
            }
            return MUSIC_DATA;
        }

        public final char id;
        Channel(char id) {
            this.id = id;
        }
    }
}
