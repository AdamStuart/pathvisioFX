package diagrams.pViz.tables;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import animation.BorderPaneAnimator;
import diagrams.pViz.app.App;
import diagrams.pViz.app.Controller;
import diagrams.pViz.app.Document;
import diagrams.pViz.gpml.GPML;
import diagrams.pViz.model.edges.Edge;
import diagrams.pViz.model.edges.Interaction;
import gui.DraggableTableRow;
import icon.FontAwesomeIcons;
import icon.GlyphsDude;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import model.IController;
import model.bio.Gene;
import model.bio.GeneSetRecord;
import util.FileUtil;
import util.StringUtil;


public class GeneListController  extends TableController  {
	
//	public static final DataFormat COLUMN_MIME_TYPE = new DataFormat("application/x-java-serialized-column");
//	public static final DataFormat GENE_MIME_TYPE = new DataFormat("application/x-java-serialized-gene");
	@FXML private Button draggable;
	@FXML private TableColumn<Gene, String> geneTypeColumn;
	@FXML private TableColumn<Gene, String> geneNameColumn;
	@FXML private TableColumn<Gene, String> graphidColumn;
	@FXML private TableColumn<Gene, String> geneIdColumn;
	@FXML private TableColumn<Gene, String> databaseColumn;
	@FXML private TableColumn<Gene, String> dbidColumn;
	@FXML private TableColumn<Gene, String> dataColumn;
	@FXML private TableColumn<Gene, String> ensemblColumn;
	@FXML private TableColumn<Gene, String> urlColumn;
	@FXML private TableColumn<Gene, String> termsColumn;
	@FXML private TableColumn<Gene, String> locationColumn;
	@FXML private TableColumn<Gene, String> logger;
	@FXML private TableColumn<Gene, String> pvalue;
	@FXML private TableColumn<Gene, String> fdr;
	
	protected List<TableColumn<Edge, ?>> edgeColumns = new ArrayList<TableColumn<Edge, ?>>();

	@FXML protected TableView<Edge> edgeTable;
	@FXML private TableColumn<Edge, String> source;
	@FXML private TableColumn<Edge, String> sourceid;
	@FXML private TableColumn<Edge, String> interaction;
	@FXML private TableColumn<Edge, String> target;
	@FXML private TableColumn<Edge, String> targetid;
	@FXML private TableColumn<Edge, String> edgeid;
	@FXML private TableColumn<Edge, String> database;
	@FXML private TableColumn<Edge, String> dbid;
	@FXML private TableColumn<Edge, String> graphid;

	
	@FXML private Button bottomSideBarButton;
	@FXML private Label select;
	@FXML private Label size;

	@FXML private Label eselect;
	@FXML private Label esize;

	@FXML private MenuBar menubar;
	@FXML private MenuItem showAsList;
	@FXML private MenuItem showAsTable;
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
	private boolean windowstate;		// true if its in list-state
	public void setWindowState(boolean s) 	{ 	windowstate = s; }
	public boolean getWindowState()	 	{ 	return windowstate; 	}
	//------------------------------------------------------

	private GeneSetRecord geneListRecord;		// the model
	private List<Gene> allGenes = new ArrayList<Gene>();
	private List<Edge> allInteractions = new ArrayList<Edge>();
//	GeneListTable geneTable;
	
	protected void createTableRecord()
	{
		tableRecord = geneListRecord = new GeneSetRecord("GeneList", allColumns);	
	}
	public static String tooltip = "Text or expressions can be entered here";
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		super.initialize(location, resources);
		IController.setGraphic(bottomSideBarButton, FontAwesomeIcons.ARROW_CIRCLE_DOWN);
		new BorderPaneAnimator(container, bottomSideBarButton, Side.BOTTOM, false, 150);
		
		geneTypeColumn.setUserData("T");	allColumns.add(geneTypeColumn);
		geneNameColumn.setUserData("T");	allColumns.add(geneNameColumn);
		graphidColumn.setUserData("T");		allColumns.add(graphidColumn);
		geneIdColumn.setUserData("T");		allColumns.add(geneIdColumn);
		databaseColumn.setUserData("D");	allColumns.add(databaseColumn);
		dbidColumn.setUserData("T");		allColumns.add(dbidColumn);
		dataColumn.setUserData("N");		allColumns.add(dataColumn);
		ensemblColumn.setUserData("T");		allColumns.add(ensemblColumn);
		urlColumn.setUserData("U");			allColumns.add(urlColumn);
		termsColumn.setUserData("T");		allColumns.add(termsColumn);
		locationColumn.setUserData("T");	allColumns.add(locationColumn);
		
		makeSeparatorColumn();

		geneTypeColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("type"));
		graphidColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("graphid"));
		urlColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("url"));
		typeColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("type"));
		geneNameColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("name"));
		databaseColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("database"));
		dbidColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("dbid"));
		termsColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("goFunction"));
		locationColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("chromosome"));

		logger.setCellValueFactory(new PropertyValueFactory<Gene, String>("logger"));
		pvalue.setCellValueFactory(new PropertyValueFactory<Gene, String>("pvalue"));
		fdr.setCellValueFactory(new PropertyValueFactory<Gene, String>("fdr"));

		source.setCellValueFactory(new PropertyValueFactory<Edge, String>("source"));
		target.setCellValueFactory(new PropertyValueFactory<Edge, String>("target"));
		sourceid.setCellValueFactory(new PropertyValueFactory<Edge, String>("sourceid"));
		targetid.setCellValueFactory(new PropertyValueFactory<Edge, String>("targetid"));
		interaction.setCellValueFactory(new PropertyValueFactory<Edge, String>("interaction"));
		database.setCellValueFactory(new PropertyValueFactory<Edge, String>("database"));
		dbid.setCellValueFactory(new PropertyValueFactory<Edge, String>("dbid"));
		//-------
		theTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		theTable.setRowFactory((a) -> {
		       return new DraggableTableRow<Gene>(theTable, Controller.GENE_MIME_TYPE, this, geneListRecord);
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

		edgeid.setUserData("T");		edgeColumns.add(edgeid);
		source.setUserData("T");		edgeColumns.add(source);
		sourceid.setUserData("T");		edgeColumns.add(sourceid);
		interaction.setUserData("T");	edgeColumns.add(interaction);
		target.setUserData("T");		edgeColumns.add(target);
		targetid.setUserData("T");		edgeColumns.add(targetid);
		database.setUserData("T");		edgeColumns.add(database);
		dbid.setUserData("T");			edgeColumns.add(dbid);
//		graphid.setUserData("T");		edgeColumns.add(graphid);
		edgeid.setCellValueFactory(new PropertyValueFactory<Edge, String>("edgeid"));

	//		graphid.setCellValueFactory(new PropertyValueFactory<Edge, String>("graphid"));
		edgeTable.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>()
		{
			@Override public void onChanged(Change<? extends Integer> change) 
			{ 
				eselect.setText("" + edgeTable.getSelectionModel().getSelectedIndices().size() + "/ " + edgeTable.getItems().size());  
			}
		});
		
	}

	// TODO used for both new lists and unioning lists
	public void loadTables(GeneSetRecord geneList, Collection<Interaction> edgeList, boolean setAllGenes) {
		geneListRecord = geneList;
//		allColumns = geneListRecord.getAllColumns();
		dumpColumns();
		resetTableColumns();
		if (setAllGenes && true)
			allGenes = geneListRecord.getGeneSet();
		theTable.getItems().addAll(allGenes);
		locationColumn.setPrefWidth(200);
		geneTypeColumn.setPrefWidth(28);
		geneNameColumn.setPrefWidth(100);
		dataColumn.setMaxWidth(100);

		if (edgeList != null) 
			edgeTable.getItems().addAll(edgeList);

		
		draggable.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.HAND_ALT_UP, "12"));
		draggable.setText("");
	    draggable.setOnDragDetected(e -> {
	                Dragboard db = draggable.startDragAndDrop(TransferMode.MOVE);
	                db.setDragView(draggable.snapshot(null, null));

	                // The DragView wont be displayed unless we set the content of the dragboard as well. 
	                // Here you probably want to do more meaningful stuff than adding an empty String to the content.
	                ClipboardContent content = new ClipboardContent();
	                content.put(Controller.GENE_MIME_TYPE, "");  // TODO add payload
	                db.setContent(content);

	                e.consume();
	            });
//	    draggable.setOnMouseClicked(new EventHandler<MouseEvent>() {
//            @Override
//            public void handle(MouseEvent mouseEvent) {
////                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
////                    if (mouseEvent.getClickCount() == 2) {
////                        label.setText("Double Click Fire... " + i);
////                        i = i + 1;
////                    }
////                }
//            }
//        });
//		theTable.addKeyListener();
	}
	@Override public void reorderColumns(int a, int b) 
	{	
//		TableColumn col = (TableColumn) allColumns.remove(a);
//		allColumns.add(b, col);
	}
	
	
	
	
	@FXML private void showTranscripts()		
	{	 
		System.out.println("showTranscripts");	
		showCols(urlColumn, locationColumn);
	}
	
	boolean columnIn(TableColumn<?, ?> col, TableColumn<?, ?>[] cols)
	{
		for (TableColumn<?, ?> c : cols)
			if (c == col) return true;
		return false;
	}
	@FXML private void showCols(TableColumn<Gene, String>... cols)		
	{	
		theTable.getColumns().clear();
		theTable.getColumns().addAll(cols);
		columnTable.getItems().clear();
		columnTable.getItems().addAll(cols);
//		columnTable.getItems().addAll(separatorColumn);
		for (Object o : allColumns)
		{
			TableColumn<?, ?> c = (TableColumn<?, ?>) o;
			if (!columnIn(c, cols))
				columnTable.getItems().add(c);
		}
	}
	
	
	@FXML private void showCategories()			
	{	 System.out.println("showCategories");	
		theTable.getColumns().clear();
		theTable.getColumns().addAll(geneNameColumn, geneTypeColumn, databaseColumn, dbidColumn);
		columnTable.getItems().clear();
		columnTable.getItems().addAll(geneNameColumn, geneTypeColumn, databaseColumn, dbidColumn);
	}
	@FXML private void showDifferences()		
	{	 System.out.println("showDifferences");	
		theTable.getColumns().clear();
		theTable.getColumns().addAll(dataColumn);
		columnTable.getItems().clear();
		columnTable.getItems().addAll(dataColumn);
	
	}

	public void showAll()
	{
		theTable.getItems().clear();
		theTable.getColumns().clear();
		theTable.getItems().addAll(allGenes);
		theTable.getColumns().addAll(geneNameColumn, geneTypeColumn, termsColumn, databaseColumn, dbidColumn);
		theTable.getColumns().addAll(urlColumn, locationColumn, geneIdColumn, ensemblColumn);		
		
		columnTable.getItems().addAll(geneNameColumn, geneTypeColumn, termsColumn, databaseColumn, dbidColumn, urlColumn, locationColumn, geneIdColumn, ensemblColumn);		
	}
	private void setWindowWidthToList(boolean b) {
		Stage win = (Stage) theTable.getScene().getWindow();
		win.setMaxWidth(b ? 100 : 6000);
		win.setMinWidth(100);
		if (!b)	
		{
			win.setWidth(700);
			win.setMinWidth(700);
			win.setMinWidth(100);
		}
	}
	@FXML private  void showAsTable()
	{
		resetTableColumns();
		setWindowWidthToList(false);
//		setButtonsToWindowState(false);
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
		if (FileUtil.isCSV(f))
			open(f);
		else if (FileUtil.isCDT(f) || FileUtil.isTXT(f))
			 geneListRecord = readTabularText(f);
		 else if (FileUtil.isGPML(f))
			 geneListRecord = GPML.readGeneList(f, getSpecies());

		 if (geneListRecord != null)
		 {
			 if (theTable.getItems().isEmpty())
				 allColumns.clear();
			 allColumns.addAll(geneListRecord.getAllColumns());
			 resetTableColumns();
			 theTable.getItems().addAll(geneListRecord.getGeneSet());
		 }
	}
	protected GeneSetRecord readTabularText(File f)
	{
		return Document.readTabularText(f, getSpecies());
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
		for (Gene gene : geneListRecord.getGeneSet())
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

	private GeneSetRecord getGeneList() {		return geneListRecord;	}
	@FXML private void newGeneList()	{		App.doNewGeneList(null, null);	}
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
		GeneSetRecord subset = getSelectedGeneList();
		subset.setWindowState(geneListRecord.getWindowState());
		App.doNewGeneList(subset, null, true);
	}
	
	//------------------------------------------------------
	private GeneSetRecord getSelectedGeneList() {
		
		GeneSetRecord subset = new GeneSetRecord(geneListRecord);
		List<Gene> sel = FXCollections.observableArrayList();
		List<Gene> genes = (List<Gene>) theTable.getSelectionModel().getSelectedItems();
		sel.addAll(genes);
		subset.setGeneSet(sel);
		return subset;
	}

//
//	// TODO used for both new lists and unioning lists
//	public void loadTables(GeneListRecord geneList, List<Edge> edgeList, boolean setAllGenes) {
//		geneListRecord = geneList;
////		allColumns = geneListRecord.getAllColumns();
//		dumpColumns();
//		resetTableColumns();
//		if (setAllGenes && true)
//			allGenes = geneListRecord.getGeneList();
//		theTable.getItems().addAll(allGenes);
//		if (edgeList != null) 
//			edgeTable.getItems().addAll(edgeList);
//		
////		columnTable.getItems().addAll(geneListRecord.getAllColumns());
//	}
	@FXML private void selectAll()			{		showAll();	}
	@FXML private void showAllColumns()		{		showAll();	}
	@FXML private void editColumns()		{		showAll();	}
	@FXML private void doOpen()		
	{	
		FileChooser chooser = new FileChooser();	
		chooser.setTitle("Open Drawing");
		file = chooser.showOpenDialog(App.getInstance().getStage());
		if (file != null)				// dialog wasn't canceled
			open(file);			
	
		
	}
	
	private void open(File f)
	{
		List<String> lines = FileUtil.readFileIntoStringList(f.getAbsolutePath(), 30000);
//		StringUtil.removeEmptyLines(lines);
		GeneSetRecord record = new GeneSetRecord(f);

//		 if (theTable.getItems().isEmpty())
//			 allColumns.clear();
//		 allColumns.addAll(record.getAllColumns());
//		 allColumns.add(geneNameColumn);
		 resetTableColumns();
		 List<Gene> genes = record.getGeneSet();
		 theTable.getItems().addAll(genes);
	}
	@FXML private void doClose()		{	 	}
	
//	public void showAll()
//	{
//		theTable.getItems().clear();
//		theTable.getItems().addAll(allGenes);
//		List<TableColumn<Gene,?>> cols = theTable.getColumns();
//		cols.add(geneNameColumn);
//		cols.add(geneTypeColumn);
//		cols.add(termsColumn);
//		cols.add(databaseColumn);
//		cols.add(dbidColumn);
//		cols.add(urlColumn);
//		cols.add(locationColumn);
//		cols.add(geneIdColumn);
//		
//	}

	public static String FIND_PATHWAYS_BASE = "http://webservice.wikipathways.org/findPathwaysByText?";
	@FXML private void doAddColumn() 	 
	{ 
		System.out.println("doAddColumn"); 	
		TableColumn extant =geneListRecord.findByText(allColumns, "All IDs");
		if (extant != null) return;
		
		GeneSetRecord rec = getGeneList();
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
		GeneSetRecord rec = getGeneList();
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
		GeneSetRecord rec = getGeneList();
		if (rec == null) 
		{
			System.err.println("doChart failed");
			return;
		}
		List<String> headerList = new ArrayList<String>();
		List<TableColumn<Gene,?>> cols = theTable.getColumns();
		for (TableColumn<Gene,?> col : cols )
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
			if (ref.equals(((Gene)rec).getGraphId()))
				System.out.println("openByReference " + ref);
		}
	}
   //---------------------------------------------------------------------------
	@Override
	public void getInfo(DataFormat fmt, String a, String colname, MouseEvent event) {
		// TODO Auto-generated method stub
		
	}
	public void doShowAsList() {
		showAsList();
		
	}
	@FXML private  void showAsList()
	{
		resetTableColumns();
		setWindowWidthToList(true);
		setButtonsToWindowState(false);
	}
	private void setButtonsToWindowState(boolean b) {
		westSidebar.setVisible(!b);
		
	}
}
