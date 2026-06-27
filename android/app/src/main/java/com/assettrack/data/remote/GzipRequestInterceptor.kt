package com.assettrack.data.remote

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.BufferedSink
import okio.GzipSink
import okio.buffer

/**
 * OkHttp Interceptor yang mengkompresi request body dengan GZIP.
 * Mengurangi ukuran JSON payload 60-80% saat push ke server.
 */
class GzipRequestInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val body     = original.body

        // Hanya kompres jika ada body dan belum dikompres
        if (body == null || original.header("Content-Encoding") != null) {
            return chain.proceed(original)
        }

        val compressedRequest = original.newBuilder()
            .header("Content-Encoding", "gzip")
            .method(original.method, gzip(body))
            .build()

        return chain.proceed(compressedRequest)
    }

    private fun gzip(body: RequestBody): RequestBody = object : RequestBody() {
        override fun contentType(): MediaType? = body.contentType()
        override fun contentLength(): Long     = -1  // tidak diketahui setelah kompresi

        override fun writeTo(sink: BufferedSink) {
            val gzipSink = GzipSink(sink).buffer()
            body.writeTo(gzipSink)
            gzipSink.close()
        }
    }
}
