package ru.amsocat.imageeditor

import javafx.beans.value.ChangeListener
import javafx.embed.swing.SwingFXUtils.fromFXImage
import javafx.embed.swing.SwingFXUtils.toFXImage
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.SnapshotParameters
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.stage.Stage
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.File
import javax.imageio.ImageIO

class FloatClass : NodeController() {
    private val output = TextField("0.0")

    override fun toSerial(): JsonNode {
        val tmp = super.toSerial()
        tmp.content = output.text
        return tmp
    }

    override fun fromSerial(js: JsonNode) {
        super.fromSerial(js)
        this.output.text = js.content
    }

    init {
        output.textProperty().addListener { _, oldValue, newValue ->
            try {
                output.text = newValue.toDouble().toString()
            } catch (_: Exception) {
                if (newValue !== "") {
                    output.text = oldValue
                }
            }
        }
        this.nodeContentVBox.children.remove(this.outputImageView)
        this.nodeContentVBox.children.add(output)
        val tmp = NodeLinkController(this, "Out", OUTPUTState, FLOATClass, output)
        this.outputVBox.children.add(tmp)
        this.nodeName.text = "Float"
    }
}

class IntClass : NodeController() {
    private val output = TextField("0")

    override fun toSerial(): JsonNode {
        val tmp = super.toSerial()
        tmp.content = output.text
        return tmp
    }

    override fun fromSerial(js: JsonNode) {
        super.fromSerial(js)
        this.output.text = js.content
    }

    init {
        output.textProperty().addListener { _, oldValue, newValue ->
            try {
                output.text = newValue.toInt().toString()
            } catch (_: Exception) {
                if (newValue !== "") {
                    output.text = oldValue
                }
            }
        }
        this.nodeContentVBox.children.remove(this.outputImageView)
        this.nodeContentVBox.children.add(output)
        val tmp = NodeLinkController(this, "Out", OUTPUTState, INTClass, output)
        this.outputVBox.children.add(tmp)
        this.nodeName.text = "Integer"
    }
}

class StringClass : NodeController() {
    private val output = TextField("")

    override fun toSerial(): JsonNode {
        val tmp = super.toSerial()
        tmp.content = output.text
        return tmp
    }

    override fun fromSerial(js: JsonNode) {
        super.fromSerial(js)
        this.output.text = js.content
    }


    init {
        this.nodeContentVBox.children.remove(this.outputImageView)
        this.nodeContentVBox.children.add(output)
        val tmp = NodeLinkController(this, "Out", OUTPUTState, STRINGClass, output)
        this.outputVBox.children.add(tmp)
        this.nodeName.text = "String"
    }
}

class InputImage : NodeController() {
    private val fileButton = Button("Choose image")
    private var image: Image? = null
    private val link = NodeLinkController(this, "Out", OUTPUTState, IMAGEClass, this.outputImageView)
    private var file: File? = null
    override fun toSerial(): JsonNode {
        val tmp = super.toSerial()
        tmp.content = this.file.toString()
        return tmp
    }

    override fun fromSerial(js: JsonNode) {
        super.fromSerial(js)
        if (js.content != "null") {
            this.file = File(js.content)
            this.image = toFXImage(ImageIO.read(this.file), null)
        }
        this.outputImageView.image = this.image
    }

    init {
        fileButton.onAction = EventHandler {
            val fileChooser = FileChooser()
            fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"))
            this.file = fileChooser.showOpenDialog(Stage())
            if (this.file != null) {
                image = toFXImage(ImageIO.read(this.file), null)
                if (image !== null) {
                    this.outputImageView.image = image
                }
            }
        }
        this.nodeContentVBox.children.add(fileButton)
        this.outputVBox.children.add(link)
        this.nodeName.text = "Image"
    }
}

class AddText : NodeController() {
    private var image: Image? = null
    private var textX = 0
    private var textY = 0
    private var text = ""
    private val inputImage = NodeLinkController(this, "Img", INPUTState, IMAGEClass)
    private val inputIntX = NodeLinkController(this, "X", INPUTState, INTClass)
    private val inputIntY = NodeLinkController(this, "Y", INPUTState, INTClass)
    private val inputStringText = NodeLinkController(this, "Text", INPUTState, STRINGClass)
    private val outputImage = NodeLinkController(this, "Out", OUTPUTState, IMAGEClass, this.outputImageView)
    private val inputImageListener = ChangeListener<Image> { _, _, newValue ->
        this.image = newValue
        this.update()
    }
    private val inputIntXListener = ChangeListener<String> { _, _, newValue ->
        if (newValue == "") {
            this.textX = 0
        } else {
            this.textX = newValue.toInt()
        }
        this.update()
    }
    private val inputIntYListener = ChangeListener<String> { _, _, newValue ->
        if (newValue == "") {
            this.textY = 0
        } else {
            this.textY = newValue.toInt()
        }
        this.update()
    }
    private val inputStringTextListener = ChangeListener<String> { _, _, newValue ->
        this.text = newValue
        this.update()
    }

    private fun update() {
        var width = 0.0
        var height = 0.0
        if (this.image !== null) width = this.image!!.width
        if (this.image !== null) height = this.image!!.height
        val tmp = Canvas(width, height)
        val tmp2 = tmp.graphicsContext2D
        tmp2.drawImage(this.image, 0.0, 0.0)
        tmp2.fillText(this.text, this.textX.toDouble(), this.textY.toDouble())
        val snapParam = SnapshotParameters()
        snapParam.fill = Color.TRANSPARENT
        this.outputImageView.image = tmp.snapshot(snapParam, null)
    }

    init {
        inputImage.factory = fun(source: NodeLinkController) {
            this.image = (source.outputNode as ImageView).image
            source.outputNode.imageProperty().addListener(inputImageListener)
        }
        inputImage.defactory = fun(source: NodeLinkController) {
            (source.outputNode as ImageView).imageProperty().removeListener(inputImageListener)
        }
        inputIntX.factory = fun(source: NodeLinkController) {
            this.textX = (source.outputNode as TextField).text.toInt()
            source.outputNode.textProperty().addListener(inputIntXListener)
        }
        inputIntX.defactory = fun(source: NodeLinkController) {
            (source.outputNode as TextField).textProperty().removeListener(inputIntXListener)
        }
        inputIntY.factory = fun(source: NodeLinkController) {
            this.textY = (source.outputNode as TextField).text.toInt()
            source.outputNode.textProperty().addListener(inputIntYListener)
        }
        inputIntY.defactory = fun(source: NodeLinkController) {
            (source.outputNode as TextField).textProperty().removeListener(inputIntYListener)
        }
        inputStringText.factory = fun(source: NodeLinkController) {
            this.text = (source.outputNode as TextField).text
            source.outputNode.textProperty().addListener(inputStringTextListener)
        }
        inputStringText.defactory = fun(source: NodeLinkController) {
            (source.outputNode as TextField).textProperty().removeListener(inputStringTextListener)
        }
        this.inputVBox.children.addAll(inputImage, inputIntX, inputIntY, inputStringText)
        this.outputVBox.children.add(this.outputImage)
        this.nodeName.text = "Add text"
    }
}

class AddImage : NodeController() {
    private var image: Image? = null
    private var imageX = 0.0
    private var imageY = 0.0
    private var addImage: Image? = null
    private val inputImage = NodeLinkController(this, "Img", INPUTState, IMAGEClass)
    private val inputIntX = NodeLinkController(this, "X", INPUTState, INTClass)
    private val inputIntY = NodeLinkController(this, "Y", INPUTState, INTClass)
    private val inputAddImage = NodeLinkController(this, "Add", INPUTState, IMAGEClass)
    private val outputImage = NodeLinkController(this, "Out", OUTPUTState, IMAGEClass, this.outputImageView)
    private val inputImageListener = ChangeListener<Image> { _, _, newValue ->
        this.image = newValue
        this.update()
    }
    private val inputIntXListener = ChangeListener<String> { _, _, newValue ->
        if (newValue == "") {
            this.imageX = 0.0
        } else {
            this.imageX = newValue.toDouble()
        }
        this.update()
    }
    private val inputIntYListener = ChangeListener<String> { _, _, newValue ->
        if (newValue == "") {
            this.imageY = 0.0
        } else {
            this.imageY = newValue.toDouble()
        }
        this.update()
    }
    private val inputAddImageListener = ChangeListener<Image> { _, _, newValue ->
        this.addImage = newValue
        this.update()
    }

    private fun update() {
        var width = 0.0
        var height = 0.0
        if (this.image !== null) width = this.image!!.width
        if (this.image !== null) height = this.image!!.height
        val tmp = Canvas(width, height)
        val tmp2 = tmp.graphicsContext2D
        tmp2.drawImage(this.image, 0.0, 0.0)
        tmp2.drawImage(this.addImage, this.imageX, this.imageY)
        val snapParam = SnapshotParameters()
        snapParam.fill = Color.TRANSPARENT
        this.outputImageView.image = tmp.snapshot(snapParam, null)
    }

    init {
        inputImage.factory = fun(source: NodeLinkController) {
            this.image = (source.outputNode as ImageView).image
            this.update()
            source.outputNode.imageProperty().addListener(inputImageListener)
        }
        inputImage.defactory = fun(source: NodeLinkController) {
            (source.outputNode as ImageView).imageProperty().removeListener(inputImageListener)
        }
        inputIntX.factory = fun(source: NodeLinkController) {
            this.imageX = (source.outputNode as TextField).text.toDouble()
            this.update()
            source.outputNode.textProperty().addListener(inputIntXListener)
        }
        inputIntX.defactory = fun(source: NodeLinkController) {
            (source.outputNode as TextField).textProperty().removeListener(inputIntXListener)
        }
        inputIntY.factory = fun(source: NodeLinkController) {
            this.imageY = (source.outputNode as TextField).text.toDouble()
            this.update()
            source.outputNode.textProperty().addListener(inputIntYListener)
        }
        inputIntY.defactory = fun(source: NodeLinkController) {
            (source.outputNode as TextField).textProperty().removeListener(inputIntYListener)
        }
        inputAddImage.factory = fun(source: NodeLinkController) {
            this.addImage = (source.outputNode as ImageView).image
            this.update()
            source.outputNode.imageProperty().addListener(inputAddImageListener)
        }
        inputAddImage.defactory = fun(source: NodeLinkController) {
            (source.outputNode as ImageView).imageProperty().removeListener(inputAddImageListener)
        }
        this.inputVBox.children.addAll(inputImage, inputIntX, inputIntY, inputAddImage)
        this.outputVBox.children.add(this.outputImage)
        this.nodeName.text = "Add image"
    }
}

class GrayFilterClass : NodeController() {
    private var image: Image? = null
    private val inputImage = NodeLinkController(this, "Img", INPUTState, IMAGEClass)
    private val outputImage = NodeLinkController(this, "Out", OUTPUTState, IMAGEClass, this.outputImageView)
    private val inputImageListener = ChangeListener<Image> { _, _, newValue ->
        this.image = newValue
        this.update()
    }

    private fun update() {
        if (this.image !== null) {
            val mat = img2mat(this.image!!)
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY)
            this.outputImageView.image = mat2Image(mat)
        }
    }

    init {
        inputImage.factory = fun(source: NodeLinkController) {
            this.image = (source.outputNode as ImageView).image
            this.update()
            source.outputNode.imageProperty().addListener(inputImageListener)
        }
        inputImage.defactory = fun(source: NodeLinkController) {
            (source.outputNode as ImageView).imageProperty().removeListener(inputImageListener)
        }
        this.inputVBox.children.add(inputImage)
        this.outputVBox.children.add(this.outputImage)
        this.nodeName.text = "Gray Filter"
    }
}

class BrightnessClass : NodeController() {
    private var image: Image? = null
    private var level = 0.0
    private val inputImage = NodeLinkController(this, "Img", INPUTState, IMAGEClass)
    private val inputLevel = NodeLinkController(this, "Bright", INPUTState, FLOATClass)
    private val outputImage = NodeLinkController(this, "Out", OUTPUTState, IMAGEClass, this.outputImageView)
    private val inputImageListener = ChangeListener<Image> { _, _, newValue ->
        this.image = newValue
        this.update()
    }
    private val inputLevelListener = ChangeListener<String> { _, _, newValue ->
        if (newValue == "") {
            this.level = 0.0
        } else {
            this.level = newValue.toDouble()
        }
        this.update()
    }

    private fun update() {
        if (this.image !== null) {
            val mat = img2mat(this.image!!)
            mat.convertTo(mat, -1, this.level)
            this.outputImageView.image = mat2Image(mat)
        }
    }

    init {
        inputImage.factory = fun(source: NodeLinkController) {
            this.image = (source.outputNode as ImageView).image
            this.update()
            source.outputNode.imageProperty().addListener(inputImageListener)
        }
        inputImage.defactory = fun(source: NodeLinkController) {
            (source.outputNode as ImageView).imageProperty().removeListener(inputImageListener)
        }
        inputLevel.factory = fun(source: NodeLinkController) {
            this.level = (source.outputNode as TextField).text.toDouble()
            this.update()
            source.outputNode.textProperty().addListener(inputLevelListener)
        }
        inputLevel.defactory = fun(source: NodeLinkController) {
            (source.outputNode as TextField).textProperty().removeListener(inputLevelListener)
        }
        this.inputVBox.children.addAll(inputImage, inputLevel)
        this.outputVBox.children.add(this.outputImage)
        this.nodeName.text = "Brightness Filter"
    }
}

class SepiaFilterClass : NodeController() {
    private var image: Image? = null
    private val inputImage = NodeLinkController(this, "Img", INPUTState, IMAGEClass)
    private val outputImage = NodeLinkController(this, "Out", OUTPUTState, IMAGEClass, this.outputImageView)
    private val inputImageListener = ChangeListener<Image> { _, _, newValue ->
        this.image = newValue
        this.update()
    }

    private fun update() {
        if (this.image !== null) {
            val img = fromFXImage(this.image!!, null)
            for (y in 0 until img.height) {
                for (x in 0 until img.width) {
                    //Retrieving the values of a pixel
                    val pixel: Int = img.getRGB(x, y)
                    //Creating a Color object from pixel value
                    var color = java.awt.Color(pixel, true)
                    //Retrieving the R G B values
                    var red = color.red
                    var green = color.green
                    var blue = color.blue
                    val avg = (red + green + blue) / 3
                    val depth = 20
                    val intensity = 30
                    red = avg + depth * 2
                    green = avg + depth
                    blue = avg - intensity
                    //Making sure that RGB values lies between 0-255
                    if (red > 255) red = 255
                    if (green > 255) green = 255
                    if (blue > 255) blue = 255
                    if (blue < 0) blue = 0
                    //Creating new Color object
                    color = java.awt.Color(red.toFloat() / 255, green.toFloat() / 255, blue.toFloat() / 255)
                    //Setting new Color object to the image
                    img.setRGB(x, y, color.rgb)
                }
            }
            this.outputImageView.image = toFXImage(img, null)
        }
    }

    init {
        inputImage.factory = fun(source: NodeLinkController) {
            this.image = (source.outputNode as ImageView).image
            this.update()
            source.outputNode.imageProperty().addListener(inputImageListener)
        }
        inputImage.defactory = fun(source: NodeLinkController) {
            (source.outputNode as ImageView).imageProperty().removeListener(inputImageListener)
        }
        this.inputVBox.children.add(inputImage)
        this.outputVBox.children.add(this.outputImage)
        this.nodeName.text = "Sepia Filter"
    }
}

class NegativeFilterClass : NodeController() {
    private var image: Image? = null
    private val inputImage = NodeLinkController(this, "Img", INPUTState, IMAGEClass)
    private val outputImage = NodeLinkController(this, "Out", OUTPUTState, IMAGEClass, this.outputImageView)
    private val inputImageListener = ChangeListener<Image> { _, _, newValue ->
        this.image = newValue
        this.update()
    }

    private fun update() {
        if (this.image !== null) {
            val img = fromFXImage(this.image!!, null)
            for (y in 0 until img.height) {
                for (x in 0 until img.width) {
                    val pixel: Int = img.getRGB(x, y)
                    var color = java.awt.Color(pixel, true)
                    color = java.awt.Color(255 - color.red, 255 - color.green, 255 - color.blue)
                    img.setRGB(x, y, color.rgb)
                }
            }
            this.outputImageView.image = toFXImage(img, null)
        }
    }

    init {
        inputImage.factory = fun(source: NodeLinkController) {
            this.image = (source.outputNode as ImageView).image
            this.update()
            source.outputNode.imageProperty().addListener(inputImageListener)
        }
        inputImage.defactory = fun(source: NodeLinkController) {
            (source.outputNode as ImageView).imageProperty().removeListener(inputImageListener)
        }
        this.inputVBox.children.add(inputImage)
        this.outputVBox.children.add(this.outputImage)
        this.nodeName.text = "Negative Filter"
    }
}

class BlurFilterClass : NodeController() {
    private var image: Image? = null
    private var kernelSize = 1
    private val inputImage = NodeLinkController(this, "Img", INPUTState, IMAGEClass)
    private val inputLevel = NodeLinkController(this, "KernelSize", INPUTState, INTClass)
    private val outputImage = NodeLinkController(this, "Out", OUTPUTState, IMAGEClass, this.outputImageView)
    private val inputImageListener = ChangeListener<Image> { _, _, newValue ->
        this.image = newValue
        this.update()
    }
    private val inputLevelListener = ChangeListener<String> { _, _, newValue ->
        if (newValue == "") {
            this.kernelSize = 0
        } else {
            this.kernelSize = newValue.toInt()
        }
        this.update()
    }

    private fun update() {
        if (this.image !== null) {
            val mat = img2mat(this.image!!)
            try {
                Imgproc.GaussianBlur(mat, mat, Size(this.kernelSize.toDouble(), this.kernelSize.toDouble()), 0.0)
            } catch (_: Exception) {
            }
            this.outputImageView.image = mat2Image(mat)
        }
    }

    init {
        inputImage.factory = fun(source: NodeLinkController) {
            this.image = (source.outputNode as ImageView).image
            this.update()
            source.outputNode.imageProperty().addListener(inputImageListener)
        }
        inputImage.defactory = fun(source: NodeLinkController) {
            (source.outputNode as ImageView).imageProperty().removeListener(inputImageListener)
        }
        inputLevel.factory = fun(source: NodeLinkController) {
            this.kernelSize = (source.outputNode as TextField).text.toInt()
            this.update()
            source.outputNode.textProperty().addListener(inputLevelListener)
        }
        inputLevel.defactory = fun(source: NodeLinkController) {
            (source.outputNode as TextField).textProperty().removeListener(inputLevelListener)
        }
        this.inputVBox.children.addAll(inputImage, inputLevel)
        this.outputVBox.children.add(this.outputImage)
        this.nodeName.text = "Blur Filter"
    }
}

class TransformScale : NodeController() {
    private var image: Image? = null
    private var imageScaleX = 1.0
    private var imageScaleY = 1.0
    private val inputImage = NodeLinkController(this, "Img", INPUTState, IMAGEClass)
    private val inputFloatX = NodeLinkController(this, "fX", INPUTState, FLOATClass)
    private val inputFloatY = NodeLinkController(this, "fY", INPUTState, FLOATClass)
    private val outputImage = NodeLinkController(this, "Out", OUTPUTState, IMAGEClass, this.outputImageView)
    private val inputImageListener = ChangeListener<Image> { _, _, newValue ->
        this.image = newValue
        this.update()
    }
    private val inputIntXListener = ChangeListener<String> { _, _, newValue ->
        if (newValue == "") {
            this.imageScaleX = 1.0
        } else {
            this.imageScaleX = newValue.toDouble()
        }
        this.update()
    }
    private val inputIntYListener = ChangeListener<String> { _, _, newValue ->
        if (newValue == "") {
            this.imageScaleY = 1.0
        } else {
            this.imageScaleY = newValue.toDouble()
        }
        this.update()
    }

    private fun update() {
        if (this.image !== null) {
            val mat = img2mat(this.image!!)

            try {
                val mat2 = Mat()
                Imgproc.resize(mat, mat2, Size(0.0, 0.0), this.imageScaleX, this.imageScaleY)
                this.outputImageView.image = mat2Image(mat2)
            } catch (e: Exception) {
                this.outputImageView.image = mat2Image(mat)
                println(e)
            }

        }
    }

    init {
        inputImage.factory = fun(source: NodeLinkController) {
            this.image = (source.outputNode as ImageView).image
            this.update()
            source.outputNode.imageProperty().addListener(inputImageListener)
        }
        inputImage.defactory = fun(source: NodeLinkController) {
            (source.outputNode as ImageView).imageProperty().removeListener(inputImageListener)
        }
        inputFloatX.factory = fun(source: NodeLinkController) {
            this.imageScaleX = (source.outputNode as TextField).text.toDouble()
            this.update()
            source.outputNode.textProperty().addListener(inputIntXListener)
        }
        inputFloatX.defactory = fun(source: NodeLinkController) {
            (source.outputNode as TextField).textProperty().removeListener(inputIntXListener)
        }
        inputFloatY.factory = fun(source: NodeLinkController) {
            this.imageScaleY = (source.outputNode as TextField).text.toDouble()
            this.update()
            source.outputNode.textProperty().addListener(inputIntYListener)
        }
        inputFloatY.defactory = fun(source: NodeLinkController) {
            (source.outputNode as TextField).textProperty().removeListener(inputIntYListener)
        }
        this.inputVBox.children.addAll(inputImage, inputFloatX, inputFloatY)
        this.outputVBox.children.add(this.outputImage)
        this.nodeName.text = "Transform Scale"
    }
}

class TransformMove : NodeController() {
    private var image: Image? = null
    private var imageX = 0.0
    private var imageY = 0.0
    private val inputImage = NodeLinkController(this, "Img", INPUTState, IMAGEClass)
    private val inputFloatX = NodeLinkController(this, "fX", INPUTState, FLOATClass)
    private val inputFloatY = NodeLinkController(this, "fY", INPUTState, FLOATClass)
    private val outputImage = NodeLinkController(this, "Out", OUTPUTState, IMAGEClass, this.outputImageView)
    private val inputImageListener = ChangeListener<Image> { _, _, newValue ->
        this.image = newValue
        this.update()
    }
    private val inputIntXListener = ChangeListener<String> { _, _, newValue ->
        if (newValue == "") {
            this.imageX = 1.0
        } else {
            this.imageX = newValue.toDouble()
        }
        this.update()
    }
    private val inputIntYListener = ChangeListener<String> { _, _, newValue ->
        if (newValue == "") {
            this.imageY = 1.0
        } else {
            this.imageY = newValue.toDouble()
        }
        this.update()
    }

    private fun update() {
        if (this.image !== null) {
            val tmp = Canvas(this.image!!.width + this.imageX, this.image!!.height + this.imageY)
            val tmp2 = tmp.graphicsContext2D
            tmp2.drawImage(this.image, this.imageX, this.imageY)
            val tmp3 = SnapshotParameters()
            tmp3.fill = Color.WHITE
            this.outputImageView.image = tmp.snapshot(tmp3, null)
        }
    }

    init {
        inputImage.factory = fun(source: NodeLinkController) {
            this.image = (source.outputNode as ImageView).image
            this.update()
            source.outputNode.imageProperty().addListener(inputImageListener)
        }
        inputImage.defactory = fun(source: NodeLinkController) {
            (source.outputNode as ImageView).imageProperty().removeListener(inputImageListener)
        }
        inputFloatX.factory = fun(source: NodeLinkController) {
            this.imageX = (source.outputNode as TextField).text.toDouble()
            this.update()
            source.outputNode.textProperty().addListener(inputIntXListener)
        }
        inputFloatX.defactory = fun(source: NodeLinkController) {
            (source.outputNode as TextField).textProperty().removeListener(inputIntXListener)
        }
        inputFloatY.factory = fun(source: NodeLinkController) {
            this.imageY = (source.outputNode as TextField).text.toDouble()
            this.update()
            source.outputNode.textProperty().addListener(inputIntYListener)
        }
        inputFloatY.defactory = fun(source: NodeLinkController) {
            (source.outputNode as TextField).textProperty().removeListener(inputIntYListener)
        }
        this.inputVBox.children.addAll(inputImage, inputFloatX, inputFloatY)
        this.outputVBox.children.add(this.outputImage)
        this.nodeName.text = "Transform Move"
    }
}

class TransformRotate : NodeController() {
    private var image: Image? = null
    private var rad = 0.0
    private val inputImage = NodeLinkController(this, "Img", INPUTState, IMAGEClass)
    private val inputRad = NodeLinkController(this, "fRad", INPUTState, FLOATClass)
    private val outputImage = NodeLinkController(this, "Out", OUTPUTState, IMAGEClass, this.outputImageView)
    private val inputImageListener = ChangeListener<Image> { _, _, newValue ->
        this.image = newValue
        this.update()
    }
    private val inputIntXListener = ChangeListener<String> { _, _, newValue ->
        if (newValue == "") {
            this.rad = 0.0
        } else {
            this.rad = newValue.toDouble()
        }
        this.update()
    }

    private fun update() {
        if (this.image !== null) {
            val tmp = Canvas(this.image!!.width, this.image!!.height)
            tmp.rotate = this.rad * 180 / Math.PI
            val tmp2 = tmp.graphicsContext2D
            tmp2.drawImage(this.image, 0.0, 0.0)
            val tmp3 = SnapshotParameters()
            tmp3.fill = Color.WHITE
            this.outputImageView.image = tmp.snapshot(tmp3, null)
        }
    }

    init {
        inputImage.factory = fun(source: NodeLinkController) {
            this.image = (source.outputNode as ImageView).image
            this.update()
            source.outputNode.imageProperty().addListener(inputImageListener)
        }
        inputImage.defactory = fun(source: NodeLinkController) {
            (source.outputNode as ImageView).imageProperty().removeListener(inputImageListener)
        }
        inputRad.factory = fun(source: NodeLinkController) {
            this.rad = (source.outputNode as TextField).text.toDouble()
            this.update()
            source.outputNode.textProperty().addListener(inputIntXListener)
        }
        inputRad.defactory = fun(source: NodeLinkController) {
            (source.outputNode as TextField).textProperty().removeListener(inputIntXListener)
        }
        this.inputVBox.children.addAll(inputImage, inputRad)
        this.outputVBox.children.add(this.outputImage)
        this.nodeName.text = "Transform Rotate"
    }
}

class ResultImageClass : NodeController() {
    private var image: Image? = null
    var imageV: ImageView? = null
    private val inputImage = NodeLinkController(this, "", INPUTState, IMAGEClass)
    private val inputImageListener = ChangeListener<Image> { _, _, newValue ->
        this.image = newValue
        this.update()
    }

    private fun update() {
        if (this.image !== null) {
            imageV!!.image = this.image
            imageV!!.fitHeight = this.image!!.height
            imageV!!.fitWidth = this.image!!.width
        }
    }

    init {
        (this.deleteNode.parent as Pane).children.remove(this.deleteNode)
        (this.outputImageView.parent as Pane).children.remove(this.outputImageView)
        inputImage.factory = fun(source: NodeLinkController) {
            this.image = (source.outputNode as ImageView).image
            this.update()
            source.outputNode.imageProperty().addListener(inputImageListener)
        }
        inputImage.defactory = fun(source: NodeLinkController) {
            (source.outputNode as ImageView).imageProperty().removeListener(inputImageListener)
        }
        this.inputVBox.children.add(inputImage)
        HBox.setMargin(this.nodeName, Insets(0.0, 0.0, 0.0, 0.0))
        this.nodeName.text = "Result Image"
    }
}

