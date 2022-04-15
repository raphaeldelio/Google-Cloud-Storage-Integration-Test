package raphael.delio.gcsintegration

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.BucketInfo
import com.google.cloud.storage.Storage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path


@RunWith(SpringRunner::class)
@SpringBootTest
@ContextConfiguration(
    initializers = [
        GoogleCloudStorageTestContainerInitializer::class
    ]
)
class GcsIntegrationApplicationTests {

    @Autowired
    private lateinit var storage: Storage

    private val bucketName = "gcs-integration"

    fun init() {
        storage.create(BucketInfo.newBuilder(bucketName).build())
    }

    @Test
    fun `GIVEN file is created in bucket THEN file is processed AND reuploaded to bucket`() {
        init() // Create bucket

        // Create file
        val message = "Hello World"
        val file = File("/tmp/test.txt")
        file.writeText(message)

        // Upload file
        val blobId = BlobId.of(bucketName, "input/test.txt")
        val blobInfo = BlobInfo.newBuilder(blobId).build()
        storage.create(blobInfo, Files.readAllBytes(file.toPath()))

        // Process file
        Thread.sleep(1000)

        // Download output file
        val outputBlobId = BlobId.of(bucketName, "output/test.txt")
        val outputBlob = storage.get(outputBlobId)
        outputBlob.downloadTo(Path("/tmp/output.txt"))
        val output = File("/tmp/output.txt").readText()

        // Assert output file is correct
        assertEquals(message.uppercase(), output)
    }

}
