package sampling

import Constants
import java.awt.Point
import java.awt.Shape
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * [VoronoiSampling] class - implements [ISampling].
 * Generate points by using K-means clustering approximating a centroidal Voronoi diagram.
 *
 * @version 14.11.2022 (dd.mm.yyyy)
 * @author Duc Long Hoang
 */
class VoronoiSampling : ISampling {

    override fun generatePoints(polygon: Shape, k: Int): List<Point> {
        val generatedPoints: MutableList<Point> = ArrayList()
//        val auxPoints: List<Point> = RandomSampling().generatePoints(polygon, AUX_POINTS)
        val auxPoints: List<Point> = RandomSampling().generatePoints(polygon, Constants.AUX_POINTS).sortedBy {
//             technique used when mapping a 2D-array to a 1D-array, sorting by furthest from origin
            (polygon.bounds.width * it.y) + it.x
        }
//        val r = Random

        val step: Double = (Constants.AUX_POINTS - 1.0) / (k - 1.0)
        (0 until k).forEach {
            val p = auxPoints[(it * step).roundToInt()]
            generatedPoints.add(Point(p))
        }

//        IntRange(1, k).forEach { _ ->
//            val rndIndex = r.nextInt(auxPoints.size)
//            val p = auxPoints[rndIndex]
//            generatedPoints.add(Point(p))
//        }

        while (kMeans(generatedPoints, auxPoints)) { }
        return generatedPoints
    }

    /**
     * Method executes the K-Means algorithm with a List of [centers] and a List of [auxPoints].
     * Returns true if any [Cluster] changed its center position, otherwise false.
     */
    private fun kMeans(centers: List<Point>, auxPoints: List<Point>): Boolean {
        val clusters: MutableMap<Point, Cluster> = HashMap()

        // init clusters
        centers.forEach { clusters[it] = Cluster(it) }

        // assign every point to a cluster
        auxPoints.forEach { auxPoint ->
            val closestCenter = centers.minByOrNull { center -> center.distance(auxPoint) }!!
            clusters[closestCenter]!!.clusterPoints.add(auxPoint)
        }

        // check if cluster center changes location
        var change = false
        clusters.forEach { change = change or it.value.recenter() }
        return change
    }

    /**
     * [Cluster] class - represents a k-means cluster.
     * Has a [clusterCenter] and [clusterPoints].
     *
     * @version 14.11.2022 (dd.mm.yyyy)
     * @author Duc Long Hoang
     */
    private class Cluster(val clusterCenter: Point) {
        val clusterPoints: MutableList<Point>

        init {
            clusterPoints = ArrayList()
        }

        /**
         * Method calculates a new [clusterCenter]. Return false if it remains the same otherwise true.
         */
        fun recenter(): Boolean {
            val oldCenter = clusterCenter.location
            clusterCenter.move(0, 0)
            clusterPoints.forEach { clusterCenter.translate(it.x, it.y) }
            clusterCenter.x /= clusterPoints.size.coerceAtLeast(1)
            clusterCenter.y /= clusterPoints.size.coerceAtLeast(1)

            return oldCenter != clusterCenter
        }
    }
}