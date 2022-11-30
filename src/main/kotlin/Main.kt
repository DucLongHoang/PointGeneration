import java.awt.*
import java.util.StringJoiner
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.SwingUtilities
import javax.swing.UIManager

/**
 * [Main] class - main class of program.
 *
 * @version 14.11.2022 (dd.mm.yyyy)
 * @author Duc Long Hoang
 */
class Main {
    companion object {
        private lateinit var panel: DisplayPanel
        private lateinit var radiusTF: JTextField
        private lateinit var pointTF: JTextField
        private lateinit var polygonXTF: JTextField
        private lateinit var polygonYTF: JTextField

        /**
         * [main] method. Entry point of program.
         */
        @JvmStatic
        fun main(args: Array<String>) {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
            SwingUtilities.invokeLater {
                val frame = JFrame("Generating K-points in a non-convex polygon")
                frame.makeGui()
                frame.pack()
                frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
                frame.setLocationRelativeTo(null)
                frame.isVisible = true
            }
        }

        /**
         * Extension function for [JFrame]. creates GUI of app.
         */
        private fun JFrame.makeGui() {
            layout = BorderLayout()
            panel = DisplayPanel(1350, 450)
            add(panel)
            add(makeConfigPanel(), BorderLayout.SOUTH)
        }

        /**
         * Method created a [JPanel] with user config options.
         */
        private fun makeConfigPanel() : JPanel {
            val panel = JPanel()
            val generateBtn = makeGenerateBtn()
            val exportBtn = makeExportBtn()
            val radiusLabel = JLabel("Circle radius")
            val pointLabel = JLabel("Points to generate")
            val polygonXLabel = JLabel("Polygon X-coordinates")
            val polygonYLabel = JLabel("Polygon Y-coordinates")
            radiusTF = JTextField(Constants.RADIUS.toString())
            pointTF = JTextField(Constants.K.toString())

            val xCoords = with(StringJoiner(",")) {
                Constants.X_COORDINATES.forEach { add(it.toString()) }
                this.toString()
            }
            val yCoords = with(StringJoiner(",")) {
                Constants.Y_COORDINATES.forEach { add(it.toString()) }
                this.toString()
            }
            polygonXTF = JTextField(xCoords)
            polygonYTF = JTextField(yCoords)

            generateBtn.preferredSize = Dimension(150, 50)
            generateBtn.setSamePreferredSize(listOf(exportBtn))
            polygonXLabel.preferredSize = Dimension(200, 20)
            polygonXLabel.setSamePreferredSize(listOf(polygonYLabel, polygonXTF, polygonYTF))
            pointLabel.preferredSize = Dimension(100, 20)
            pointLabel.setSamePreferredSize(listOf(radiusLabel, pointTF, radiusTF))

            val l = GridBagLayout()
            val c = GridBagConstraints()
            c.insets = Insets(5, 5, 5, 5)
            panel.layout = l

            panel.addLabelAndTextField(polygonXLabel, polygonXTF, l ,c, 0, 0)
            panel.addLabelAndTextField(polygonYLabel, polygonYTF, l ,c, 2, 0)
            panel.addLabelAndTextField(pointLabel, pointTF, l ,c, 4, 0)
            panel.addLabelAndTextField(radiusLabel, radiusTF, l ,c, 6, 0)
            panel.addComponent(generateBtn, l, c, 8, 0, 3, 2)
            panel.addComponent(exportBtn, l, c, 11, 0, 3, 2)

            return panel
        }

        /**
         * Method creates a [JButton] to generate points inside polygon.
         */
        private fun makeGenerateBtn(): JButton {
            val btn = JButton("Generate")
            btn.addActionListener {
                if (pointTF.text.isNotEmpty()) panel.k = pointTF.text.toInt()
                if (radiusTF.text.isNotEmpty()) panel.radius = radiusTF.text.toInt()
                if (polygonXTF.text.isNotEmpty() && polygonXTF.text.length == polygonYTF.text.length) {
                    panel.xCoordinates = polygonXTF.text.split(",".toRegex()).map { it.toInt() }.toIntArray()
                    panel.yCoordinates = polygonYTF.text.split(",".toRegex()).map { it.toInt() }.toIntArray()
                }
                else println("ERROR: Invalid polygon! Ignoring coordinate input.")
                panel.revalidate()
                panel.repaint()
                println("Points generated")
            }
            return btn
        }

        /**
         * Method creates a [JButton] to export generated points.
         */
        private fun makeExportBtn(): JButton {
            val btn = JButton("Export to SVG")
            btn.addActionListener { panel.exportAllToSVG() }
            btn.requestFocusInWindow()
            return btn
        }

        /**
         * Extension function for [JPanel]. Method adds [component] to [this]
         * at [x], [y] with [width] and [height] using [GridBagLayout]
         * and [GridBagConstraints].
         */
        private fun JPanel.addComponent(component: JComponent, l: GridBagLayout,
                                        c: GridBagConstraints, x: Int, y: Int,
                                        width: Int, height: Int) {
            c.gridx = x; c.gridy = y; c.gridwidth = width; c.gridheight = height
            l.setConstraints(component, c)
            this.add(component)
        }

        /**
         * Extension function for [JPanel]. Method adds [label] and [tf] to [this]
         * at [x], [y] using [GridBagLayout] and [GridBagConstraints].
         */
        private fun JPanel.addLabelAndTextField(label: JLabel, tf: JTextField, l: GridBagLayout,
                                                c: GridBagConstraints, x:Int, y: Int) {
            this.addComponent(label, l, c, x, y, 1, 1)
            this.addComponent(tf, l, c, x, y + 1, 1, 1)
        }

        /**
         * Extension function for [JComponent]. Method sets dimensions of [jComponents]
         * to [this] using [widthMul] and [heightMul].
         */
        private fun JComponent.setSamePreferredSize(jComponents: List<JComponent>,
                                                    widthMul: Double = 1.0, heightMul: Double = 1.0) {
            val ps = this.preferredSize
            val newWidth = (ps.width + widthMul).toInt()
            val newHeight = (ps.height + heightMul).toInt()
            jComponents.forEach { it.preferredSize = Dimension(newWidth, newHeight) }
        }
    }
}