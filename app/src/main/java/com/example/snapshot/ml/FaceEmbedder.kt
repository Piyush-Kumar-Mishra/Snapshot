package com.example.snapshot.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.snapshot.util.ImageUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class FaceEmbedder @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private var interpreter: Interpreter? = null
    private var inputWidth = 112
    private var inputHeight = 112
    private var embeddingDim = 192
    private var isUsingFallback = false

    companion object {
        private const val TAG = "FaceEmbedder"
        private val CANDIDATE_MODEL_NAMES = listOf(
            "facenet.tflite",
            "mobile_face_net.tflite",
            "facenet_512.tflite",
            "face_recognition.tflite"
        )
    }

    init {
        initModel()
    }

    private fun initModel() {
        try {
            val assetList = context.assets.list("")?.toList() ?: emptyList()
            val modelName = CANDIDATE_MODEL_NAMES.firstOrNull { it in assetList }

            if (modelName != null) {
                val modelBuffer = loadModelFile(modelName)
                val options = Interpreter.Options().apply {
                    numThreads = 4
                }
                val interp = Interpreter(modelBuffer, options)
                
                // Dynamically inspect tensor shapes from the loaded model
                val inShape = interp.getInputTensor(0).shape()
                inputHeight = inShape[1]
                inputWidth = inShape[2]

                val outShape = interp.getOutputTensor(0).shape()
                embeddingDim = outShape[1]

                interpreter = interp
                isUsingFallback = false
                Log.i(TAG, "Successfully loaded TFLite model '$modelName'. Input: ${inputWidth}x$inputHeight, Output: $embeddingDim-d")
            } else {
                Log.w(TAG, "No face embedding model found in assets. Using deterministic fallback descriptor until mobile_face_net.tflite is placed in assets.")
                isUsingFallback = true
                embeddingDim = 128
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load TFLite model, using fallback descriptor", e)
            isUsingFallback = true
            embeddingDim = 128
        }
    }

    private fun loadModelFile(modelName: String): ByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Extracts an L2-normalized identity embedding vector from a cropped face bitmap.
     */
    fun extractEmbedding(faceBitmap: Bitmap): FloatArray {
        val currentInterpreter = interpreter
        if (currentInterpreter == null || isUsingFallback) {
            return generateFallbackEmbedding(faceBitmap)
        }

        return try {
            val scaledBitmap = if (faceBitmap.width != inputWidth || faceBitmap.height != inputHeight) {
                Bitmap.createScaledBitmap(faceBitmap, inputWidth, inputHeight, true)
            } else {
                faceBitmap
            }

            // Allocate input buffer: 1 * height * width * 3 channels * 4 bytes (Float)
            val inputBuffer = ByteBuffer.allocateDirect(1 * inputHeight * inputWidth * 3 * 4).apply {
                order(ByteOrder.nativeOrder())
                rewind()
            }

            val intValues = IntArray(inputWidth * inputHeight)
            scaledBitmap.getPixels(intValues, 0, inputWidth, 0, 0, inputWidth, inputHeight)

            // Standard MobileFaceNet / FaceNet normalization: [-1.0, 1.0]
            for (pixel in intValues) {
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF

                inputBuffer.putFloat((r - 127.5f) / 128.0f)
                inputBuffer.putFloat((g - 127.5f) / 128.0f)
                inputBuffer.putFloat((b - 127.5f) / 128.0f)
            }

            val outputBuffer = Array(1) { FloatArray(embeddingDim) }
            currentInterpreter.run(inputBuffer, outputBuffer)

            // Return L2-normalized vector
            ImageUtils.l2Normalize(outputBuffer[0])
        } catch (e: Exception) {
            Log.e(TAG, "Error running TFLite face embedding, reverting to fallback descriptor", e)
            generateFallbackEmbedding(faceBitmap)
        }
    }

    /**
     * Deterministic spatial-color descriptor used if model file is not present in assets.
     */
    private fun generateFallbackEmbedding(faceBitmap: Bitmap): FloatArray {
        val targetSize = 32
        val scaled = Bitmap.createScaledBitmap(faceBitmap, targetSize, targetSize, false)
        val pixels = IntArray(targetSize * targetSize)
        scaled.getPixels(pixels, 0, targetSize, 0, 0, targetSize, targetSize)

        val vector = FloatArray(128)
        val blockSize = pixels.size / 128

        for (i in 0 until 128) {
            var sumR = 0f
            var sumG = 0f
            var sumB = 0f
            val start = i * blockSize
            val end = max(start + 1, (i + 1) * blockSize)

            for (j in start until end) {
                val p = pixels[j]
                sumR += ((p shr 16) and 0xFF) / 255f
                sumG += ((p shr 8) and 0xFF) / 255f
                sumB += (p and 0xFF) / 255f
            }
            vector[i] = (sumR * 0.299f + sumG * 0.587f + sumB * 0.114f) / (end - start)
        }

        // Subtract mean to make vector discriminative across different faces
        val mean = vector.average().toFloat()
        for (i in vector.indices) {
            vector[i] -= mean
        }

        return ImageUtils.l2Normalize(vector)
    }

    fun close() {
        try {
            interpreter?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
