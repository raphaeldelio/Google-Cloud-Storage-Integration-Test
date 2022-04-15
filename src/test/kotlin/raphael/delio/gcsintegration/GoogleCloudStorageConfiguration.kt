package raphael.delio.gcsintegration

import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

/**
 * This configuration is used to override the default Google Cloud Storage client with the one created by Testcontainers.
 */
@Configuration
class GoogleCloudStorageConfiguration {

    @Value("\${gcs.port}")
    private lateinit var gcsContainerPort: String

    @Bean
    @Primary
    fun storage(): Storage {
        return StorageOptions.newBuilder()
            .setHost("http://localhost:$gcsContainerPort")
            .build()
            .service
    }
}
