package ru.amsocat.imageeditor

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage

class ImageEditor : Application() {
    override fun start(stage: Stage) {
        val mainScene = Scene(MainActivity(), 1280.0, 720.0)
        stage.title = "ImageEditor"
        stage.scene = mainScene
        stage.show()
    }
}

fun main() {
    nu.pattern.OpenCV.loadLocally()
    Application.launch(ImageEditor::class.java)
}