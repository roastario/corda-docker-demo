package exchange

import net.corda.core.identity.Party
import net.corda.core.node.services.NetworkMapCache
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import rpc.NodeRPCConnection
import rx.schedulers.Schedulers
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentSkipListSet
import javax.annotation.PostConstruct

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class ParticipantsCache(@Autowired val rpc: NodeRPCConnection) {
    private val members: ConcurrentSkipListSet<Party> = ConcurrentSkipListSet()
    @PostConstruct
    fun setupListeners() {
        CompletableFuture.runAsync {
            val networkMapFeed = rpc.proxy.networkMapFeed()
            val me = rpc.proxy.nodeInfo().legalIdentities.first()
            networkMapFeed.snapshot.filter {
                it.legalIdentities.first() != me
            }.forEach { members.add(it.legalIdentities.first()) }
            val updates = networkMapFeed.updates
            updates.observeOn(Schedulers.io()).doOnNext {
                when (it) {
                    is NetworkMapCache.MapChange.Added ->
                        members.add(it.node.legalIdentities.first())
                    is NetworkMapCache.MapChange.Removed ->
                        members.remove(it.node.legalIdentities.first())
                    is NetworkMapCache.MapChange.Modified -> {
                        members.remove(it.previousNode.legalIdentities.first())
                        members.add(it.node.legalIdentities.first())
                    }
                }
            }
        }
    }

}