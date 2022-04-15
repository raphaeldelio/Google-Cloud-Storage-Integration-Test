package raphael.delio.gcsintegration

import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobListOption.prefix

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.*

@SpringBootApplication
@EnableScheduling
class GcsIntegrationApplication(
    private val storage: Storage
) {

    @Scheduled(fixedDelay = 100)
    fun schedule() {
        mkdirs()
        downloadFiles()
        processFiles()
        uploadFiles()
        cleanDirs()
    }

    /**
     * Create directories for the files to be downloaded and stored after processing.
     */
    fun mkdirs() {
        val inputDir = File(TMP_DIR_INPUT)
        inputDir.mkdirs()

        val outputDir = File(TMP_DIR_OUTPUT)
        outputDir.mkdirs()
    }

    /**
     * Clean up the directories after processing and uploading.
     */
    fun cleanDirs() {
        val inputDir = File(TMP_DIR_INPUT)
        inputDir.deleteRecursively()

        val outputDir = File(TMP_DIR_OUTPUT)
        outputDir.deleteRecursively()
    }

    /**
     * Download the files from the bucket.
     */
    private fun downloadFiles() {
        val bucket = storage.get(BUCKET_NAME)
        bucket?.list(prefix(BUCKET_INPUT_DIR))?.iterateAll()?.forEach { blob ->
            val path = Paths.get(TMP_DIR_INPUT + "/" + blob.name.split("/").last())
            blob.downloadTo(path)
        }
    }

    /**
     * Process the files in the input directory.
     */
    private fun processFiles() {
        val inputDir = Paths.get(TMP_DIR_INPUT)
        Files.walk(inputDir).filter { it.isRegularFile() }.forEach { file ->
            val text = file.readText(Charsets.UTF_8)
            val outputText = text.uppercase()

            val outputDir = Path(TMP_DIR_OUTPUT + "/" + file.fileName)
            outputDir.writeText(outputText)
        }
    }

    /**
     * Upload the files to the bucket.
     */
    private fun uploadFiles() {
        val bucket = storage.get(BUCKET_NAME)
        val outputDir = Paths.get(TMP_DIR_OUTPUT)
        Files.walk(outputDir).filter { it.isRegularFile() }.forEach { file ->
            bucket?.create(BUCKET_OUTPUT_DIR + file.fileName.toString(), file.readBytes())
        }
    }

    companion object {
        const val BUCKET_NAME = "gcs-integration"
        const val BUCKET_INPUT_DIR = "input/"
        const val BUCKET_OUTPUT_DIR = "output/"
        const val TMP_DIR_INPUT = "/tmp/gcs-integration/input"
        const val TMP_DIR_OUTPUT = "/tmp/gcs-integration/output"
    }
}

fun main(args: Array<String>) {
    runApplication<GcsIntegrationApplication>(*args)
}
