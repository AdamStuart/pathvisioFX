package diagrams.pViz.tables;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import diagrams.pViz.app.App;
import diagrams.pViz.app.Document;
import diagrams.pViz.gpml.GPML;
import gui.DraggableTableRow;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import model.bio.Gene;
import model.bio.GeneListRecord;
import table.referenceList.TableController;
import util.FileUtil;
import util.StringUtil;


public class GeneListController  extends TableController  {
	
	public static final DataFormat COLUMN_MIME_TYPE = new DataFormat("application/x-java-serialized-column");
	public static final DataFormat GENE_MIME_TYPE = new DataFormat("application/x-java-serialized-gene");

	@FXML private TableColumn<Gene, String> geneTypeColumn;
	@FXML private TableColumn<Gene, String> geneNameColumn;
	@FXML private TableColumn<Gene, String> locationColumn;
	@FXML private TableColumn<Gene, String> geneIdColumn;
	@FXML private TableColumn<Gene, String> urlColumn;
	@FXML private TableColumn<Gene, String> databaseColumn;
	@FXML private TableColumn<Gene, String> dbidColumn;
	@FXML private TableColumn<Gene, String> dataColumn;
	@FXML private TableColumn<Gene, String> termsColumn;
	
	@FXML private Label select;
	@FXML private Label size;

	@FXML private MenuBar menubar;
	@FXML private MenuItem info;
	@FXML private MenuItem save;
	@FXML private MenuItem saveAs;
	@FXML private MenuItem addColumn;
	@FXML private MenuItem chart;
	@FXML private MenuItem groupSelectedGenes;
	@FXML private MenuItem browsePathways;
	//------------------------------------------------------
	private String state;		// undo information
	public void setState(String s) 	{ 	state = s; }
	public String getState()	 	{ 	return state; 	}
	//------------------------------------------------------

	private GeneListRecord geneListRecord;		// the model
	private List<Gene> allGenes = new ArrayList<Gene>();
//	GeneListTable geneTable;
	
	protected void createTableRecord()
	{

		tableRecord = geneListRecord = new GeneListRecord("GeneList", allColumns);	
	}
	public static String tooltip = "Text or expressions can be entered here";
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		super.initialize(location, resources);
		geneNameColumn.setUserData("T");	allColumns.add(geneNameColumn);
		termsColumn.setUserData("T");		allColumns.add(termsColumn);
		geneTypeColumn.setUserData("T");		allColumns.add(geneTypeColumn);
		
		makeSeparatorColumn();
		dataColumn.setUserData("N");		allColumns.add(dataColumn);
		locationColumn.setUserData("T");	allColumns.add(locationColumn);
		geneIdColumn.setUserData("T");		allColumns.add(geneIdColumn);
		urlColumn.setUserData("U");			allColumns.add(urlColumn);
		databaseColumn.setUserData("D");	allColumns.add(databaseColumn);
		dbidColumn.setUserData("T");		allColumns.add(dbidColumn);

		//-------
//		geneTable = (GeneListTable) theTable;
		theTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//		geneNameColumn.setText("Label");
//		geneNameColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("name"));
//		geneIdColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("ensembl"));
//		urlColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("url"));
//		locationColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("location"));
//		databaseColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("database"));
//		dbidColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("dbid"));
//		dataColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("data"));
//		termsColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("termSummary"));

		theTable.setRowFactory((a) -> {
		       return new DraggableTableRow<Gene>(theTable, GENE_MIME_TYPE, this, geneListRecord);
			});
		
		// bind two labels:  select and size to the number selected and number of rows, e.g.: (32 / 2092)
		select.setAlignment(Pos.CENTER_RIGHT);
		select.setText("0");
		size.setText("0");
		theTable.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>()
		{
			@Override public void onChanged(Change<? extends Integer> change) 
			{ 
				select.setText("" + theTable.getSelectionModel().getSelectedIndices().size()); 
			}
		});
		theTable.getItems().addListener(new ListChangeListener<Integer>()
		{
			@Override public void onChanged(Change<? extends Integer> change) { size.setText("/ " + theTable.getItems().size()); }
		});
		theTable.setOnKeyPressed( new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(final KeyEvent keyEvent) {
				if (keyEvent.getCode().equals(KeyCode.DELETE) || keyEvent.getCode().equals(KeyCode.BACK_SPACE)) {
					List<Integer> ids = theTable.getSelectionModel().getSelectedIndices();
					int sz = ids.size();
					for (int i = sz - 1; i >= 0; i--)
						theTable.getItems().remove(i);
					theTable.getSelectionModel().clearSelection();
				}
			}
		});
		search.setTooltip(new Tooltip(tooltip));
		searchBox.setTooltip(new Tooltip(tooltip));
		
		resetTableColumns();
		
//		theTable.addKeyListener();
	}
	
	@Override public void reorderColumns(int a, int b) 
	{	
//		TableColumn col = (TableColumn) allColumns.remove(a);
//		allColumns.add(b, col);
	}

	protected GeneListRecord readTabularText(File f)
	{
		return Document.readTabularText(f, getSpecies());
	}

	File file = null;
	int verbose = 0;
	@FXML private void save()	
	{ 	
		if (file == null)
		{
			FileChooser chooser = new FileChooser();	
			chooser.setTitle("Save Gene List");
            Window w = theTable.getScene().getWindow();
            if (w instanceof Stage) 
        	{
        		Stage stage = (Stage) w;
    			file = chooser.showSaveDialog(stage);
    			if (file == null) return;
    			String name = file.getName();
//    			name = StringUtil.chopExtension(name) + ".txt";
    			stage.setTitle(name);
        	}
		}
		String buff =  getTextFromTable(theTable);
		 try (FileOutputStream out = new FileOutputStream(file)) 
		 {
		    out.write( buff.getBytes());
			 out.close();
		 } 
		 catch (IOException e) {     e.printStackTrace();  }
	}
	
	private String getTextFromTable(TableView theTable) {
		StringBuilder buff = new StringBuilder();
		buff.append(getTableHeader(theTable.getColumns()));
		for (Gene g : (List<Gene>)theTable.getItems())
		{
			StringBuilder lineBuff = new StringBuilder();
			for (TableColumn<Gene, ?> col : (ObservableList<TableColumn<Gene, ?>>)theTable.getColumns())
			{
				if (!col.isVisible()) continue;
				String txt = g.getValue(col.getText());
				if (txt == null) txt = "";
				lineBuff.append(txt).append(TAB);
			}
			String line = lineBuff.toString();
			line = StringUtil.chopLast(line) + NL;
			buff.append(line);
		}
		return buff.toString();
	}
	static String TAB = "\t";
	static String NL = "\n";
	
	private String getTableHeader(ObservableList<TableColumn<Gene, ?>> columns) {
		StringBuilder buff = new StringBuilder();
		for (TableColumn<Gene, ?> col : columns)
		{
			if (!col.isVisible()) continue;
			buff.append(col.getText()).append(TAB);
		}
		String s = buff.toString();
		s = StringUtil.chopLast(s) + NL;
		return s;
	}
	protected void processFile(File f)
	{
		if (Document.isMappingFile(f))
		{
			doMapping(f);
			return;
		}
		 if (FileUtil.isCDT(f) || FileUtil.isTXT(f))
			 geneListRecord = readTabularText(f);
		 else if (FileUtil.isGPML(f))
			 geneListRecord = GPML.readGeneList(f, getSpecies());

		 if (geneListRecord != null)
		 {
			 if (theTable.getItems().isEmpty())
				 allColumns.clear();
			 allColumns.addAll(geneListRecord.getAllColumns());
			 resetTableColumns();
			 theTable.getItems().addAll(geneListRecord.getGeneList());
		 }
	}
	//------------------------------------------------------
	private void doMapping(File f) {
		long startReadTime = System.currentTimeMillis();
		Map<String, String> map = Document.readMappingFile(f, this);
		if (map.isEmpty()) 		return;
		String key = map.get("\tkey");
		String value = map.get("\tvalue");
		if (key == null || value == null)
			return;
		map.remove("\tkey");
		map.remove("\tvalue");
		TableColumn<Gene, String> oldCol = findColumnByName(theTable.getColumns(), key);
		int index = theTable.getColumns().indexOf(oldCol);
		TableColumn<Gene, String> newCol = new TableColumn<Gene, String>();
//		Callback<CellDataFeatures<Gene, String>, ObservableValue<String>> a;
//		newCol.setCellValueFactory(a);
		newCol.setCellValueFactory(new Callback<CellDataFeatures<Gene, String>, ObservableValue<String>>() {
		     public ObservableValue<String> call(CellDataFeatures<Gene, String> p) {
		         Gene gene = p.getValue();
		         String str = gene.get(value);
//		         if (str != null)
//		        	 System.out.println(str);
		         return new ReadOnlyObjectWrapper(str);
		     }
		  });
		newCol.setText(value);
		newCol.setPrefWidth(oldCol.getWidth());
		newCol.setUserData("T");
//		newCol.setCellValueFactory(new PropertyValueFactory<Gene, String>(value));

		long startMapTime = System.currentTimeMillis();
		for (Gene gene : geneListRecord.getGeneList())
		{
			String source = gene.getValue(key);
			if (StringUtil.isEmpty(source)) continue;
			String target = map.get(source);
			if (!StringUtil.isEmpty(target))
				gene.put(value,  target);
		}
		long endMapTime = System.currentTimeMillis();
		allColumns.add(index + 1, newCol);
		theTable.getColumns().add(index + 1, newCol);
		long endTime = System.currentTimeMillis();
		System.out.println("Times: " + (startMapTime-startReadTime) + ", " + (endMapTime-startMapTime) + ", " + (endTime-endMapTime));
	}
	//------------------------------------------------------

	private GeneListRecord getGeneList() {		return geneListRecord;	}
	@FXML private void newGeneList()	{		App.doNewGeneList(null);	}
	@FXML private void getInfo()		{		System.out.println("getInfo");	}
	@FXML private void saveAs()			{		file = null; 	save();	 }
	@FXML private void invert()		
	{		
		TableViewSelectionModel<Gene> sel = theTable.getSelectionModel();
		int ct = geneListRecord.getRowCount();
		for (int i=0; i<ct ; i++)
		{
			if (sel.isSelected(i))
				sel.clearSelection(i);		// SLOW
			else sel.select(i);
		}
		System.out.println("invert");	
	}
	
	@FXML private void drillDown()		
	{		
		GeneListRecord subset = getSelectedGeneList();
		System.out.println("drillDown");	
		App.doNewGeneList(subset);
	}
	
	//------------------------------------------------------
	private GeneListRecord getSelectedGeneList() {
		
		GeneListRecord subset = new GeneListRecord(geneListRecord);
		List<Gene> sel = FXCollections.observableArrayList();
		List<Gene> genes = (List<Gene>) theTable.getSelectionModel().getSelectedItems();
		sel.addAll(genes);
		subset.setGeneList(sel);
		return subset;
	}


	// TODO used for both new lists and unioning lists
	public void loadTables(GeneListRecord geneList, boolean setAllGenes) {
		geneListRecord = geneList;
//		allColumns = geneListRecord.getAllColumns();
		dumpColumns();
		resetTableColumns();
		if (setAllGenes && true)
			allGenes = geneListRecord.getGeneList();
		theTable.getItems().addAll(allGenes);
		
//		columnTable.getItems().addAll(geneListRecord.getAllColumns());
	}
	@FXML private void selectAll()			{		showAll();	}
	@FXML private void showAllColumns()		{		showAll();	}
	@FXML private void editColumns()		{		showAll();	}
	@FXML private void doOpen()		{		}
	@FXML private void doClose()		{	 	}
	
	public void showAll()
	{
		theTable.getItems().clear();
		theTable.getItems().addAll(allGenes);
		theTable.getColumns().add(geneNameColumn);
		theTable.getColumns().add(geneTypeColumn);
		theTable.getColumns().add(termsColumn);
		theTable.getColumns().add(databaseColumn);
		theTable.getColumns().add(dbidColumn);
		theTable.getColumns().add(urlColumn);
		theTable.getColumns().add(locationColumn);
		theTable.getColumns().add(geneIdColumn);
		
	}

	public static String FIND_PATHWAYS_BASE = "http://webservice.wikipathways.org/findPathwaysByText?";
	@FXML private void doAddColumn() 	 
	{ 
		System.out.println("doAddColumn"); 	
		TableColumn extant =geneListRecord.findByText(allColumns, "All IDs");
		if (extant != null) return;
		
		GeneListRecord rec = getGeneList();
		rec.fillIdlist();
		TableColumn<Gene, String> mapCol = new TableColumn("All IDs");
		mapCol.setPrefWidth(300);
		mapCol.setCellValueFactory(new PropertyValueFactory<Gene, String>("idlist"));
		allColumns.add(mapCol);
		//FORCE REFRESH   TODO
	}
	
	@FXML private void filterSelectedGenes() { System.out.println("filterSelectedGenes"); }
	@FXML private void browsePathways()		{		App.browsePathways(null);	}
	@FXML private void other()	 { 	System.out.println(""); }
	//------------------------------------------------------

	@FXML private void doChart()	
	{
		long start = System.currentTimeMillis();
		GeneListRecord rec = getGeneList();
		if (rec == null || !geneListRecord.hasHeaders()) 
		{
			System.err.println("doChart failed");
			return;
		}
		String[] headers = geneListRecord.getHeader(0).split("\t");
		List<String> hdrList = new ArrayList<String>();
		for (int i=4; i<headers.length; i++)
			hdrList.add(headers[i]);
		VBox histos = rec.buildHypercube(hdrList);
		long complete = System.currentTimeMillis();
		System.out.println("doChart took: " + (complete - start) + " ms");
		if (histos != null)
			App.buildStage("Histogram List", new ScrollPane(histos), 700,400);
	}

	//------------------------------------------------------
	@FXML private void doChartVisible()	
	{
		long start = System.currentTimeMillis();
		GeneListRecord rec = getGeneList();
		if (rec == null) 
		{
			System.err.println("doChart failed");
			return;
		}
		List<String> headerList = new ArrayList<String>();
		List<TableColumn<?,?>> cols = theTable.getColumns();
		for (TableColumn<?,?> col : cols )
		{
			if (col.getProperties().get("Numeric") != null)
				headerList.add(col.getText());
		}
		VBox histos = rec.buildHypercube(headerList);
		long complete = System.currentTimeMillis();
		System.out.println("doChart took: " + (complete - start) + " ms");
		if (histos != null)
			App.buildStage("Histogram List", new ScrollPane(histos), 700,400);
	}
	//------------------------------------------------------
	@FXML public void doSearch()	
	{
		String text = searchBox.getText().trim();
		if (StringUtil.isEmpty(text)) 	
			return;
		theTable.getSelectionModel().clearSelection();
		if (isExpression(text))
		{
			processExpression(text);
			return;
		}
		for (int i =0; i<theTable.getItems().size(); i++)
		{
			Gene gene =  (Gene) theTable.getItems().get(i);
			if (gene.match(text.toUpperCase()))
			{
				System.out.println("hit: " + gene.getName());
				theTable.getSelectionModel().select(i);
			}
		}
	}
	private boolean isExpression(String text) {

		String[] tokens = text.split(" ");
		if (tokens.length != 3)		return false;
		if (!isComparator(tokens[1]))	return false;
		if (!isColumnOrNumber(tokens[0]))	return false;
		if (!isColumnOrNumber(tokens[2]))	return false;
		return true;
	}
	private void processExpression(String text) {
		
		String[] tokens = text.split(" ");
		double valueA = Double.NaN;
		double valueB = Double.NaN;
		if (StringUtil.isNumber(tokens[0]))
			valueA = StringUtil.toDouble(tokens[0]);
		if (StringUtil.isNumber(tokens[2]))
			valueB = StringUtil.toDouble(tokens[2]);
		
		for (int i =0; i<theTable.getItems().size(); i++)
		{
			Gene gene =  (Gene) theTable.getItems().get(i);
			if (gene == null) continue;   //??
			double a;
			if (Double.isNaN(valueA))
			{
				Object col = findColumnContains(tokens[0]);
				a = gene.getValueByName(((TableColumn)col).getText());
			}
			else a = valueA;
			
			double b;
			if (Double.isNaN(valueB))
			{
				Object col = findColumnContains(tokens[2]);
				b = gene.getValueByName(((TableColumn)col).getText());
			}
			else b = valueB;
			
			
			if (parseExpression(a, tokens[1], b))
			{
				System.out.println("expression hit: " + gene.getSymbol());
				theTable.getSelectionModel().select(i);
			}
		}
		System.out.println("selection: " + theTable.getSelectionModel().getSelectedIndices().size());

	
	}
	private boolean isComparator(String str)
	{
		String[] legal = { ">", ">=", "<", "<=", "=", "!=" };
		for (String s : legal)
			if (s.equals(str)) return true;
		return false;
	}
	private boolean isColumnOrNumber(String str)
	{
		if (StringUtil.isNumber(str)) return true;
		Object col = findColumnContains(str);
		return col != null;
	}
	private Double getNumericValue(String str, Gene g)
	{
		if (StringUtil.isNumber(str)) 
			return StringUtil.toDouble(str);
		Object col = findColumnContains(str);
		if (g == null || !(col instanceof TableColumn)) return Double.NaN;
		return g.getValueByName(((TableColumn)col).getText());
	}
	double EPSILON = 0.001;
	
	boolean parseExpression(double a, String comparator, double b)
	{
		if ("=".equals(comparator)) return Math.abs(a - b) < EPSILON;
		if ("!=".equals(comparator)) return Math.abs(a - b) > EPSILON;
		if (">".equals(comparator)) return a > b;
		if (">=".equals(comparator)) return a >= b;
		if ("<".equals(comparator)) return a < b;
		if ("<=".equals(comparator)) return a <= b;
		return false;

	}
	//---------------------------------------------------------------------------
	public void openByReference(String ref) {
		for (Object rec : theTable.getItems())
		{
			if (ref.equals(((Gene)rec).getId()))
				System.out.println("openByReference " + ref);
		}
	}
   //---------------------------------------------------------------------------
	@Override
	public void getInfo(DataFormat fmt, String a, String colname, MouseEvent event) {
		// TODO Auto-generated method stub
		
	}
}
