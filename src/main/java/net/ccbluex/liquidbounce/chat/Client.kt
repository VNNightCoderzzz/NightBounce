package net.ccbluex.liquidbounce.chat

import com.google.gson.GsonBuilder
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.DefaultHttpHeaders
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory
import io.netty.handler.codec.http.websocketx.WebSocketVersion
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import net.ccbluex.liquidbounce.chat.packet.PacketDeserializer
import net.ccbluex.liquidbounce.chat.packet.PacketSerializer
import net.ccbluex.liquidbounce.chat.packet.packets.*
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.login.UserUtils
import java.net.URI
import java.util.*

abstract class Client : ClientListener, MinecraftInstance {

    internal var channel: Channel? = null
    var username = ""
    var jwt = false
    var loggedIn = false

    private val serializer = PacketSerializer()
    private val deserializer = PacketDeserializer()

    init {
        serializer.registerPacket("RequestMojangInfo", ServerRequestMojangInfoPacket::class.java)
        serializer.registerPacket("LoginMojang", ServerLoginMojangPacket::class.java)
        serializer.registerPacket("Message", ServerMessagePacket::class.java)
        serializer.registerPacket("PrivateMessage", ServerPrivateMessagePacket::class.java)
        serializer.registerPacket("BanUser", ServerBanUserPacket::class.java)
        serializer.registerPacket("UnbanUser", ServerUnbanUserPacket::class.java)
        serializer.registerPacket("RequestJWT", ServerRequestJWTPacket::class.java)
        serializer.registerPacket("LoginJWT", ServerLoginJWTPacket::class.java)


        deserializer.registerPacket("MojangInfo", ClientMojangInfoPacket::class.java)
        deserializer.registerPacket("NewJWT", ClientNewJWTPacket::class.java)
        deserializer.registerPacket("Message", ClientMessagePacket::class.java)
        deserializer.registerPacket("PrivateMessage", ClientPrivateMessagePacket::class.java)
        deserializer.registerPacket("Error", ClientErrorPacket::class.java)
        deserializer.registerPacket("Success", ClientSuccessPacket::class.java)
    }

        fun connect() {
        onConnect()

        val uri = URI("wss://chat.liquidbounce.net:7886/ws")

        val ssl = uri.scheme.equals("wss", true)
        val sslContext = if (ssl) SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE) else null

        val group = NioEventLoopGroup()
        val handler = ClientHandler(this, WebSocketClientHandshakerFactory.newHandshaker(
                uri, WebSocketVersion.V13, null,
                true, DefaultHttpHeaders()))

        val bootstrap = Bootstrap()

        bootstrap.group(group)
                .channel(NioSocketChannel::class.java)
                .handler(object : ChannelInitializer<SocketChannel>() {

                                        override fun initChannel(ch: SocketChannel) {
                        val pipeline = ch.pipeline()

                        if (sslContext != null) pipeline.addLast(sslContext.newHandler(ch.alloc()))

                        pipeline.addLast(HttpClientCodec(), HttpObjectAggregator(8192), handler)
                    }

                })

        channel = bootstrap.connect(uri.host, uri.port).sync().channel()
        handler.handshakeFuture.sync()

        if (isConnected()) onConnected()
    }

        fun disconnect() {
        channel?.close()
        channel = null
        username = ""
        jwt = false
    }

        fun loginMojang() = sendPacket(ServerRequestMojangInfoPacket())

        fun loginJWT(token: String) {
        onLogon()
        sendPacket(ServerLoginJWTPacket(token, allowMessages = true))
        jwt = true
    }

    fun isConnected() = channel != null && channel!!.isOpen

        internal fun onMessage(message: String) {
        val gson = GsonBuilder()
                .registerTypeAdapter(Packet::class.java, deserializer)
                .create()

        val packet = gson.fromJson(message, Packet::class.java)

        if (packet is ClientMojangInfoPacket) {
            onLogon()

            try {
                val sessionHash = packet.sessionHash

                mc.sessionService.joinServer(mc.session.profile, mc.session.token, sessionHash)
                username = mc.session.username
                jwt = false

                sendPacket(ServerLoginMojangPacket(mc.session.username, mc.session.profile.id, allowMessages = true))
            } catch (throwable: Throwable) {
                onError(throwable)
            }
            return
        }

        onPacket(packet)
    }

        fun sendPacket(packet: Packet) {
        val gson = GsonBuilder()
                .registerTypeAdapter(Packet::class.java, serializer)
                .create()

        channel?.writeAndFlush(TextWebSocketFrame(gson.toJson(packet, Packet::class.java)))
    }

        fun sendMessage(message: String) = sendPacket(ServerMessagePacket(message))

        fun sendPrivateMessage(username: String, message: String) = sendPacket(ServerPrivateMessagePacket(username, message))

        fun banUser(target: String) = sendPacket(ServerBanUserPacket(toUUID(target)))

        fun unbanUser(target: String) = sendPacket(ServerUnbanUserPacket(toUUID(target)))

        private fun toUUID(target: String): String {
        return try {
            UUID.fromString(target)

            target
        } catch (_: IllegalArgumentException) {
            val incomingUUID = UserUtils.getUUID(target)

            if (incomingUUID.isNullOrBlank()) return ""

            val uuid = StringBuilder(incomingUUID)
                    .insert(20, '-')
                    .insert(16, '-')
                    .insert(12, '-')
                    .insert(8, '-')

            uuid.toString()
        }
    }
}
