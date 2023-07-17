package com.example.simplemediasoup.service

import android.annotation.SuppressLint
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okio.ByteString
import org.apache.http.conn.ssl.SSLSocketFactory
import org.json.JSONObject
import org.mediasoup.droid.Logger
import org.protoojs.droid.transports.AbsWebSocketTransport
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.CountDownLatch
import javax.net.ssl.*

class WebSocketTransport(url: String) : AbsWebSocketTransport(url) {

    companion object {
        const val TAG = "WebSocketTransport"
    }

    private var mClosed = false
    private var mConnected = false
    private var mOkHttpClient: OkHttpClient? = null
    private var mHandler: Handler? = null
    private var mWebSocket: WebSocket? = null
    private var mListener: Listener? = null
    private var mRetryStrategy: RetryStrategy? = null

    init {
        mOkHttpClient = getUnsafeOkHttpClient()
        val handlerThread = HandlerThread("socket")
        handlerThread.start()
        mHandler = Handler(handlerThread.looper)
        mRetryStrategy = RetryStrategy(10, 2, 1000, 8 * 1000)
    }

    private class RetryStrategy(
        private val retries: Int,
        private val factor: Int,
        private val minTimeout: Int,
        private val maxTimeout: Int
    ) {
        var retryCount = 1
        fun retried() {
            retryCount++
        }

        val reconnectInterval: Int
            get() {
                if (retryCount > retries) {
                    return -1
                }
                var reconnectInterval =
                    (minTimeout * Math.pow(factor.toDouble(), retryCount.toDouble())).toInt()
                reconnectInterval = Math.min(reconnectInterval, maxTimeout)
                return reconnectInterval
            }

        fun reset() {
            if (retryCount != 0) {
                retryCount = 0
            }
        }
    }

    private fun newWebSocket() {
        mWebSocket = null
        mOkHttpClient?.newWebSocket(
            Request.Builder().url(mUrl).addHeader("Sec-WebSocket-Protocol", "protoo").build(),
            ProtooWebSocketListener())
    }

    private fun scheduleReconnect(): Boolean {
        val reconnectInterval: Int = mRetryStrategy?.reconnectInterval ?: -1
        if (reconnectInterval == -1) {
            return false
        }
        Logger.d(TAG, "scheduleReconnect() ")
        mHandler!!.postDelayed(
            {
                if (mClosed) {
                    return@postDelayed
                }
                Logger.w(TAG, "doing reconnect job, retryCount: " + mRetryStrategy?.retryCount)
                mOkHttpClient!!.dispatcher.cancelAll()
                newWebSocket()
                mRetryStrategy?.retried()
            },
            reconnectInterval.toLong()
        )
        return true
    }

    private inner class ProtooWebSocketListener: WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            if (mClosed) return
            Logger.d(TAG, "onOpen() ")
            mWebSocket = webSocket
            mConnected = true
            mListener?.onOpen()
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Logger.w(TAG, "onClose() ")
            if (mClosed) return
            mConnected = true
            mListener?.onClose()
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Logger.w(TAG, "onClosing() ")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Logger.w(TAG, "onFailure() ")
            if (mClosed) return
            if (scheduleReconnect()) {
                mListener?.let { listener ->
                    if (mConnected) {
                        listener.onFail()
                    } else listener.onDisconnected()
                }
            } else {
                Logger.e(TAG, "give up reconnect. notify closed")
                mClosed = true
                mListener?.onClose()
                mRetryStrategy?.reset()
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            Logger.d(TAG, "onMessage() ")
            if (mClosed) return
            val message = org.protoojs.droid.Message.parse(text) ?: return
            mListener?.onMessage(message)
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Logger.d(TAG, "onMessage()")
        }
    }

    override fun connect(listener: Listener?) {
        Logger.d(TAG, "connect() ")
        mListener = listener
        mHandler?.post { newWebSocket() }
    }

    override fun sendMessage(message: JSONObject?): String {
        if (mClosed) {
            throw java.lang.IllegalStateException("transport closed")
        }

        val payload = message.toString()
        mHandler?.post {
            if (mClosed) {
                return@post
            }
            mWebSocket?.send(payload)
        }
        return payload
    }

    override fun close() {
        if (mClosed) return
        mClosed = true
        Logger.d(TAG, "close()")
        val countDownLatch = CountDownLatch(1)
        mHandler?.post {
            mWebSocket?.close(1000, "bye")
            mWebSocket = null
            countDownLatch.countDown()
        }
        try {
            countDownLatch.await()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun isClosed(): Boolean = mClosed

    private fun getUnsafeOkHttpClient(): OkHttpClient? {
        return try {
            val trustAllCerts = arrayOf<TrustManager>(
                @SuppressLint("CustomX509TrustManager")
                object : X509TrustManager {
                    @SuppressLint("TrustAllX509TrustManager")
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate>, authType: String
                    ) {
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate>, authType: String
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
            )
            val sslContext = SSLContext.getInstance(SSLSocketFactory.SSL)
            sslContext.init(null, trustAllCerts, SecureRandom())
            val sslSocketFactory = sslContext.socketFactory
            val httpLoggingInterceptor = HttpLoggingInterceptor { Log.i("RetrofitLog", "Retrofit callback = $it") }.apply {
                setLevel(HttpLoggingInterceptor.Level.BODY)
            }

            val builder =  OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .retryOnConnectionFailure(true)
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { _: String?, _: SSLSession? -> true }
            builder.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}