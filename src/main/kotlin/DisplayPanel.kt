import sampling.PoissonDiskSampling
import sampling.RandomSampling
import sampling.VoronoiSampling
import java.awt.*
import java.io.File
import java.lang.StringBuilder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.JPanel

/**
 * [DisplayPanel] class - extends [JPanel] to add custom appearance.
 *
 * @version 14.11.2022 (dd.mm.yyyy)
 * @author Duc Long Hoang
 */
class DisplayPanel(w: Int, h: Int) : JPanel() {
    var k: Int = Constants.K
    var radius: Int = Constants.RADIUS
    var xCoordinates: IntArray = Constants.X_COORDINATES
    var yCoordinates: IntArray = Constants.Y_COORDINATES

    private lateinit var polygon: Shape
    private lateinit var allGeneratedPoints: MutableList<List<Point>>

    init {
        preferredSize = Dimension(w, h)
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        val g2d: Graphics2D = g as Graphics2D
        background = Color.white

        val numberOfPoints = xCoordinates.size
        polygon = Polygon(xCoordinates, yCoordinates, numberOfPoints)
        val samplingMethods = arrayOf(RandomSampling(), PoissonDiskSampling(), VoronoiSampling())
        allGeneratedPoints = ArrayList()

        // generate points by different methods
        (samplingMethods[1] as PoissonDiskSampling).radius = this.radius
        samplingMethods.forEach { allGeneratedPoints.add(it.generatePoints(polygon, k)) }

        // display all generated points
        val offsetX = polygon.bounds.width * 1.2

        g2d.drawSampling(polygon, allGeneratedPoints[0], Color.red, samplingMethods[0].javaClass.simpleName)

        g2d.translate(offsetX.toInt(), 0)
        g2d.drawSampling(polygon, allGeneratedPoints[1], Color.blue, samplingMethods[1].javaClass.simpleName)

        g2d.translate(offsetX.toInt(), 0)
        g2d.drawSampling(polygon, allGeneratedPoints[2], Color.green.darker(), samplingMethods[2].javaClass.simpleName)
    }

    /**
     * Extension function for [Graphics2D]. Method the sampling method aka the [polygon],
     * the [generatedPoints] in color [c] with a big [string] on top.
     */
    private fun Graphics2D.drawSampling(polygon: Shape, generatedPoints: List<Point>, c: Color, string: String) {
        color = Color.black
        stroke = BasicStroke(1f)
        draw(polygon)
        draw(polygon.bounds)
        font = Font(Font.SANS_SERIF, Font.BOLD, 13)
        val str = string.plus(" - points generated: ${generatedPoints.size}")
        drawString(str, polygon.bounds.minX.toFloat(), (polygon.bounds.minY * 0.75).toFloat())

        color = c
        stroke = BasicStroke(5f)
        generatedPoints.forEach { drawPoint(it) }
        stroke = BasicStroke(1f)
        generatedPoints.forEach { drawCircleAroundPoint(it) }
    }

    /**
     * Extension function for [Graphics2D]. Method draws point [p].
     */
    private fun Graphics2D.drawPoint(p: Point) {
        drawLine(p.x, p.y, p.x, p.y)
    }

    /**
     * Extension function for [Graphics2D]. Method draws a circle of radius [radius]
     * around point [p].
     */
    private fun Graphics2D.drawCircleAroundPoint(p: Point) {
        val x = p.x - radius
        val y = p.y - radius
        drawOval(x, y, 2 * radius, 2 * radius)
    }

    /**
     * Method exports [polygon] and its [allGeneratedPoints] in all Sampling types.
     */
    fun exportAllToSVG() {
        val dir = File(Constants.OUTPUT_DIR)
        if (!dir.exists()) {
            if (dir.mkdirs()) println("Created image output dir: img")
        }
        exportToSVG(polygon, allGeneratedPoints[0], "red", "random")
        exportToSVG(polygon, allGeneratedPoints[1], "blue", "poisson")
        exportToSVG(polygon, allGeneratedPoints[2], "green", "voronoi")
    }

    /**
     * Method exports [shape] and the generated [points] to an SVG file format
     * while colouring the points in [color]. The filename has the format
     * "[Constants.OUTPUT_DIR]/[basename]_[radius]_[k]_[points.size].svg"
     */
    private fun exportToSVG(shape: Shape, points: List<Point>, color: String, basename: String) {
        val box = shape.bounds
        val polygon = shape as Polygon
        val lines: MutableList<String> = ArrayList()

        // add SVG header
        lines.add("<svg width=\"${box.width}\" height=\"${box.height}\" " +
                "viewBox=\"${box.x} ${box.y} ${box.width} ${box.height}\" xmlns=\"http://www.w3.org/2000/svg\">")

        // add clipping for circles outside rectangle
        lines.add("<defs>")
        lines.add("<clipPath id=\"clip\">")
        lines.add("<rect x=\"${polygon.bounds.x}\" y=\"${polygon.bounds.y}\" " +
                "width=\"${polygon.bounds.width}\" height=\"${polygon.bounds.height}\" />")
        lines.add("</clipPath>")
        lines.add("</defs>")

        // add polygon
        val sb = StringBuilder("<polygon points=\"")
        polygon.xpoints.zip(polygon.ypoints).forEach {
            sb.append("${it.first},${it.second} ")
        }
        sb.append("\" stroke=\"black\" fill=\"none\" />")
        lines.add(sb.toString())

        // add bounding box
        lines.add("<rect x=\"${polygon.bounds.x}\" y=\"${polygon.bounds.y}\" " +
                "width=\"${polygon.bounds.width}\" height=\"${polygon.bounds.height}\" " +
                "stroke=\"black\" fill=\"none\" />")

        // add points and circles around
        points.forEach { p: Point ->
            lines.add("<circle cx=\"${p.x}\" cy=\"${p.y}\" r=\"1\" stroke=\"$color\" fill=\"$color\" />")
            lines.add("<circle cx=\"${p.x}\" cy=\"${p.y}\" r=\"$radius\" " +
                    "stroke=\"$color\" fill=\"none\" clip-path=\"url(#clip)\" />")
        }

        // add SVG footer
        lines.add("</svg>")

        val filename = "img/${basename}_${radius}_${k}_${points.size}.svg"
        val file = File(filename)
        val overwrite: String = if (file.exists()) "- overwritten" else ""

        val path: Path = Files.write(Paths.get(filename), lines)
        println("Exported SVG file: $path $overwrite")
    }
}