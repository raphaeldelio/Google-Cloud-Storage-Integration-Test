package raphael.delio.gcsintegration

import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

/**
 * [GoogleCloudStorageTestContainer] is a [GenericContainer] that provides a Google Cloud Storage service.
 */
class GoogleCloudStorageTestContainer : GenericContainer<GoogleCloudStorageTestContainer>(
    DockerImageName.parse("fsouza/fake-gcs-server")
) {
    init {
        withExposedPorts(4443)
        withCreateContainerCmdModifier {
            it.withEntrypoint("/bin/fake-gcs-server", "-scheme", "http")
        }
    }
}
