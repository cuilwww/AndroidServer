# AndroidServer

[![@Tony沈哲 on weibo](https://img.shields.io/badge/weibo-%40Tony%E6%B2%88%E5%93%B2-blue.svg)](http://www.weibo.com/fengzhizi715)
[![License](https://img.shields.io/badge/license-Apache%202-lightgrey.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

基于 Kotlin+Netty 开发，为 Android 系统提供 Web Server 的服务，包括 Http、TCP、WebSocket

# Feature:

* 支持 Http、TCP、WebSocket 服务
* Http 的路由表采用字典树(Tried Tree)实现
* 日志隔离，开发者可以使用自己的日志库

# 最新版本

模块|最新版本
---|:-------------:
android-server-core|[ ![Download](https://api.bintray.com/packages/fengzhizi715/maven/android-server-core/images/download.svg) ](https://bintray.com/fengzhizi715/maven/android-server-core/_latestVersion)
android-server-converter-gson|[ ![Download](https://api.bintray.com/packages/fengzhizi715/maven/android-server-converter-gson/images/download.svg) ](https://bintray.com/fengzhizi715/maven/android-server-converter-gson/_latestVersion)

# 下载安装

Gradle:

```groovy
implementation 'com.safframework.server:android-server-core:<latest-version>'
```

```groovy
implementation 'com.safframework.server:android-server-converter-gson:<latest-version>'
```

# Usage:

## Http 服务

```kotlin
class HttpService : Service() {

    private lateinit var androidServer: AndroidServer

    override fun onCreate() {
        super.onCreate()
        startServer()
    }

    // 启动 Http 服务端
    private fun startServer() {

        androidServer = AndroidServer.Builder().converter(GsonConverter()).build()

        androidServer
            .get("/hello")  { _, response: Response ->
                response.setBodyText("hello world")
            }
            .get("/sayHi/{name}") { request,response: Response ->
                val name = request.param("name")
                response.setBodyText("hi $name!")
            }
            .post("/uploadLog") { request,response: Response ->
                val requestBody = request.content()
                response.setBodyText(requestBody)
            }
            .start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        androidServer.close()
        super.onDestroy()
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

}
```

## Socket 服务

Socket 服务支持同一个端口支持 TCP/WebSocket 服务

```kotlin
class SocketService : Service() {

    private lateinit var androidServer: AndroidServer

    override fun onCreate() {
        super.onCreate()
        startServer()
    }

    // 启动 Socket 服务端
    private fun startServer() {
        androidServer = AndroidServer.Builder().converter(GsonConverter()).port(8888).logProxy(LogProxy).build()

        androidServer
            .socket("/ws", object: SocketListener<String> {
                override fun onMessageResponseServer(msg: String, ChannelId: String) {
                    LogManager.d("SocketService","msg = $msg")
                }

                override fun onChannelConnect(channel: Channel) {
                    val insocket = channel.remoteAddress() as InetSocketAddress
                    val clientIP = insocket.address.hostAddress
                    LogManager.d("SocketService","connect client: $clientIP")

                }

                override fun onChannelDisConnect(channel: Channel) {
                    val ip = channel.remoteAddress().toString()
                    LogManager.d("SocketService","disconnect client: $ip")
                }

            })
            .start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {

        androidServer.close()
        super.onDestroy()
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
```

# TODO List：

* 支持 HttpSession
* 支持 Https
* 支持 HTTP/2
* 自定义拦截器
