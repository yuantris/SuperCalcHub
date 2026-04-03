package com.core.app.supercalchub.features.mathboard

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

enum class DrawingTool {
    Freehand, Line, Rectangle, Circle, Trapezoid, Text, Polygon, Eraser
}

sealed class DrawElement {
    data class Freehand(
        val points: List<Offset>,
        val color: Color,
        val strokeWidth: Float
    ) : DrawElement()

    data class Line(
        val start: Offset,
        val end: Offset,
        val color: Color,
        val strokeWidth: Float
    ) : DrawElement()

    data class Rectangle(
        val topLeft: Offset,
        val bottomRight: Offset,
        val strokeColor: Color,
        val strokeWidth: Float,
        val fill: Boolean,
        val fillColor: Color
    ) : DrawElement()

    data class Ellipse(
        val topLeft: Offset,
        val bottomRight: Offset,
        val strokeColor: Color,
        val strokeWidth: Float,
        val fill: Boolean,
        val fillColor: Color
    ) : DrawElement()

    data class Trapezoid(
        val topLeft: Offset,
        val bottomRight: Offset,
        val topInsetRatio: Float = 0.3f,
        val strokeColor: Color,
        val strokeWidth: Float,
        val fill: Boolean,
        val fillColor: Color
    ) : DrawElement()

    data class Polygon(
        val center: Offset,
        val radius: Float,
        val sides: Int,
        val strokeColor: Color,
        val strokeWidth: Float,
        val fill: Boolean,
        val fillColor: Color
    ) : DrawElement()

    data class TextElement(
        val position: Offset,
        val text: String,
        val color: Color,
        val fontSize: Float
    ) : DrawElement()
}

data class PolygonSettings(
    val sides: Int = 6,
    val fill: Boolean = true,
    val strokeWidth: Float = 12f,
    val fillColor: Color = Color.Black,
    val strokeColor: Color = Color.Black
)
