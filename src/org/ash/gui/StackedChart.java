/*
 *-------------------
 * The StackedChart.java is part of ASH Viewer
 *-------------------
 * 
 * ASH Viewer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ASH Viewer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ASH Viewer.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (c) 2009, Alex Kardapolov, All rights reserved.
 *
 */

package org.ash.gui;

import com.sleepycat.je.DatabaseException;
import org.ash.database.ASHDatabase;
import org.ash.util.Options;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.BorderArrangement;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer3;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.CategoryTableXYDataset;
import org.jfree.ui.*;

import java.awt.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The Class StackedXYAreaChart.
 * 
 */
public class StackedChart {

	/** The database. */
	private ASHDatabase database;
	
	/** The dataset. */
	private CategoryTableXYDataset dataset;
	
	/** The chart. */
	private JFreeChart chart;
	
	/** The chart panel. */
	private ChartPanel chartPanel;
	
	/** The date format. */
	private DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
	
	/** The current date. */
	private Date currentDate;	
    
    /** The x axis. */
    private DateAxis xAxis;
	
	/** The threshold max cpu. */
	private ValueMarker thresholdMaxCpu;
	
	/** The max cpu. */
	private double maxCpu;
	
	/** The plot. */
	private XYPlot plot;
	
	/** The current end. */
	private Marker currentEnd;
	
	/** The flag threshold begin time auto selection. */
   	private boolean flagThresholdBeginTimeAutoSelection = false;

    private boolean isMinimalistic = false;
   	
	/**
	 * Instantiates a new stacked xy area chart.
	 * 
	 * @param database0 the database0
	 */
    public StackedChart(ASHDatabase database0) {
       this.database = database0;
    }
    
    /**
     * Creates the chart panel.
     * 
     * @return the chart panel
     * 
     * @throws DatabaseException the database exception
     */
    public ChartPanel createChartPanel() throws DatabaseException {
    	createDataset();
        JFreeChart chart = createChart();
        chartPanel = new ChartPanel(chart);
        chartPanel.setRangeZoomable(false);
        return chartPanel;
    }
    
 	/**
	  * Update xaxis label.
	  * 
	  * @param time the time
	  */
    public void updatexAxisLabel(double time) {
        if (!isMinimalistic) {
            if (!xAxis.getLabel().equalsIgnoreCase(dateFormat.format(time))) {
                xAxis.setLabel(dateFormat.format(time));
            }
        }
    }
    
    /**
     * Sets the threshold max cpu.
     * 
     * @param maxCpu the new threshold max cpu
     */
    public void setThresholdMaxCpu(double maxCpu){  	
    	this.maxCpu = maxCpu;
      }
    
    /**
     * Sets the flag threshold begin time auto selection.
     * 
     * @param flag0 the new flag threshold begin time auto selection
     */
    public void setFlagThresholdBeginTimeAutoSelection(boolean flag0){
    	this.flagThresholdBeginTimeAutoSelection = flag0;
    }
    
    /**
     * Checks if is flag threshold begin time auto selection.
     * 
     * @return true, if is flag threshold begin time auto selection
     */
    public boolean isFlagThresholdBeginTimeAutoSelection(){
    	return this.flagThresholdBeginTimeAutoSelection;
    }
    
    /**
     * Sets the threshold begin time auto selection.
     * 
     * @param beginTime the begin time
     * @param range the range
     */
    public synchronized void setThresholdBeginTimeAutoSelection(double beginTime, int range){
    	plot.removeDomainMarker(currentEnd);
        currentEnd = new ValueMarker(beginTime);
        currentEnd.setPaint(Color.red);
        currentEnd.setLabel(range+" min");
        currentEnd.setStroke(new BasicStroke(1.0f));
        currentEnd.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        currentEnd.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        plot.addDomainMarker(currentEnd);
    }
    
    /**
     * Removes the threshold begin time auto selection.
     */
    public void removeThresholdBeginTimeAutoSelection(){
        plot.removeDomainMarker(currentEnd);
    }
    
    /**
     * Adds the listener chart panel.
     * 
     * @param l the l
     */
    public void addListenerChartPanel(Object l){  	
    	chartPanel.addListenerReleaseMouse(l);
      }

    /**
     * Removes the listener chart panel.
     * 
     * @param l the l
     */
    public void removeListenerChartPanel(Object l){  	
    	chartPanel.removeListenerReleaseMouse(l);
      }
    
    /**
     * Sets the selection chart.
     * 
     * @param flag the new selection chart
     */
    public void setSelectionChart(boolean flag){
    	chartPanel.setDomainZoomable(flag);
    }
    
    /**
     * Checks if is mouse dragged.
     * 
     * @return true, if is mouse dragged
     */
    public boolean isMouseDragged(){
    	return chartPanel.isMouseDragged();
    }

    /**
     *
     */
    public void setMinimalistic(boolean flag){
        this.isMinimalistic = flag;
        if (isMinimalistic){
            xAxis.setLabel("");
            chart.setTitle("");
            plot.getRangeAxis().setVisible(false);

        } else {
            xAxis.setVisible(true);
            chart.setTitle("Top activity");
            plot.getRangeAxis().setVisible(true);
            updatexAxisLabel(new Long(currentDate.getTime()).doubleValue());
        }
    }

    /**
     * Set upper bound of range axis
     * 
     * @param bound
     */
    public void setUpperBoundOfRangeAxis(double bound){
    	if (bound == 0.0){
    		plot.getRangeAxis().setAutoRange(true);
    	}
    	else {
    		plot.getRangeAxis().setAutoRange(false);
    		plot.getRangeAxis().setUpperBound(bound*this.maxCpu);
    	}
    }
    
    /**
     * Creates the chart.
     * 
     * @return the j free chart
     */
    private JFreeChart createChart() {

        xAxis = new DateAxis("time");
        xAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
        
        currentDate = new Date();	
    	updatexAxisLabel(new Long(currentDate.getTime()).doubleValue());

        chart = ChartFactory.createStackedXYAreaChart(
            "Top activity",  				 // chart title
            "X Value",                       // domain axis label
            "Active Sessions",               // range axis label
            dataset,                         // data
            PlotOrientation.VERTICAL,        // the plot orientation
            xAxis,
            false,                           // legend
            true,                            // tooltips
            false                            // urls
        );
        
        plot = (XYPlot) chart.getPlot();
        StackedXYAreaRenderer3 renderer = new StackedXYAreaRenderer3(); 
        renderer.setRoundXCoordinates(true);
        renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator
        		("{0} ({1}, {2})",
        		 new SimpleDateFormat("HH:mm"),
        		 new DecimalFormat("0.0")));
        plot.setRenderer(0, renderer);   
        plot.getRangeAxis().setLowerBound(0.0);
        plot.getRangeAxis().setAutoRange(true);
        
        // add a labelled marker for the cpu_count
        thresholdMaxCpu = new ValueMarker(this.maxCpu);
        thresholdMaxCpu.setLabelOffsetType(LengthAdjustmentType.EXPAND);
        thresholdMaxCpu.setPaint(Color.red);
        thresholdMaxCpu.setStroke(new BasicStroke(1.0f));
        thresholdMaxCpu.setLabel("Maximum CPU");
        thresholdMaxCpu.setLabelFont(new Font("SansSerif", Font.PLAIN, 11));
        thresholdMaxCpu.setLabelPaint(Color.red);
        thresholdMaxCpu.setLabelAnchor(RectangleAnchor.TOP_LEFT);
        thresholdMaxCpu.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
        plot.addRangeMarker(thresholdMaxCpu);
        
        renderer.setSeriesPaint(0, Options.getInstance().getColor(
        				Options.getInstance().getResource("cpuLabel.text")),true);
        renderer.setSeriesPaint(1, Options.getInstance().getColor(
						Options.getInstance().getResource("schedulerLabel.text")), true);
        renderer.setSeriesPaint(2,Options.getInstance().getColor(
        				Options.getInstance().getResource("userIOLabel.text")), true);
        renderer.setSeriesPaint(3, Options.getInstance().getColor(
						Options.getInstance().getResource("systemIOLabel.text")), true);
        renderer.setSeriesPaint(4, Options.getInstance().getColor(
						Options.getInstance().getResource("concurrencyLabel.text")), true);
        renderer.setSeriesPaint(5, Options.getInstance().getColor(
						Options.getInstance().getResource("applicationsLabel.text")), true);
        renderer.setSeriesPaint(6, Options.getInstance().getColor(
						Options.getInstance().getResource("commitLabel.text")), true);
        renderer.setSeriesPaint(7, Options.getInstance().getColor(
						Options.getInstance().getResource("configurationLabel.text")), true);
        renderer.setSeriesPaint(8, Options.getInstance().getColor(
						Options.getInstance().getResource("administrativeLabel.text")), true);
        renderer.setSeriesPaint(9, Options.getInstance().getColor(
						Options.getInstance().getResource("networkLabel.text")), true);
        renderer.setSeriesPaint(11, Options.getInstance().getColor(
						Options.getInstance().getResource("queueningLabel.text")), true);//que
        renderer.setSeriesPaint(10, Options.getInstance().getColor(
        				Options.getInstance().getResource("clusterLabel.text")), true);//cluster
        renderer.setSeriesPaint(12, Options.getInstance().getColor(
						Options.getInstance().getResource("otherLabel.text")), true);
        
        // Set format for x axis
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
        // Add legend to right
        LegendTitle legend = new LegendTitle(chart.getPlot());
        
        BlockContainer wrapper = new BlockContainer(new BorderArrangement());
        wrapper.setFrame(new BlockBorder(1.0, 1.0, 1.0, 1.0));
        
        BlockContainer itemss = legend.getItemContainer();
        itemss.setPadding(2, 10, 5, 2);
        wrapper.add(itemss);
        legend.setWrapper(wrapper);
        
        legend.setPosition(RectangleEdge.RIGHT);
        legend.setHorizontalAlignment(HorizontalAlignment.LEFT);
        
        chart.addSubtitle(legend);
        
        return chart;
    }



    /**
     * Creates the dataset.
     * 
     * @throws DatabaseException the database exception
     */
    private void createDataset() throws DatabaseException {
    	dataset = new CategoryTableXYDataset();
        this.database.loadDataToChartPanelDataSet(dataset);
    }
    
}

