package trading

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import rpc.NodeRPCConnection

@RestController
class TradingApi(@Autowired val rpc: NodeRPCConnection) {

    @RequestMapping(path = ["whoami"], method = [RequestMethod.GET])
    fun whoami() {
        println(rpc.proxy.nodeInfo().legalIdentities.first().name.toString())
    }

}