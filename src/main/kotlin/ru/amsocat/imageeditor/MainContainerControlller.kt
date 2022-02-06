package ru.amsocat.imageeditor

import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.embed.swing.SwingFXUtils.fromFXImage
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Button
import javafx.scene.control.TabPane
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.util.Duration
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URL
import java.util.*
import javax.imageio.ImageIO

@Serializable
data class JsonNodeLink(
    var firstNode: Int,
    var secondNode: Int,
    var inputPos: Int,
    var outputPos: Int
)

@Serializable
data class JsonNodeList(var nodes: MutableList<JsonNode>, var links: MutableList<JsonNodeLink>)

class MainActivity : TabPane() {

    @FXML
    private lateinit var resources: ResourceBundle

    @FXML
    private lateinit var location: URL

    @FXML
    private lateinit var brightnessButton: Button

    @FXML
    private lateinit var addImageButton: Button

    @FXML
    private lateinit var addTextButton: Button

    @FXML
    private lateinit var blurFilterButton: Button

    @FXML
    private lateinit var floatButton: Button

    @FXML
    private lateinit var globalOutputImageView: ImageView

    @FXML
    private lateinit var grayFilterButton: Button

    @FXML
    private lateinit var inputImageButton: Button

    @FXML
    private lateinit var intButton: Button

    @FXML
    private lateinit var invertFilterButton: Button

    @FXML
    private lateinit var nodeContainer: AnchorPane

    @FXML
    private lateinit var sepiaFilterButton: Button

    @FXML
    private lateinit var stringButton: Button

    @FXML
    private lateinit var transformMoveButton: Button

    @FXML
    private lateinit var transformRotateButton: Button

    @FXML
    private lateinit var transformScaleButton: Button

    @FXML
    private lateinit var saveImageButton: Button

    @FXML
    private lateinit var saveSchemeButton: Button

    @FXML
    private lateinit var loadSchemeButton: Button

    @FXML
    fun initialize() {
        floatButton.onAction = EventHandler {
            nodeContainer.children.add(FloatClass())
        }
        intButton.onAction = EventHandler {
            nodeContainer.children.add(IntClass())
        }
        stringButton.onAction = EventHandler {
            nodeContainer.children.add(StringClass())
        }
        inputImageButton.onAction = EventHandler {
            nodeContainer.children.add(InputImage())
        }
        addTextButton.onAction = EventHandler {
            nodeContainer.children.add(AddText())
        }
        addImageButton.onAction = EventHandler {
            nodeContainer.children.add(AddImage())
        }
        brightnessButton.onAction = EventHandler {
            nodeContainer.children.add(BrightnessClass())
        }
        grayFilterButton.onAction = EventHandler {
            nodeContainer.children.add(GrayFilterClass())
        }
        sepiaFilterButton.onAction = EventHandler {
            nodeContainer.children.add(SepiaFilterClass())
        }
        invertFilterButton.onAction = EventHandler {
            nodeContainer.children.add(NegativeFilterClass())
        }
        blurFilterButton.onAction = EventHandler {
            nodeContainer.children.add(BlurFilterClass())
        }
        transformMoveButton.onAction = EventHandler {
            nodeContainer.children.add(TransformMove())
        }
        transformRotateButton.onAction = EventHandler {
            nodeContainer.children.add(TransformRotate())
        }
        transformScaleButton.onAction = EventHandler {
            nodeContainer.children.add(TransformScale())
        }
        saveImageButton.onAction = EventHandler {
            if (this.globalOutputImageView.image !== null) {
                val fileChooser = FileChooser()
                fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"))
                val file = fileChooser.showSaveDialog(Stage())
                if (file != null) {
                    ImageIO.write(fromFXImage(this.globalOutputImageView.image, null), "png", file)
                }
            }
        }
        saveImageButton.onMouseEntered = EventHandler {
            saveImageButton.opacity = 1.0
        }
        saveImageButton.onMouseExited = EventHandler {
            saveImageButton.opacity = 0.6
        }
        saveSchemeButton.onAction = EventHandler {
            val fileChooser = FileChooser()
            fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Json Files", "*.json"))
            val file = fileChooser.showSaveDialog(Stage())
            if (file != null) {
                val nodes = mutableListOf<NodeController>()
                for (node in this.nodeContainer.children) {
                    if (node.javaClass.superclass == NodeController().javaClass) {
                        nodes.add(node as NodeController)
                    }
                }
                val nodeList = mutableListOf<JsonNode>()
                nodes.forEach { node ->
                    nodeList.add(node.toSerial())
                }
                val nodeLinkList = mutableListOf<JsonNodeLink>()
                for (node in nodes) {
                    for (node_link in node.inputVBox.children) {
                        val link = node_link as NodeLinkController
                        if (link.linked) {
                            nodeLinkList.add(
                                JsonNodeLink(
                                    nodes.indexOf(node),
                                    nodes.indexOf(link.sourceMainParent?.mainParent),
                                    node.inputVBox.children.indexOf(link),
                                    link.sourceMainParent!!.mainParent.outputVBox.children.indexOf(link.sourceMainParent)
                                )
                            )
                        }
                    }
                }
                file.writeText(Json.encodeToString(JsonNodeList(nodeList, nodeLinkList)))
            }
        }
        loadSchemeButton.onAction = EventHandler {
            val fileChooser = FileChooser()
            fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Json Files", "*.json"))
            val file = fileChooser.showOpenDialog(Stage())
            if (file != null){
                val lines = Json.decodeFromString<JsonNodeList>(file.readText())
                this.nodeContainer.children.clear()
                val nodes = mutableListOf<NodeController>()
                lines.nodes.forEach {
                    val newNode = this.getNode(it.callableName)
                    newNode.fromSerial(it)
                    nodes.add(newNode)
                    this.nodeContainer.children.add(newNode)
                }
                val timeline = Timeline(KeyFrame(Duration.millis(10.0), {
                    lines.links.forEach {
                        try {
                            (nodes[it.secondNode].outputVBox.children[it.outputPos] as NodeLinkController).makeConnection(
                                (nodes[it.firstNode].inputVBox.children[it.inputPos] as NodeLinkController).circleItem
                            )
                        } catch (e: Exception){
                            println(e)
                        }
                    }
                }))
                timeline.cycleCount = 1
                timeline.play()
            }
        }
        val tmp = ResultImageClass()
        tmp.imageV = this.globalOutputImageView
        nodeContainer.children.add(tmp)
    }

    private fun getNode(name: String): NodeController {
        return when (name) {
            "FloatClass" -> FloatClass()
            "IntClass" -> IntClass()
            "StringClass" -> StringClass()
            "InputImage" -> InputImage()
            "AddText" -> AddText()
            "AddImage" -> AddImage()
            "GrayFilterClass" -> GrayFilterClass()
            "BrightnessClass" -> BrightnessClass()
            "SepiaFilterClass" -> SepiaFilterClass()
            "NegativeFilterClass" -> NegativeFilterClass()
            "BlurFilterClass" -> BlurFilterClass()
            "TransformScale" -> TransformScale()
            "TransformMove" -> TransformMove()
            "TransformRotate" -> TransformRotate()
            "ResultImageClass" -> {
                val tmp = ResultImageClass()
                tmp.imageV = this.globalOutputImageView
                return tmp
            }
            else -> {
                NodeController()
            }
        }
    }

    init {
        val tmp = FXMLLoader(javaClass.getResource("mainContainer.fxml"))
        tmp.setRoot(this)
        tmp.setController(this)
        tmp.load<Any>()
    }
}
