import kotlinx.coroutines.*
import ws.schild.jave.Encoder
import ws.schild.jave.MultimediaObject
import ws.schild.jave.encode.AudioAttributes
import ws.schild.jave.encode.EncodingAttributes
import java.io.File
import kotlin.system.measureTimeMillis

class AudioConverter {
    suspend fun convertWavToMp3(inputDirectory: File, outputDirectory: File) = coroutineScope {
        launch {
            // Code in here won't block the main thread.
            outputDirectory.mkdirs()

            if (!inputDirectory.exists() || !inputDirectory.isDirectory) {
                println("Input directory does not exist or is not a directory.")
                return@launch
            }

            val files = inputDirectory.listFiles { file ->
                file.extension == "wav"
            } ?: arrayOf()

            val jobs = files.map { inputFile ->
                async(Dispatchers.IO) {
                    processFile(inputFile, outputDirectory)
                }
            }

            jobs.awaitAll()
        }
    }

    private fun createAudioAttributes(): AudioAttributes {
        val audioAttributes = AudioAttributes()
        audioAttributes.setCodec("libmp3lame")
        audioAttributes.setBitRate(128000) // 128 kbps
        audioAttributes.setChannels(2)
        audioAttributes.setSamplingRate(44100)
        return audioAttributes
    }

    private fun createEncodingAttributes(audioAttributes: AudioAttributes): EncodingAttributes {
        val encodingAttributes = EncodingAttributes()
        encodingAttributes.setOutputFormat("mp3")
        encodingAttributes.setAudioAttributes(audioAttributes)
        return encodingAttributes
    }

    private fun processFile(inputFile: File, outputDirectory: File) {
        try {
            val outputFileName = "${inputFile.nameWithoutExtension}.mp3"
            val outputFile = File(outputDirectory, outputFileName)

            val audioAttributes = createAudioAttributes()
            val encodingAttributes = createEncodingAttributes(audioAttributes)

            Encoder().encode(MultimediaObject(inputFile), outputFile, encodingAttributes)

        } catch (exception: Exception) {
            println("Error converting file ${inputFile.name}: ${exception.message}")
        }
    }
}

fun main() {
    runBlocking {
        println("What WAV files do you want to convert? (Path to directory):")
        val userInputForWAV = readLine()
        println("Where would you like your MP3 files to end up? (Path to directory):")
        val userInputForMP3 = readLine()

        val inputDirectoryPath = File(userInputForWAV)
        val outputDirectoryPath = File(userInputForMP3)

        val time = measureTimeMillis {
            AudioConverter().convertWavToMp3(inputDirectoryPath, outputDirectoryPath)
        }
        println("Completed in $time ms")
    }
}