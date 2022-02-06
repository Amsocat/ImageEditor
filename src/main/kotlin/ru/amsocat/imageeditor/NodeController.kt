package ru.amsocat.imageeditor

import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Point2D
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.input.ClipboardContent
import javafx.scene.input.DataFormat
import javafx.scene.input.TransferMode
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import kotlinx.serialization.Serializable
import java.util.*

var stateAddLink = DataFormat("linkAdd")
var parentId = DataFormat("parentId")
var stateAddNode = DataFormat("nodeAdd")
const val INPUTState = "input"
const val OUTPUTState = "output"
const val INTClass = "int"
const val FLOATClass = "float"
const val STRINGClass = "string"
const val IMAGEClass = "image"

@Serializable
data class JsonNode(
    var callableName: String,
    var x: Double,
    var y: Double,
    var content: String = ""
)

open class NodeController : BorderPane() {
    @FXML
    lateinit var deleteNode: Button

    @FXML
    lateinit var outputImageView: ImageView

    @FXML
    lateinit var nodeName: Label

    @FXML
    lateinit var inputVBox: VBox

    @FXML
    private lateinit var mainLayout: BorderPane

    @FXML
    lateinit var nodeContentVBox: VBox

    @FXML
    lateinit var outputVBox: VBox

    @FXML
    fun initialize() {
        id = UUID.randomUUID().toString()
        nodeContentVBox.onDragDetected = EventHandler { mouseEvent ->
            val offset = Point2D(
                mouseEvent.sceneX - mainLayout.layoutX,
                mouseEvent.sceneY - mainLayout.layoutY
            )
            mainLayout.parent.onDragOver = EventHandler { dragEvent ->
                mainLayout.layoutX = dragEvent.sceneX - offset.x
                mainLayout.layoutY = dragEvent.sceneY - offset.y
                dragEvent.acceptTransferModes(*TransferMode.ANY)
                dragEvent.consume()
            }
            mainLayout.parent.onDragDropped = EventHandler { dragEvent ->
                mainLayout.parent.onDragOver = null
                mainLayout.parent.onDragDropped = null
                dragEvent.isDropCompleted = true
                dragEvent.consume()
            }
            val content = ClipboardContent()
            content.putString("node")
            mainLayout.startDragAndDrop(*TransferMode.ANY).setContent(content)
            mouseEvent.consume()
        }

        nodeContentVBox.onDragDone = EventHandler { dragEvent ->
            mainLayout.parent.onDragOver = null
            mainLayout.parent.onDragDropped = null
            dragEvent.consume()
        }
        deleteNode.onAction = EventHandler {
            this.deleteMyself()
        }
    }

    private fun deleteMyself() {
        inputVBox.children.forEach { i ->
            (i as NodeLinkController).deleteAllNodes()
        }
        outputVBox.children.forEach { i ->
            (i as NodeLinkController).deleteAllNodes()
        }
        (this.parent as Pane).children.remove(this)
    }

    open fun toSerial(): JsonNode {
        return JsonNode(this.javaClass.simpleName, this.layoutX, this.layoutY)
    }

    open fun fromSerial(js: JsonNode) {
        this.layoutX = js.x
        this.layoutY = js.y
    }

    init {
        val fxmlLoader = FXMLLoader(javaClass.getResource("node.fxml"))
        fxmlLoader.setRoot(this)
        fxmlLoader.setController(this)
        fxmlLoader.load<NodeController>()
    }
}
