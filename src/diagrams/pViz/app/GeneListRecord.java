package diagrams.pViz.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import diagrams.pViz.model.DimensionRecord;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import model.bio.Gene;
import model.stat.Range;

public class GeneListRecord {
	public GeneListRecord(String n)
	{
		name.set(n);
		geneList = new ArrayList<Gene>();
		dataColumns = new ArrayList<TableColumn<Gene, Double>>();
	}
	public GeneListRecord(GeneListRecord parent)
	{
		this("Subset of " + parent.getName());
		type.set(parent.getType());
		species.set(parent.getSpecies());
		header1.set(parent.getHeader1());
		header2.set(parent.getHeader2());
		history.set(parent.getHistory());
	}
	
	
	StringProperty id = new SimpleStringProperty();
	StringProperty type = new SimpleStringProperty();
	StringProperty name = new SimpleStringProperty();
	BooleanProperty visible = new SimpleBooleanProperty();
	BooleanProperty editable = new SimpleBooleanProperty();
	DoubleProperty score = new SimpleDoubleProperty(0);
	DoubleProperty size = new SimpleDoubleProperty(0);
	StringProperty header1 = new SimpleStringProperty();
	StringProperty header2 = new SimpleStringProperty();
	StringProperty comments = new SimpleStringProperty();
	StringProperty history = new SimpleStringProperty();
	StringProperty species = new SimpleStringProperty();
	StringProperty tissue = new SimpleStringProperty();

	public StringProperty  idProperty()  { return id;}
	public String getId()  { return id.get();}
	public void setId(String s)  { id.set(s);}

	public StringProperty  typeProperty()  { return type;}
	public String getType()  { return  type.get();		}
	public void setType(String s)  { type.set(s);}

	public StringProperty  nameProperty()  { return name;}
	public String getName()  { return name.get();}
	public void setName(String s)  { name.set(s);}

	public BooleanProperty  visibleProperty()  { return visible;}
	public Boolean getVisible()  { return visible.get();}
	public void setVisible(Boolean s)  { visible.set(s);}
	
	public BooleanProperty  editableProperty()  { return editable;}
	public Boolean getEditable()  { return editable.get();}
	public void setEditable(Boolean s)  { editable.set(s);}
	
	public DoubleProperty  scoreProperty()  { return score;}
	public Double getScore()  { return score.get();}
	public void setScore(Double s)  { score.set(s);}

	public StringProperty  header1Property()  { return header1;}
	public String getHeader1()  { return header1.get();}
	public void setHeader1(String s)  { header1.set(s);}

	public StringProperty  header2Property()  { return header2;}
	public String getHeader2()  { return header2.get();}
	public void setHeader2(String s)  { header2.set(s);}

	public StringProperty  historyProperty()  { return history;}
	public String getHistory()  { return history.get();}
	public void setHistory(String s)  { history.set(s);}

	public StringProperty  speciesProperty()  { return species;}
	public String getSpecies()  { return species.get();}
	public void setSpecies(String s)  { species.set(s);}

	private List<Gene> geneList ;		// observableList created in Doc.readCDT or GPML.readGeneList
	public void setGeneList(List<Gene> g) {  geneList = g; 	}
	public List<Gene>  getGeneList() {	return geneList; }

	private List<TableColumn<Gene, Double>> dataColumns;
	public List<TableColumn<Gene, Double>> getDataColumns() { return dataColumns;	}
	public void addDataColumn(TableColumn<Gene, Double> col) { 	dataColumns.add(col);	}

	int getValueIndex(String colName)
	{
		String header = header1.get();
		String[] fields = header.split("\t");
		for (int i=0; i<fields.length; i++)
			if (fields[i].equals(colName))
				return i;
		return -1;
	}
	Map<String, DimensionRecord> dimensions = new HashMap<String, DimensionRecord>();
	public VBox buildHypercube(List<String> headers)
	{
		VBox vbox = new VBox(12);
		try
		{
			int nCols = headers.size();
			for (int col = 0; col < nCols; col++)
			{
				String title = headers.get(col);
				int index = getValueIndex(title);
				if (index < 0) continue;
				List<Double> vals = new ArrayList<Double>();
				for (Gene g : getGeneList())
					vals.add(new Double(g.getValue(index)));
				DimensionRecord rec = new DimensionRecord(title, vals);
				dimensions.put(title, rec);
				rec.build1DChart();
	//				vbox.getChildren().add(rec.getChart());
			}
			for (int col = 0; col < nCols; col += 2)
			{
				String xDim = headers.get(col);
				String yDim = headers.get(col+1);
				DimensionRecord xRec = dimensions.get(xDim);
				DimensionRecord yRec = dimensions.get(yDim);
				if (xRec != null && yRec != null)
				{
					LineChart<Number, Number> x1D = xRec.getChart();
					LineChart<Number, Number> y1D = yRec.getChart();
					ScatterChart<Number, Number> xy2D = buildScatterChart(xRec, yRec);
					HBox conglom = new HBox(xy2D, new VBox(x1D, y1D));
					vbox.getChildren().add(conglom);
				}
	//				break;  //  when debugging, quit after first 2D chart
			}
		}
		catch (Exception ex) 	{ ex.printStackTrace();  return null;	}
		return vbox;
	}

	private ScatterChart<Number, Number> buildScatterChart(DimensionRecord xRec, DimensionRecord yRec) {
		final NumberAxis xAxis = new NumberAxis();
		Range xRange = xRec.getRange();
		xAxis.setLowerBound(xRange.min);
		xAxis.setUpperBound(xRange.max);
		xAxis.setLabel(xRec.getTitle());
		final NumberAxis yAxis = new NumberAxis();
		Range yRange = yRec.getRange();
		yAxis.setLowerBound(yRange.min);
		yAxis.setUpperBound(yRange.max);
		yAxis.setLabel(yRec.getTitle());

		ScatterChart<Number, Number>	scatter = new ScatterChart<Number, Number>(xAxis, yAxis);
		scatter.setTitle(xRec.getTitle() + " x " + yRec.getTitle());
		XYChart.Series<Number, Number> dataSeries = new XYChart.Series<Number, Number>();
		scatter.getStyleClass().add("custom-chart");
		dataSeries.setName("Genes");
		int sz = Math.min(xRec.getNValues(), yRec.getNValues());
		for (int i=0; i< sz; i++)
		{
			double x = xRec.getValue(i);
			double y = yRec.getValue(i);
			if (Double.isNaN(x) || Double.isNaN(y)) continue;
			XYChart.Data<Number, Number> data = new XYChart.Data<Number, Number>(x, y);
//				Region plotpoint = new Region();
			Rectangle r = new Rectangle(2,2);
			r.setFill(i<2000 ? Color.FIREBRICK : Color.YELLOW);
//		        plotpoint.setShape(r);
	        data.setNode(r);
	        
			dataSeries.getData().add(data);
		}
//			Shape circle = new Circle(1);
//			circle.setFill(Color.RED);
		dataSeries.setNode(new Rectangle(1,1));
		scatter.getData().addAll(dataSeries);
		return scatter;
	}
	public void setColumnList() {
		String header = header1.get();
		String[] fields = header.split("\t");
		for (int i=4; i<fields.length; i++)
		{
			String fld = fields[i];
			final int idx = i;
			TableColumn<Gene, Double> column = new TableColumn<Gene, Double>(fld);
			column.getProperties().put("Numeric", "TRUE");
			column.setCellValueFactory(new Callback<CellDataFeatures<Gene, Double>, ObservableValue<Double>>() {
			     public ObservableValue<Double> call(CellDataFeatures<Gene, Double> p) {
			         Gene gene = p.getValue();
			         double d = gene.getValueByIndex(idx);
			         return new ReadOnlyObjectWrapper(String.format("%4.2f", d));
			     }
			  });
			addDataColumn(column);
		}
	}

}
