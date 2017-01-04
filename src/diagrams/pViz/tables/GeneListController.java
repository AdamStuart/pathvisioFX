package diagrams.pViz.tables;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import animation.BorderPaneAnimator;
import diagrams.pViz.app.App;
import diagrams.pViz.app.Controller;
import diagrams.pViz.app.Document;
import diagrams.pViz.app.GeneListRecord;
import diagrams.pViz.app.IController;
import diagrams.pViz.gpml.GPML;
import gui.DropUtil;
import icon.FontAwesomeIcons;
import icon.GlyphIcon;
import icon.GlyphsDude;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import model.bio.Gene;
import model.bio.Species;
import util.FileUtil;
import util.StringUtil;


public class GeneListController  implements Initializable, IController  {
	
	public static final DataFormat COLUMN_MIME_TYPE = new DataFormat("application/x-java-serialized-column");
	public static final DataFormat GENE_MIME_TYPE = new DataFormat("application/x-java-serialized-gene");
	@FXML private BorderPane geneListContainer;

	@FXML private TableView<Gene> geneListTable;
	@FXML private TableColumn<Gene, String> geneNameColumn;
	@FXML private TableColumn<Gene, String> locationColumn;
	@FXML private TableColumn<Gene, String> geneIdColumn;
	@FXML private TableColumn<Gene, String> urlColumn;
	@FXML private TableColumn<Gene, String> databaseColumn;
	@FXML private TableColumn<Gene, String> dbidColumn;
	@FXML private TableColumn<Gene, String> dataColumn;
	@FXML private TableColumn<Gene, String> termsColumn;

	private TableColumn<Gene, String> separatorColumn = new TableColumn<Gene, String>();

	private List<TableColumn<Gene,?>> allColumns = new ArrayList<TableColumn<Gene,?>>();

	
	@FXML private TableView<TableColumn> columnTable;
	@FXML private TableColumn<TableColumn, String> typeColumn;
	@FXML private TableColumn<TableColumn, String> colNameColumn;
	@FXML private TableColumn<TableColumn, Integer> widthColumn;

	
	@FXML private ChoiceBox<String> species;
	@FXML private Button search;
	@FXML private TextField searchBox;
	@FXML private Button westSidebar;
	@FXML private MenuBar menubar;
	@FXML private MenuItem info;
	@FXML private MenuItem addColumn;
	@FXML private MenuItem chart;
	@FXML private MenuItem groupSelectedGenes;
	@FXML private MenuItem browsePathways;
	//------------------------------------------------------
	public Species getSpecies() {
		String name = species.getSelectionModel().getSelectedItem();
		return Species.lookup(name);
	}
	
	private GeneListRecord geneListRecord;		// the model

	public static String tooltip = "GENELIST TOOLTIP";
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		allColumns.add(geneNameColumn);
		allColumns.add(termsColumn);
		
		allColumns.add(separatorColumn);  
		separatorColumn.setPrefWidth(0);  
		separatorColumn.setText("---------------");		// TODO horizontal line in TableCell
		
		allColumns.add(dataColumn);
		allColumns.add(locationColumn);
		allColumns.add(geneIdColumn);
		allColumns.add(urlColumn);
		allColumns.add(databaseColumn);
		allColumns.add(dbidColumn);

// inherit table with columnEditing
		Controller.setGraphic(westSidebar, FontAwesomeIcons.ARROW_CIRCLE_O_RIGHT);
		new BorderPaneAnimator(geneListContainer, westSidebar, Side.LEFT, false, 250);
		westSidebar.fire();		// start with columns hidden
		//-------
		typeColumn.setSortable(false);
		colNameColumn.setSortable(false);
		widthColumn.setSortable(false);
		typeColumn.setCellValueFactory(new PropertyValueFactory<TableColumn, String>("type"));
		colNameColumn.setCellValueFactory(new PropertyValueFactory<TableColumn, String>("text"));
		widthColumn.setCellValueFactory(new PropertyValueFactory<TableColumn, Integer>("width"));
		columnTable.setRowFactory((a) -> {
		       return new DraggableTableRow<TableColumn>(columnTable, COLUMN_MIME_TYPE, this);
			    });
		resetColumnTable();
		columnTable.getItems().addListener(resetColumnOrder);
		// inherit table with columnEditing
		
		
		//-------
		geneListTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		geneNameColumn.setText("Label");
		geneNameColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("name"));
		geneIdColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("ensembl"));
		urlColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("url"));
		locationColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("location"));
		databaseColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("database"));
		dbidColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("dbid"));
		dataColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("data"));
		termsColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("termSummary"));

		geneListTable.setRowFactory((a) -> {
		       return new DraggableTableRow<Gene>(geneListTable, GENE_MIME_TYPE, this);
			    });
		
//		geneListTable.getColumns().addListener(columnDragged);		// reset our column side table if the columns change
		//-------
		String[] organisms = { "Any", "Homo sapiens", "Mus musculus", "Rattus norvegicus", "Canis familiarus", "Box taurus", "Pan troglodytes", "Gallus gallus" };
		species.getItems().addAll(organisms);
		species.getSelectionModel().select(1);
		
		search.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.SEARCH, GlyphIcon.DEFAULT_ICON_SIZE));
		search.setText("");
		search.setTooltip(new Tooltip(tooltip));
		searchBox.setTooltip(new Tooltip(tooltip));
		searchBox.setText("");
		searchBox.addEventHandler(KeyEvent.KEY_PRESSED, e -> {  if (e.getCode() == KeyCode.ENTER) doSearch();	} );
		
		DropUtil.makeFileDropPane(geneListTable, e -> { doDrag(e); });
		String url = "";				// TODO
//		findGenes(url);
	}
	
	private TableColumn<Gene, ?> findColumnByName(List<TableColumn<Gene, ?>> cols, String name) {
		for (TableColumn<Gene, ?> genecol : cols)
		{
			String txt = genecol.getText();
			if (txt.equals(name))
				return genecol;
			if (txt.equals("Label") && name.equals("Name"))
				return genecol;
		}
		
		return null;
	}
	
//	List<TableColumn> cols;
	ListChangeListener<TableColumn> resetColumnOrder = new ListChangeListener<TableColumn>()
	{
		@Override public void onChanged(Change<? extends TableColumn> c)	
		{
			if (allColumns == null) return;		// this gets called too early in initialization
//			if (allColumns != null) return;		// this gets called too early in initialization
			System.out.println("resetColumnOrder");
			geneListTable.getColumns().clear();
			
			for (TableColumn col : columnTable.getItems())
			{
				String colName = col.getText();
				if (col == separatorColumn) break;
				TableColumn column = findColumnByName(allColumns, colName);
				if (column != null)
					geneListTable.getColumns().add(column);
			}
			// readjust widths, etc
		}

	};
//	ListChangeListener<TableColumn> columnDragged = new ListChangeListener<TableColumn>()
//	{
//		@Override public void onChanged(Change<? extends TableColumn> c)	
//		{
//			System.out.println("columnDragged " + allColumns.size());
////			if (allColumns.size() < 3393) return;
//			ObservableList<? extends TableColumn> list = c.getList();
////			String[] colNames = getColumnNames(list);		// names of columns show in gene table
//			String[] colNames = new String[list.size()];
//			for (int i=0; i<list.size(); i++)
//				colNames[i] = list.get(i).getText();
//			int idx = 0;
//			for (; idx < colNames.length; idx++)
//			{
//				int targetIdx = findColumnIndex(allColumns, colNames[idx]);
//				if (targetIdx > idx)
//				{
//					TableColumn<Gene,?> tc = allColumns.get(targetIdx);
//					allColumns.remove(tc);
//					allColumns.add(idx, tc);
//				}
//			}
//			if (allColumns.remove(separatorColumn))
//				allColumns.set(idx, separatorColumn);
//		}
//
//		int findColumnIndex(List<TableColumn<Gene,?>> cols, String t)
//		{
//			for (int i=0; i<cols.size(); i++)
//				if ( cols.get(i).getText().equals(t))
//					return i;
//			return -1;
//		}
//
//		private List<String> getColumnNames(ObservableList<? extends TableColumn> list) {
//			List<String> strs = new ArrayList<String>();
//			for (int i=0; i<list.size(); i++)
//				if (list.get(i).isVisible())
//					strs.add(list.get(i).getText());
//			return strs;
//		}
//		
//	};
	
	public void doDrag(DragEvent e)
	{
		Dragboard db = e.getDragboard();
		Set<DataFormat> formats = db.getContentTypes();
		formats.forEach(a -> System.out.println("getContentTypes " + a.toString()));
		 for (File f : db.getFiles())
		 {
			 if (FileUtil.isCDT(f))
			 {
				 geneListRecord = Document.readCDT(f, getSpecies());
				 geneListTable.getItems().addAll(geneListRecord.getGeneList());
				 allColumns.addAll(geneListRecord.getDataColumns());
			 }
			 if (FileUtil.isGPML(f))
			 {
				 geneListRecord = GPML.readGeneList(f, getSpecies());
				 geneListTable.getItems().addAll(geneListRecord.getGeneList());
				 allColumns.addAll(geneListRecord.getDataColumns());
			 }
		 }
		 e.consume();
		 resetColumnTable();
	}

	private void resetColumnTable() {
		System.err.println("resetColumnTable");
		columnTable.getItems().clear();
		for (TableColumn<Gene,?> col : allColumns)
			columnTable.getItems().add(col);	
	}

	//------------------------------------------------------
//	private Controller parentController;
//	public void setParentController(Controller inParent) {		parentController = inParent;	}
//	private GPML gpmlReader;

	List<Gene> allGenes = new ArrayList<Gene>();
	
	// TODO used for both new lists and unioning lists
	public void loadTables(GeneListRecord geneList, boolean setAllGenes) {
		geneListRecord = geneList;
		if (setAllGenes)
			allGenes = geneListRecord.getGeneList();
		geneListTable.getItems().addAll(geneListRecord.getGeneList());
	}
	@FXML private void selectAll()		{		showAll();	}
	@FXML private void showAllColumns()		{		showAll();	}
	@FXML private void editColumns()		{		showAll();	}
	@FXML private void doOpen()		{	
		
	}
	@FXML private void doClose()		{	
		
	}
	public void showAll()
	{
		geneListTable.getItems().clear();
		geneListTable.getItems().addAll(allGenes);
	}
//	public void loadTables(List<Gene> genes) {
//		geneListTable.getItems().addAll(genes);
//	}

	public static String FIND_PATHWAYS_BASE = "http://webservice.wikipathways.org/findPathwaysByText?";
	@FXML private void doAddColumn()	
	{
		System.out.println("doAddColumn");
	}
	@FXML private void filterSelectedGenes()	
	{
		System.out.println("filterSelectedGenes");
	}
	@FXML private void browsePathways()		{		App.browsePathways(null);	}


	@FXML private void other()	
	{
		System.out.println("");
	}
//------------------------------------------------------
	
	@FXML private void doChart()	
	{
		long start = System.currentTimeMillis();
		GeneListRecord rec = getGeneList();
		if (rec == null) 
		{
			System.err.println("doChart failed");
			return;
		}
		String[] headers = geneListRecord.getHeader1().split("\t");
		List<String> hdrList = new ArrayList<String>();
		for (int i=4; i<headers.length; i++)
			hdrList.add(headers[i]);
		VBox histos = rec.buildHypercube(hdrList);
		long complete = System.currentTimeMillis();
		System.out.println("doChart took: " + (complete - start) + " ms");
		if (histos != null)
			App.buildStage("Histogram List", new ScrollPane(histos), 700,400);
	}

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
		for (TableColumn col : geneListTable.getColumns())
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

	private GeneListRecord getGeneList() {		return geneListRecord;	}
	@FXML private void newGeneList()	{		App.doNewGeneList(null);	}
	@FXML private void getInfo()		{		System.out.println("getInfo");	}
	@FXML private void drillDown()		
	{		
		GeneListRecord subset = getSelectedGeneList();
		System.out.println("drillDown");	
		App.doNewGeneList(subset);
	}
	
	private GeneListRecord getSelectedGeneList() {
		
		GeneListRecord subset = new GeneListRecord(geneListRecord);
		List<Gene> sel = FXCollections.observableArrayList();
		for (Gene gene : geneListTable.getSelectionModel().getSelectedItems())
			sel.add(gene);
		subset.setGeneList(sel);
		return subset;
	}
	//------------------------------------------------------
		
	@FXML private void doSearch()	
	{
		String text = searchBox.getText().trim();
		geneListTable.getSelectionModel().clearSelection();
		if (StringUtil.isEmpty(text)) 	
			return;
		for (int i =0; i<geneListTable.getItems().size(); i++)
		{
			Gene gene =  geneListTable.getItems().get(i);
			if (gene.match(text.toUpperCase()))
			{
				System.out.println("hit: " + gene.getName());
				geneListTable.getSelectionModel().select(gene);
			}
		}
	}
	   //---------------------------------------------------------------------------
	public void openByReference(String ref) {
		for (Gene rec : geneListTable.getItems())
			if (ref.equals(rec.getId()))
			{
				System.out.println("openByReference " + ref);
			}
	}
   //---------------------------------------------------------------------------
	@Override
	public void getInfo(DataFormat fmt, String a, String colname, MouseEvent event) {
		// TODO Auto-generated method stub
		
	}
}
