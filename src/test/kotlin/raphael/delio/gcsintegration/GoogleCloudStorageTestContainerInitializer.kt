package raphael.delio.gcsintegration

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ApplicationListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.event.ContextClosedEvent

open class GoogleCloudStorageTestContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    private lateinit var container: GoogleCloudStorageTestContainer

    override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
        container = createContainer()
        container.start()

        applyConfiguration(configurableApplicationContext)

        val applicationListener: ApplicationListener<ContextClosedEvent> = ApplicationListener {
            if (container.isRunning)
                container.stop()
        }
        configurableApplicationContext.addApplicationListener(applicationListener)
    }

    private fun createContainer(): GoogleCloudStorageTestContainer {
        return GoogleCloudStorageTestContainer()
    }

    private fun applyConfiguration(
        configurableApplicationContext: ConfigurableApplicationContext
    ) {
        TestPropertyValues.of(
            "gcs.port=${container.firstMappedPort}"
        ).applyTo(configurableApplicationContext.environment)
    }
}
