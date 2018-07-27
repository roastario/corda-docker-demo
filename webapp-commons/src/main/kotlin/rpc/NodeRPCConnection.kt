package rpc

import net.corda.client.rpc.CordaRPCClient
import net.corda.client.rpc.CordaRPCConnection
import net.corda.client.rpc.RPCException
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.loggerFor
import org.apache.activemq.artemis.api.core.ActiveMQNotConnectedException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.lang.Thread.sleep
import javax.annotation.PreDestroy

/**
 * Wraps a node RPC proxy.
 *
 * The RPC proxy is configured based on the properties in `application.properties`.
 *
 * @param host The host of the node we are connecting to.
 * @param rpcPort The RPC port of the node we are connecting to.
 * @param username The username for logging into the RPC client.
 * @param password The password for logging into the RPC client.
 * @property proxy The RPC proxy.
 */
@Component
class NodeRPCConnection(
        @Value("\${config.rpc.username}") private val username: String,
        @Value("\${config.rpc.password}") private val password: String,
        @Value("\${config.rpc.host}") private val host: String,
        @Value("\${config.rpc.port}") private val rpcPort: Int
) : AutoCloseable {

    private companion object {
        private val log = loggerFor<NodeRPCConnection>()
    }

    private lateinit var rpcConnection: CordaRPCConnection
    lateinit var proxy: CordaRPCOps

    init {
        val rpcAddress = NetworkHostAndPort(host, rpcPort)
        val rpcClient = CordaRPCClient(rpcAddress)
        connect(rpcClient)
    }

    private fun connect(rpcClient: CordaRPCClient) {
        try {
            rpcConnection = rpcClient.start(username, password)
            proxy = rpcConnection.proxy
        } catch (e: Exception) {
            when (e) {
                is ActiveMQNotConnectedException -> retry(rpcClient)
                is RPCException -> retry(rpcClient)
                else -> throw e
            }

        }
        log.info("Connected to RPC proxy: $proxy")
    }

    private fun retry(rpcClient: CordaRPCClient) {
        log.info("Cannot connect to RPC client, retrying")
        sleep(10000)
        connect(rpcClient)
    }

    @PreDestroy
    override fun close() {
        rpcConnection.notifyServerAndClose()
    }
}