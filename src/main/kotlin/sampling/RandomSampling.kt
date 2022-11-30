package sampling

import java.awt.Point
import java.awt.Shape
import kotlin.random.Random

/**
 * [RandomSampling] class - implements [ISampling].
 * Generate points in a stochastic (random) manner.
 *
 * @version 14.11.2022 (dd.mm.yyyy)
 * @author Duc Long Hoang
 */
class RandomSampling : ISampling {
    override fun generatePoints(polygon: Shape, k: Int): List<Point> {
        val generatedPoints: MutableList<Point> = ArrayList()
        val boundingRect = polygon.bounds
        val r = Random

        while (generatedPoints.size != k) {
            val x: Double = (r.nextDouble() * boundingRect.width) + boundingRect.minX
            val y: Double = (r.nextDouble() * boundingRect.height) + boundingRect.minY
            if (polygon.contains(x, y)){
                generatedPoints.add(Point(x.toInt(), y.toInt()))
            }
        }
        return generatedPoints
    }
}