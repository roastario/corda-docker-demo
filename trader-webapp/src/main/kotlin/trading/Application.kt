package trading

import net.corda.client.jackson.JacksonSupport
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableAsync
import rpc.NodeRPCConnection

@SpringBootApplication(scanBasePackages = [
    "rpc",
    "trading"
])
@EnableAsync
private open class Application {
    @Bean
    open fun cordaModule(rpc: NodeRPCConnection) = JacksonSupport.createDefaultMapper(rpc.proxy)
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java)
}