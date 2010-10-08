package hudson.plugins.nsiq;

import hudson.FilePath;
import hudson.XmlFile;
import hudson.model.Build;
import hudson.model.Hudson;
import hudson.plugins.nsiq.model.FileType;
import hudson.util.ColorPalette;
import hudson.util.ShiftedCategoryAxis;
import hudson.util.XStream2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.mutable.MutableInt;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

/**
 * 
 * @author iceize at NHN Corporation
 * @version $Rev$, $Date$
 */
public final class NSiqUtil {
	private NSiqUtil() {
		super();
	}

	public static FilePath getLocFile(FilePath filePath) {
		return new FilePath(filePath, Constant.LOC_FILE);
	}

	public static FilePath getComplexityFile(FilePath filePath) {
		return new FilePath(filePath, Constant.COMPLEXITY_FILE);
	}

	public static XmlFile getDataFile(Build<?, ?> build) {
		File dir = build == null ? new File(System.getProperty("java.io.tmpdir")) : build.getRootDir();
		return new XmlFile(new XStream2(), new File(dir, Constant.RESULT_FILENAME));
	}

	/**
	 * complexity에 대한 문자열 포매팅 결과를 리턴한다.
	 */
	public static String getFormattedComplexity(double complexity) {
		return String.format("%.1f", complexity);
	}

	/**
	 * 라인 차트를 리턴한다.
	 */
	@SuppressWarnings("deprecation")
	public static JFreeChart createLineChart(CategoryDataset dataset, String yAxis, int lower, int upper) {
		final JFreeChart chart = ChartFactory.createLineChart(null, null, yAxis, dataset, PlotOrientation.VERTICAL, false, true, false);

		chart.setBackgroundPaint(Color.white);

		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setOutlinePaint(null);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.black);

		CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
		plot.setDomainAxis(domainAxis);
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
		domainAxis.setLowerMargin(0.0);
		domainAxis.setUpperMargin(0.0);
		domainAxis.setCategoryMargin(0.0);

		if (lower == Integer.MAX_VALUE) {
			lower = 0;
		}

		if (upper == Integer.MIN_VALUE) {
			upper = 0;
		}

		double lowerBound = lower * 0.95;
		double upperBound = upper * 1.05;

		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setLowerBound(lowerBound);
		rangeAxis.setUpperBound(upperBound);

		final LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
		renderer.setStroke(new BasicStroke(2.0f));
		ColorPalette.apply(renderer);
		renderer.setSeriesPaint(1, ColorPalette.RED);
		renderer.setSeriesPaint(0, ColorPalette.BLUE);
		plot.setInsets(new RectangleInsets(5.0, 0, 0, 5.0));

		return chart;
	}

	/**
	 * Stack 차트를 리턴한다.
	 */
	public static JFreeChart createStackChart(CategoryDataset dataset, String yAxis) {
		final JFreeChart chart = ChartFactory.createStackedAreaChart(null, null, yAxis, dataset, PlotOrientation.VERTICAL, false, true, false);
		chart.setBackgroundPaint(Color.white);

		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setOutlinePaint(null);
		plot.setForegroundAlpha(0.8f);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.black);

		CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
		plot.setDomainAxis(domainAxis);

		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
		domainAxis.setLowerMargin(0.0);
		domainAxis.setUpperMargin(0.0);
		domainAxis.setCategoryMargin(0.0);

		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setLowerBound(0);

		final StackedAreaRenderer renderer = (StackedAreaRenderer) plot.getRenderer();
		plot.setRenderer(renderer);
		renderer.setSeriesPaint(1, ColorPalette.RED);
		renderer.setSeriesPaint(0, ColorPalette.BLUE);

		plot.setInsets(new RectangleInsets(0, 0, 0, 5.0));

		return chart;
	}

	public static String baseURL;

	public static String getHudsonBaseURL() {
		if (baseURL == null) {
			baseURL = Hudson.getInstance().getRootUrlFromRequest();
		}
		return baseURL;
	}

	public static JFreeChart createDistrubutionChart(CategoryDataset dataset, String yAxis) {
		final JFreeChart chart = ChartFactory.createStackedBarChart(null, null, null, dataset, PlotOrientation.VERTICAL, true, true, false);
		chart.setBackgroundPaint(Color.white);
		chart.getLegend().setPosition(RectangleEdge.RIGHT);
		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setOutlinePaint(null);
		plot.setForegroundAlpha(0.8f);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.black);
		CategoryAxis domainAxis = new CategoryAxis(null);
		plot.setDomainAxis(domainAxis);
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);
		domainAxis.setLowerMargin(0.1);
		domainAxis.setUpperMargin(0.1);
		domainAxis.setTickLabelsVisible(true);
		domainAxis.setCategoryMargin(0.1);
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setLowerBound(0);
		final StackedBarRenderer renderer = (StackedBarRenderer) plot.getRenderer();
		// renderer.setSeriesVisibleInLegend(0, false);
		plot.setInsets(new RectangleInsets(0, 0, 0, 5.0));
		renderer.setBaseItemLabelsVisible(true);
		renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("0")));
		plot.setRenderer(renderer);
		return chart;
	}

	public static Map<FileType, Integer> convertLangDistMap(Map<FileType, MutableInt> locPerType) {
		Map<FileType, Integer> langDistMap = new HashMap<FileType, Integer>();
		for (Entry<FileType, MutableInt> entry : locPerType.entrySet()) {
			langDistMap.put(entry.getKey(), entry.getValue().toInteger());
		}
		return langDistMap;
	}
}
