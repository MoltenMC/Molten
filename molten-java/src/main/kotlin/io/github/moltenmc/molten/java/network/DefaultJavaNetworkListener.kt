package io.github.moltenmc.molten.java.network

import io.github.moltenmc.molten.java.network.codec.JavaPacketDecoder
import io.github.moltenmc.molten.java.network.codec.JavaPacketEncoder
import io.github.moltenmc.molten.java.network.codec.JavaVarIntFrameDecoder
import io.github.moltenmc.molten.java.network.codec.JavaVarIntFrameEncoder
import io.github.moltenmc.molten.java.network.handler.JavaConfigurationFinishHandler
import io.github.moltenmc.molten.java.network.handler.JavaHandshakeStateHandler
import io.github.moltenmc.molten.java.network.handler.JavaIntentQueueHandler
import io.github.moltenmc.molten.java.network.handler.JavaLoginStartHandler
import io.github.moltenmc.molten.java.network.handler.JavaNetworkExceptionHandler
import io.github.moltenmc.molten.java.network.handler.JavaOutboundFlushHandler
import io.github.moltenmc.molten.java.network.handler.JavaSessionTickHandler
import io.github.moltenmc.molten.java.network.handler.JavaStatusRequestHandler
import io.github.moltenmc.molten.java.network.session.JavaSessionHolder
import io.github.moltenmc.molten.java.network.session.JavaSessionTickRegistry
import io.netty5.bootstrap.ServerBootstrap
import io.netty5.channel.Channel
import io.netty5.channel.ChannelInitializer
import io.netty5.channel.EventLoopGroup
import io.netty5.channel.MultithreadEventLoopGroup
import io.netty5.channel.nio.NioHandler
import io.netty5.channel.socket.SocketChannel
import io.netty5.channel.socket.nio.NioServerSocketChannel
import io.netty5.util.concurrent.Future
import java.net.InetSocketAddress
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class DefaultJavaNetworkListener(
    private val bindTimeoutSeconds: Long = 10,
    private val tickRegistry: JavaSessionTickRegistry = JavaSessionTickRegistry(),
) : JavaNetworkListener {
    private val boundRef = AtomicBoolean(false)

    @Volatile
    private var bossGroup: EventLoopGroup? = null

    @Volatile
    private var workerGroup: EventLoopGroup? = null

    @Volatile
    private var serverChannel: Channel? = null

    val isBound: Boolean
        get() = boundRef.get()

    override val localAddress: InetSocketAddress?
        get() = serverChannel?.localAddress() as? InetSocketAddress

    override fun bind(host: String, port: Int) {
        require(host.isNotBlank()) { "Bind host is required." }
        require(port in PORT_RANGE) { "Bind port is out of range." }
        check(boundRef.compareAndSet(false, true)) { "Java network listener is already bound." }

        val boss = eventLoopGroup("molten-java-boss", threads = 1)
        val worker = eventLoopGroup("molten-java-worker")
        try {
            val channel = ServerBootstrap()
                .group(boss, worker)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(
                    object : ChannelInitializer<SocketChannel>() {
                        override fun initChannel(channel: SocketChannel) {
                            initializeChannel(channel)
                        }
                    },
                )
                .bind(host, port)
                .awaitResult()

            bossGroup = boss
            workerGroup = worker
            serverChannel = channel
        } catch (error: Throwable) {
            boundRef.set(false)
            shutdownGroup(worker)
            shutdownGroup(boss)
            throw IllegalStateException("Failed to bind Java network listener to $host:$port.", error)
        }
    }

    override fun tickSessions(): Int =
        tickRegistry.tickAll()

    override fun close() {
        if (!boundRef.compareAndSet(true, false)) {
            return
        }

        val channel = serverChannel
        serverChannel = null
        channel?.close()?.awaitResult()

        val worker = workerGroup
        val boss = bossGroup
        workerGroup = null
        bossGroup = null
        shutdownGroup(worker)
        shutdownGroup(boss)
    }

    private fun eventLoopGroup(name: String, threads: Int = 0): EventLoopGroup =
        MultithreadEventLoopGroup(
            threads,
            ThreadFactory { task ->
                Thread(task, name).apply {
                    isDaemon = true
                }
            },
            NioHandler.newFactory(),
        )

    private fun shutdownGroup(group: EventLoopGroup?) {
        group?.shutdownGracefully()?.awaitResult()
    }

    internal fun initializeChannel(
        channel: Channel,
        session: JavaSessionHolder = JavaSessionHolder(),
    ) {
        val outboundFlushHandler = JavaOutboundFlushHandler(session)
        val sessionTickHandler = JavaSessionTickHandler(outboundFlushHandler)
        channel.pipeline()
            .addLast("java-varint-frame-decoder", JavaVarIntFrameDecoder())
            .addLast("java-packet-decoder", JavaPacketDecoder(stateHolder = session))
            .addLast("java-handshake-state-handler", JavaHandshakeStateHandler(session))
            .addLast("java-status-request-handler", JavaStatusRequestHandler())
            .addLast("java-login-start-handler", JavaLoginStartHandler(session))
            .addLast(
                "java-configuration-finish-handler",
                JavaConfigurationFinishHandler(
                    sessionHolder = session,
                    outboundFlushHandler = outboundFlushHandler,
                ),
            )
            .addLast("java-intent-queue-handler", JavaIntentQueueHandler(session))
            .addLast("java-outbound-flush-handler", outboundFlushHandler)
            .addLast("java-session-tick-handler", sessionTickHandler)
            .addLast("java-varint-frame-encoder", JavaVarIntFrameEncoder())
            .addLast("java-packet-encoder", JavaPacketEncoder(stateHolder = session))
            .addLast("java-network-exception-handler", JavaNetworkExceptionHandler(session))

        tickRegistry.register(sessionTickHandler, channel.pipeline().context(sessionTickHandler))
    }

    private fun <T> Future<T>.awaitResult(): T {
        val stage = asStage()
        if (!stage.await(bindTimeoutSeconds, TimeUnit.SECONDS)) {
            throw IllegalStateException("Timed out waiting for Netty operation.")
        }
        if (stage.isFailed) {
            throw stage.cause()
        }
        return stage.getNow()
    }

    companion object {
        private val PORT_RANGE = 0..65535
    }
}
