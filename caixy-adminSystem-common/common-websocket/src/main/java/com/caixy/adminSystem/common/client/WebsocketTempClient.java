package com.caixy.adminSystem.common.client;

import com.caixy.adminSystem.common.base.utils.DateUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * WebSocketClientResource：支持同步 try-with-resources 和异步 connectAsync 两种模式
 * <p><strong>禁止在 try-with-resources 中使用该方法</strong>。该方法将自动关闭连接资源。</p>
 *
 * @Author CAIXYPROMISE
 * @since 2025/5/20 17:08
 */
@Slf4j
public class WebsocketTempClient implements Closeable
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final DateTimeFormatter TS_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // 全局回调线程池
    private static final ExecutorService CALLBACK_EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory()
    {
        private final AtomicInteger cnt = new AtomicInteger();

        public Thread newThread(Runnable r)
        {
            Thread t = new Thread(r, "ws-callback-" + cnt.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    });

    private volatile WebSocketSession session;
    private final BlockingQueue<String> textMessageQueue;
    private final BlockingQueue<byte[]> binaryMessageQueue;
    private final CompletableFuture<Void> errorFuture;
    private final long maxSessionDuration;
    private final TimeUnit maxSessionDurationUnit;

    private volatile Consumer<String> onText;
    private volatile Consumer<byte[]> onBinary;
    private volatile Consumer<Throwable> onError;

    private final String uri;
    private final WebSocketHttpHeaders headers;
    private final long connectTimeout;
    private final TimeUnit connectUnit;
    private final int queueCapacity;

    // 私有构造，仅由 Builder 调用
    private WebsocketTempClient(Builder b)
    {
        this.uri = b.uri;
        this.headers = b.headers != null ? b.headers : new WebSocketHttpHeaders();
        this.connectTimeout = b.connectTimeout;
        this.connectUnit = b.connectUnit;
        this.maxSessionDuration = b.maxSessionDuration;
        this.maxSessionDurationUnit = b.maxSessionDurationUnit;
        this.queueCapacity = b.queueCapacity;
        this.textMessageQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.binaryMessageQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.errorFuture = new CompletableFuture<>();
    }

    // — 同步阻塞构造 try-with-resources 支撑
    public static WebsocketTempClient build(Builder b) throws InterruptedException, ExecutionException, TimeoutException, IOException
    {
        WebsocketTempClient ws = new WebsocketTempClient(b);
        StandardWebSocketClient client = new StandardWebSocketClient();
        // spring boot 3.x
//        CompletableFuture<WebSocketSession> future = client.execute(ws.new InternalHandler(null), b.headers, URI.create(b.uri));
        // spring boot 2.x
        ListenableFuture<WebSocketSession> future = client.doHandshake(ws.new InternalHandler(null, b.uri), b.headers, URI.create(b.uri));
        WebSocketSession sess = future.get(b.connectTimeout, b.connectUnit);
        if (sess == null || !sess.isOpen())
        {
            throw new IOException("握手失败或会话未打开");
        }
        ws.session = sess;
        return ws;
    }

    // — 异步非阻塞构造 —— 返回 CompletableFuture
    public static CompletableFuture<WebsocketTempClient> connectWithAsync(Builder b)
    {
        // 1. 构造但不关闭：资源由异步完成时或用户最终调用 close() 来释放
        WebsocketTempClient ws = new WebsocketTempClient(b);
        CompletableFuture<WebsocketTempClient> result = new CompletableFuture<>();

        StandardWebSocketClient client = new StandardWebSocketClient();
        ListenableFuture<WebSocketSession> future = client.doHandshake(ws.new InternalHandler(null, b.uri), b.headers, URI.create(b.uri));
        // 2. 定时器只用于超时，不需要 try-with-resources
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ws-timeouter");
            t.setDaemon(true);
            return t;
        });
        ScheduledFuture<?> timeoutTask = ses.schedule(() -> {
            if (!result.isDone())
            {
                result.completeExceptionally(new TimeoutException("WebSocket 连接超时"));
                future.cancel(true);
            }
        }, b.connectTimeout, b.connectUnit);

        // 3. 在连接完成（成功或失败）时，一并 cancel 定时任务并 shutdown 定时器
        // spring boot 3.x
//        future.whenComplete((sess, err) -> {
//            timeoutTask.cancel(true);
//            ses.shutdown();
//        });
        // spring boot 2.x
        future.addCallback(new ListenableFutureCallback<WebSocketSession>()
        {
            @Override
            public void onSuccess(WebSocketSession sess)
            {
                timeoutTask.cancel(true);
                ses.shutdown();
            }

            @Override
            public void onFailure(Throwable ex)
            {
                timeoutTask.cancel(true);
                ses.shutdown();
            }
        });

        // 4. 如果异步最终是异常方向完成，自动关闭 WebSocketClientResource
        result.whenComplete((wsrc, ex) -> {
            if (ex != null)
            {
                try
                {
                    wsrc.close();
                }
                catch (Exception ignore)
                {
                }
            }
        });

        return result;
    }

    /**
     * 发送 Text
     */
    public void sendText(String text) throws IOException
    {
        session.sendMessage(new TextMessage(text));
    }

    public void sendBinary(byte[] data) throws IOException
    {
        session.sendMessage(new BinaryMessage(data));
    }

    public void sendJson(Object payload) throws IOException
    {
        String json = OBJECT_MAPPER.writeValueAsString(payload);
        session.sendMessage(new TextMessage(json));
    }

    public CompletableFuture<Void> sendTextAsync(String text)
    {
        return CompletableFuture.runAsync(() -> {
            try
            {
                sendText(text);
            }
            catch (IOException e)
            {
                throw new CompletionException(e);
            }
        });
    }

    public CompletableFuture<Void> sendBinaryAsync(byte[] data)
    {
        return CompletableFuture.runAsync(() -> {
            try
            {
                sendBinary(data);
            }
            catch (IOException e)
            {
                throw new CompletionException(e);
            }
        });
    }

    public CompletableFuture<Void> sendJsonAsync(Object payload)
    {
        return CompletableFuture.runAsync(() -> {
            try
            {
                sendJson(payload);
            }
            catch (IOException e)
            {
                throw new CompletionException(e);
            }
        });
    }

    private <T> List<T> listenerCustom(BlockingQueue<T> queue, Predicate<T> predicate) throws InterruptedException, TimeoutException, ExecutionException
    {
        List<T> collected = new ArrayList<>();
        long deadline = System.currentTimeMillis() + maxSessionDurationUnit.toMillis(maxSessionDuration);

        while (true)
        {
            if (errorFuture.isDone())
            {
                errorFuture.get();
            }

            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0)
            {
                throw new TimeoutException("等待批量消息超时");
            }

            T msg = queue.poll(remaining, TimeUnit.MILLISECONDS);
            if (msg == null)
            {
                throw new TimeoutException("等待批量消息超时");
            }

            collected.add(msg);
            if (predicate.test(msg))
            {
                break;
            }
        }
        close();
        return collected;
    }

    /**
     * 同步接收多条消息，直到 predicate 为 true 或超时抛异常；
     *
     * @return 返回监听期间的所有消息列表
     */
    public List<String> listener(Predicate<String> predicate) throws InterruptedException, TimeoutException, ExecutionException
    {
        return listenerCustom(textMessageQueue, predicate);
    }

    public List<byte[]> listenerBinary(Predicate<byte[]> predicate) throws InterruptedException, TimeoutException, ExecutionException
    {
        return listenerCustom(binaryMessageQueue, predicate);
    }

    /**
     * 异步接收多条消息，直到 predicate 为 true 或超时失败
     */
    public CompletableFuture<List<String>> listenerAsync(Predicate<String> predicate)
    {
        return CompletableFuture.supplyAsync(() -> {
            try
            {
                return listener(predicate);
            }
            catch (Exception e)
            {
                throw new CompletionException(e);
            }
        });
    }

    public CompletableFuture<List<byte[]>> listenerBinaryAsync(Predicate<byte[]> predicate)
    {
        return CompletableFuture.supplyAsync(() -> {
            try
            {
                return listenerBinary(predicate);
            }
            catch (Exception e)
            {
                throw new CompletionException(e);
            }
        });
    }


    /**
     * 注册文本回调
     */
    public WebsocketTempClient onText(Consumer<String> c)
    {
        this.onText = c;
        return this;
    }

    /**
     * 注册二进制回调
     */
    public WebsocketTempClient onBinary(Consumer<byte[]> c)
    {
        this.onBinary = c;
        return this;
    }

    /**
     * 注册错误回调
     */
    public WebsocketTempClient onError(Consumer<Throwable> c)
    {
        this.onError = c;
        return this;
    }

    /**
     * 关闭会话，try-with-resources / finally 自动调用
     */
    @Override
    public void close()
    {
        try
        {
            if (session != null && session.isOpen())
            {
                session.close(CloseStatus.NORMAL);
            }
        }
        catch (IOException ignored)
        {
        }
        textMessageQueue.clear();
        binaryMessageQueue.clear();
        errorFuture.completeExceptionally(new IOException("WebSocket 已关闭"));
    }

    private class InternalHandler extends AbstractWebSocketHandler
    {
        private final CompletableFuture<WebsocketTempClient> connectFuture;
        private final String targetUri;
        private final StopWatch stopWatch;

        InternalHandler(CompletableFuture<WebsocketTempClient> f, String targetUri)
        {
            this.connectFuture = f;
            this.targetUri = targetUri;
            this.stopWatch = new StopWatch();
        }

        /**
         * 连接建立时回调
         */
        @Override
        public void afterConnectionEstablished(WebSocketSession session)
        {
            // 保存会话
            WebsocketTempClient.this.session = session;
            // 如果是异步模式，通知外部连接完成
            if (this.connectFuture != null)
            {
                this.connectFuture.complete(WebsocketTempClient.this);
            }
            log.info("ws连接成功: {}, 连接时间: {}", targetUri, DateUtils.getDateTime());
            this.stopWatch.start();
        }

        /**
         * 处理文本消息
         */
        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception
        {
            String payload = message.getPayload();
            // 入队
            textMessageQueue.offer(payload);
            // 回调用户注册的 onText
            if (onText != null)
            {
                CALLBACK_EXECUTOR.submit(() -> onText.accept(payload));
            }
        }

        /**
         * 处理二进制消息
         */
        @Override
        protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception
        {
            ByteBuffer buf = message.getPayload();
            byte[] data = new byte[buf.remaining()];
            buf.get(data);
            // 入队
            binaryMessageQueue.offer(data);
            // 回调用户注册的 onBinary
            if (onBinary != null)
            {
                CALLBACK_EXECUTOR.submit(() -> onBinary.accept(data));
            }
        }

        /**
         * 传输错误时回调
         */
        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception
        {
            super.handleTransportError(session, exception);
            // 保持原有逻辑：完成 errorFuture、回调 onError、关闭会话、异步通知连接失败
            errorFuture.completeExceptionally(exception);
            if (onError != null)
            {
                CALLBACK_EXECUTOR.submit(() -> onError.accept(exception));
            }
            session.close(CloseStatus.SERVER_ERROR);
            if (this.connectFuture != null)
            {
                this.connectFuture.completeExceptionally(exception);
            }
        }

        /**
         * 连接关闭时回调
         */
        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception
        {
            super.afterConnectionClosed(session, status);
            if (this.connectFuture != null)
            {
                this.connectFuture.completeExceptionally(new IOException("WebSocket 已关闭"));
            }
            if (stopWatch.isRunning())
            {
                stopWatch.stop();
            }

            log.info("ws连接关闭, 目标URI: {}, 关闭时间: {}, 连接时长: {}s",
                    targetUri, DateUtils.getDateTime(), DateUtils.millisecondToSecond(stopWatch.getTotalTimeMillis()));
        }
    }


    public static class Builder
    {
        private String uri;                                         // 目标 WS URI
        private long connectTimeout = 3;                            // 请求连接等待时间
        private TimeUnit connectUnit = TimeUnit.SECONDS;            // 请求连接等待时间单位
        private long maxSessionDuration = 5;                        // 最大连线时间，默认5秒
        private TimeUnit maxSessionDurationUnit = TimeUnit.SECONDS; // 最大连线时间单位
        private int queueCapacity = 100;                            // 消息队列容量
        private WebSocketHttpHeaders headers;                       // 请求头

        /**
         * 目标 WS URI
         */
        public Builder uri(String uri)
        {
            this.uri = Objects.requireNonNull(uri);
            return this;
        }

        public Builder headers(WebSocketHttpHeaders h)
        {
            this.headers = h;
            return this;
        }

        public Builder connectTimeout(long t, TimeUnit u)
        {
            this.connectTimeout = t;
            this.connectUnit = u;
            return this;
        }

        public Builder maxSessionDuration(long t, TimeUnit u)
        {
            this.maxSessionDuration = t;
            this.maxSessionDurationUnit = u;
            return this;
        }

        public Builder queueCapacity(int c)
        {
            this.queueCapacity = c;
            return this;
        }

        /**
         * 同步构造
         */
        public WebsocketTempClient build() throws InterruptedException, ExecutionException, TimeoutException, IOException
        {
            return WebsocketTempClient.build(this);
        }

        /**
         * 异步构造
         */
        public <T> CompletableFuture<T> connectWithAsync(Function<WebsocketTempClient, CompletableFuture<T>> action)
        {
            // 1. 异步建立连接
            return WebsocketTempClient.connectWithAsync(this)
                    // 2. 连接成功后执行业务逻辑
                    .thenCompose(ws -> action.apply(ws)
                            // 3. 完成后（成功或异常）都自动关闭 ws
                            .whenComplete((res, err) -> {
                                ws.close();
                                ;
                            }));
        }
    }
}


