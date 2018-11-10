package com.banditos.piratestation.ftpserver

import org.apache.ftpserver.FtpServer
import org.apache.ftpserver.FtpServerFactory
import org.apache.ftpserver.listener.Listener
import org.apache.ftpserver.listener.ListenerFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class FileServer {

    private val logger = LoggerFactory.getLogger(javaClass)
    lateinit var ftpServer: FtpServer

    @PostConstruct
    fun init() {
        val ftpServerFactory = FtpServerFactory()
        ftpServerFactory.addListener("default", createListener())
        ftpServer = ftpServerFactory.createServer()
        ftpServer.start()
        logger.info("FTP server started")
    }

    private fun createListener() : Listener {
        val lf = ListenerFactory()
        lf.port = 2121
        return lf.createListener()
    }
}