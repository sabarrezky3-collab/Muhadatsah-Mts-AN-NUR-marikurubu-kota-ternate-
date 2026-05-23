package com.example

import java.io.File
import java.awt.Image
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun resizeLogoImage() {
    val paths = listOf(
      "src/main/res/drawable/img_app_icon_1779557473788.png",
      "app/src/main/res/drawable/img_app_icon_1779557473788.png",
      "/app/src/main/res/drawable/img_app_icon_1779557473788.png"
    )
    for (p in paths) {
      val file = File(p)
      if (file.exists()) {
        println("File found at $p. Size: ${file.length()} bytes.")
        try {
          val originalImage = ImageIO.read(file)
          if (originalImage != null) {
            val width = originalImage.width
            val height = originalImage.height
            println("Original dimensions: ${width}x${height}")
            if (width > 256 || height > 256) {
              val targetSize = 192
              val resultingImage = originalImage.getScaledInstance(targetSize, targetSize, Image.SCALE_SMOOTH)
              val outputImage = BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_ARGB)
              val g2d = outputImage.createGraphics()
              g2d.drawImage(resultingImage, 0, 0, null)
              g2d.dispose()
              ImageIO.write(outputImage, "png", file)
              println("Resized successfully to ${targetSize}x${targetSize} at $p!")
            } else {
              println("Image is already within 256x256 limit.")
            }
          }
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }
}
