/**
 * [Constants] object - global object for constants.
 * Kotlin objects are equivalent to Java Singletons.
 *
 * @version 14.11.2022 (dd.mm.yyyy)
 * @author Duc Long Hoang
 */
object Constants {
    // number of points to generate
    const val K = 10
    // radius of circle around point
    const val RADIUS = 25
    // rejection threshold of Poisson disk sampling
    const val REJECT_NUM = 30
    // number of auxiliary points for Voronoi sampling
    const val AUX_POINTS = 1000
    // directory of exported images
    const val OUTPUT_DIR = "img"
    // x,y coordinates of polygon
    val X_COORDINATES = intArrayOf(150, 250, 325, 375, 450, 275, 100)
    val Y_COORDINATES = intArrayOf(150, 100, 125, 225, 250, 375, 300)
}