package net.ccbluex.liquidbounce.chat

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER

class ClientHandler(val client: Client, private val handshaker: WebSocketClientHandshaker) : SimpleChannelInboundHandler<Any>() {

    lateinit var handshakeFuture: ChannelPromise

        override fun handlerAdded(ctx: ChannelHandlerContext) {
        handshakeFuture = ctx.newPromise()
    }

        override fun channelActive(ctx: ChannelHandlerContext) {
        handshaker.handshake(ctx.channel())
    }

        override fun channelInactive(ctx: ChannelHandlerContext) {
        client.onDisconnect()
        client.channel = null
        client.username = ""
        client.jwt = false
    }

        override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        LOGGER.error("LiquidChat error", cause)
        client.onError(cause)
        if (!handshakeFuture.isDone) handshakeFuture.setFailure(cause)
        ctx.close()
    }

        override fun channelRead0(ctx: ChannelHandlerContext, msg: Any) {
        val channel = ctx.channel()

        if (!handshaker.isHandshakeComplete) {
            try{
                handshaker.finishHandshake(channel, msg as FullHttpResponse)
                handshakeFuture.setSuccess()

            } catch (exception: WebSocketHandshakeException) {
                handshakeFuture.setFailure(exception)
            }

            client.onHandshake(handshakeFuture.isSuccess)
            return
        }

        when (msg) {
            is TextWebSocketFrame -> client.onMessage(msg.text())
            is CloseWebSocketFrame -> channel.close()
        }
    }
}