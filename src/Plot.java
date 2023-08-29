import org.jCharts.axisChart.AxisChart;
import org.jCharts.chartData.AxisChartDataSet;
import org.jCharts.chartData.ChartDataException;
import org.jCharts.chartData.DataSeries;
import org.jCharts.encoders.PNGEncoder;
import org.jCharts.properties.*;
import org.jCharts.types.ChartType;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Plot {

        public static void plot(String title, String xAxisTitle, String yAxisTitle, String[] x_values_labels, double[][] y_values,
                        String outFilePath, int x_size, int y_size, int y_min_val, int y_num_labels){

            DataSeries dataSeries = new DataSeries( x_values_labels, xAxisTitle, yAxisTitle, title );
            String[] legendLabels= { "Bugs" };//Legenda colori

            //Stile disegno
            Paint[] paints= new Paint[]{ Color.blue.darker() };//
            Stroke[] strokes= { LineChartProperties.DEFAULT_LINE_STROKE };
            Shape[] shapes= { PointChartProperties.SHAPE_CIRCLE };
            LineChartProperties lineChartProperties= new LineChartProperties( strokes, shapes );

            AxisChartDataSet axisChartDataSet= null;
            try {
                axisChartDataSet = new AxisChartDataSet( y_values, legendLabels, paints, ChartType.LINE, lineChartProperties );
            } catch (ChartDataException e) {
                throw new RuntimeException(e);
            }
            dataSeries.addIAxisPlotDataSet( axisChartDataSet );

            //Asse y
            AxisProperties axisProperties= new AxisProperties();
            DataAxisProperties dataAxisProperties= (DataAxisProperties) axisProperties.getYAxisProperties();
            try {
                dataAxisProperties.setUserDefinedScale( y_min_val, 1 );//val min asse y, val incrementi
            } catch (PropertyException e) {
                throw new RuntimeException(e);
            }
            dataAxisProperties.setRoundToNearest( 0 );//potenza di 10
            dataAxisProperties.setNumItems( y_num_labels );//numero val asse y, default=5


//          AxisChart axisChart= new AxisChart( dataSeries, new ChartProperties(), axisProperties, new LegendProperties(), x_size, y_size );
            AxisChart axisChart= new AxisChart( dataSeries, new ChartProperties(), axisProperties, null, x_size, y_size );

            // Salva su file
            FileOutputStream fout = null;
            try {
                fout = new FileOutputStream(outFilePath, false);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            try {
                PNGEncoder.encode(axisChart, fout);
            } catch (ChartDataException e) {
                throw new RuntimeException(e);
            } catch (PropertyException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                fout.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
}
