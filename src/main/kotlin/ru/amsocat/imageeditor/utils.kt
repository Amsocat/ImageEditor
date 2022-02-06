package ru.amsocat.imageeditor

import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import org.opencv.core.CvType
import org.opencv.core.Mat
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte

fun img2mat(img: Image): Mat {
    val width = img.width.toInt()
    val height = img.height.toInt()
    val bufImg = SwingFXUtils.fromFXImage(img, null)
    val convertedImage = BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)
    convertedImage.graphics.drawImage(bufImg, 0, 0, null)
    val mat = Mat(height, width, CvType.CV_8UC3)
    mat.put(0, 0, (convertedImage.raster.dataBuffer as DataBufferByte).data)
    return mat
}

fun mat2Image(frame: Mat): Image? {
    return try {
        SwingFXUtils.toFXImage(matToBufferedImage(frame), null)
    } catch (e: java.lang.Exception) {
        System.err.println("Cannot convert the Mat obejct: $e")
        null
    }
}

private fun matToBufferedImage(original: Mat): BufferedImage {
    val image: BufferedImage?
    val width = original.width()
    val height = original.height()
    val channels = original.channels()
    val sourcePixels = ByteArray(width * height * channels)
    original[0, 0, sourcePixels]
    image = if (original.channels() > 1) {
        BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)
    } else {
        BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
    }
    val targetPixels = (image.raster.dataBuffer as DataBufferByte).data
    System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.size)
    return image
}
