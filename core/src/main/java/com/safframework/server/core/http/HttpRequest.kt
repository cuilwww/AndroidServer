package com.safframework.server.core.http

import com.safframework.server.core.http.cookie.HttpCookie
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.QueryStringDecoder
import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.codec.http.cookie.ServerCookieDecoder
import io.netty.util.CharsetUtil


/**
 *
 * @FileName:
 *          com.safframework.server.core.http.HttpRequest
 * @author: Tony Shen
 * @date: 2020-03-23 20:15
 * @version: V1.0 <描述当前版本功能>
 */
class HttpRequest(private val fullHttpRequest: FullHttpRequest) : Request {

    private val params: MutableMap<String, String> = mutableMapOf()

    private val headers: MutableMap<String, String> = mutableMapOf()

    init {
        val list: List<Map.Entry<String, String>> = fullHttpRequest.headers().entries()
        for ((key, value) in list) {
            headers.put(key,value)
        }

        val decoder = QueryStringDecoder(fullHttpRequest.uri())
        val param = decoder.parameters()
        for ((key, value) in param) {
            params[key] = value[0]
        }
    }

    override fun method(): HttpMethod = HttpMethod.getMethod(fullHttpRequest.method())

    override fun url(): String = fullHttpRequest.uri()

    override fun headers(): MutableMap<String, String>  = headers

    override fun header(name: String): String?  = headers[name]

    override fun cookies(): Set<HttpCookie> = headers[COOKIE]?.let {
        val cookies: Set<Cookie> = ServerCookieDecoder.LAX.decode(it)
        wrapCookies(cookies)
    }?: mutableSetOf()

    private fun wrapCookies(cookies: Set<Cookie>): Set<HttpCookie> {
        val httpCookies: MutableSet<HttpCookie> = HashSet()
        for (cookie in cookies) {
            val hc = HttpCookie(cookie)
            httpCookies.add(hc)
        }
        return httpCookies
    }

    override fun params(): MutableMap<String, String> = params

    override fun param(name: String): String? = params[name]

    override fun content(): String = fullHttpRequest.content().toString(CharsetUtil.UTF_8)

    companion object {
        private val COOKIE = "cookie"
    }
}