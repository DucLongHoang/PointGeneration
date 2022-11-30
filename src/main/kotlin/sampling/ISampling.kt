package sampling

import java.awt.Point
import java.awt.Shape

/**
 * Interface [ISampling] - for classes that will generate points in different ways.
 *
 * @version 14.11.2022 (dd.mm.yyyy)
 * @author Duc Long Hoang
 */
interface ISampling {
    /**
     * Method generates [k] number of points in a non-convex [polygon].
     */
    fun generatePoints(polygon: Shape, k: Int): List<Point>
}