package com.assettrack.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import okio.GzipSink
import okio.buffer
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.IOException

class GzipRequestInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        val body = originalRequest.body
        if (body == null || originalRequest.method == "GET") {
            return chain.proceed(originalRequest)
        }

        // Jangan kompres multipart request (seperti upload file bukti) karena sudah terkompresi (jpeg/pdf)
        // dan agar tidak membingungkan parser multipart di backend.
        val contentType = body.contentType()
        if (contentType != null && contentType.toString().contains("multipart", ignoreCase = true)) {
            return chain.proceed(originalRequest)
        }

        val compressedRequest = originalRequest.newBuilder()
            .header("Content-Encoding", "gzip")
            .method(originalRequest.method, gzip(body))
            .build()
            
        return chain.proceed(compressedRequest)
    }

    private fun gzip(body: RequestBody): RequestBody {
        return object : RequestBody() {
            override fun contentType(): MediaType? {
                return body.contentType()
            }

            override fun contentLength(): Long {
                return -1 // We don't know the compressed length in advance!
            }

            @Throws(IOException::class)
            override fun writeTo(sink: BufferedSink) {
                val gzipSink = GzipSink(sink).buffer()
                body.writeTo(gzipSink)
                gzipSink.close()
            }
        }
    }
}
