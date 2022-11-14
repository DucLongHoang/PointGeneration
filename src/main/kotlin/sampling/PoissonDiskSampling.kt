package sampling

import java.awt.Point
import java.awt.Shape
import kotlin.math.*
import kotlin.random.Random
import Constants

/**
 * [PoissonDiskSampling] class - implements [ISampling].
 * Generate points using Poisson disk sampling method.
 *
 * @version 14.11.2022 (dd.mm.yyyy)
 * @author Duc Long Hoang
 */
class PoissonDiskSampling : ISampling {
    var radius: Int = Constants.RADIUS
    var rejectNum: Int = Constants.REJECT_NUM
    private val r: Random = Random
    private lateinit var luCorner: Point
    private lateinit var rlCorner: Point
    private fun cellSize(): Double = radius / sqrt(2.0)

    override fun generatePoints(polygon: Shape, k: Int): List<Point> {
        // Step 0 - initialization
        val width: Int = polygon.bounds.width
        val height: Int = polygon.bounds.height
        val cols: Int = ((width / cellSize()) + 1).toInt()
        val rows: Int = ((height / cellSize()) + 1).toInt()
        luCorner = Point(polygon.bounds.minX.toInt(), polygon.bounds.minY.toInt())
        rlCorner = Point(polygon.bounds.maxX.toInt(), polygon.bounds.maxY.toInt())

        val generatedPoints: MutableList<Point> = ArrayList()
        val activePoints: MutableList<Point> = ArrayList()
        val grid: Array<Array<Point?>> = Array(rows) { arrayOfNulls(cols) }

        // Step 1 - draw first Point in the middle of polygon,
        //          polygon might have a hole in the middle!!! -> then generate randomly
        val x: Int = width / 2
        val y: Int = height / 2
        var firstPoint: Point = createPoint(x, y)
        while (!polygon.contains(firstPoint)) {
            firstPoint = createPoint(r.nextInt(width), r.nextInt(height))
        }
        val i: Int = floor((firstPoint.x - luCorner.x) / cellSize()).toInt()
        val j: Int = floor((firstPoint.y - luCorner.y) / cellSize()).toInt()

        grid[j][i] = firstPoint
        generatedPoints.add(firstPoint)
        activePoints.add(firstPoint)

        // Step 2 - random uniform distribute points around firstPoint
        //          with distance = <[radius], 2x[radius]>
        while (activePoints.isNotEmpty() && generatedPoints.size < k) {
            val randomPoint: Point = activePoints.random()
            var found: Boolean = false

            // generate points around randomPoint while possible
            IntRange(1, rejectNum).asSequence().takeWhile { generatedPoints.size < k }.forEach { _ ->
                if (addNewPoint(polygon, grid, activePoints, generatedPoints, randomPoint)) found = true
            }
            // no more points can be generated around -> discard randomPoint
            if (!found) activePoints.remove(randomPoint)
        }
        return generatedPoints
    }

    /**
     * Method creates a [Point] with respect to the left-upper corner [luCorner].
     */
    private fun createPoint(x: Int, y: Int): Point = Point(luCorner.x + x, luCorner.y + y)

    /**
     * Method randomly generates a [Point] around [p] with
     * distance = <[radius], 2x[radius]> and random angle.
     * This distribution is not uniform as further generated points
     * will be sparse while closer points will be dense.
     */
    private fun generatePointAround(p: Point): Point {
        val distance: Double = (r.nextDouble() * radius) + radius
        val angle: Double = 2 * PI * r.nextDouble()
        val x: Double = distance * sin(angle)
        val y: Double = distance * cos(angle)
        return Point((p.x + x).toInt(), (p.y + y).toInt())
    }

    /**
     * Method tries to add new point [p] into the [grid]. Returns true on success, else false.
     * Also takes [polygon], [activePoints] and [generatedPoints] as parameter.
     */
    private fun addNewPoint(polygon: Shape, grid: Array<Array<Point?>>, activePoints: MutableList<Point>,
                            generatedPoints: MutableList<Point>, p: Point): Boolean {
        var found: Boolean = false
        val newPoint: Point = generatePointAround(p)

        if (!polygon.contains(newPoint)) return false

        var conflict: Boolean = false
        val col: Int = floor((newPoint.x - luCorner.x) / cellSize()).toInt()
        val row: Int = floor((newPoint.y - luCorner.y) / cellSize()).toInt()

        // check in neighbouring squares 5x5
        IntRange(-2, 2).asSequence().takeWhile { !conflict }.forEach { i ->
            IntRange(-2, 2).asSequence().takeWhile { !conflict }.forEach { j ->
                try {
                    val adjacentPoint: Point? = grid[row + i][col + j]
                    if (adjacentPoint != null && adjacentPoint.distance(newPoint) < radius )
                        conflict = true
                }
                // checking beyond bounding box of polygon -> gets ignored
                catch (_: ArrayIndexOutOfBoundsException) {}
            }
        }

        if (!conflict) {
            found = true
            grid[row][col] = newPoint
            generatedPoints.add(newPoint)
            activePoints.add(newPoint)
        }
        return found
    }
}