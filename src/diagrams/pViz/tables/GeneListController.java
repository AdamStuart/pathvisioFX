package diagrams.pViz.tables;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import diagrams.pViz.app.App;
import diagrams.pViz.app.Document;
import diagrams.pViz.gpml.GPML;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import model.bio.Gene;
import model.bio.GeneListRecord;
import util.FileUtil;
import util.StringUtil;


public class GeneListController  extends TableController  {
	
	public static final DataFormat COLUMN_MIME_TYPE = new DataFormat("application/x-java-serialized-column");
	public static final DataFormat GENE_MIME_TYPE = new DataFormat("application/x-java-serialized-gene");

	@FXML private TableColumn<Gene, String> geneNameColumn;
	@FXML private TableColumn<Gene, String> locationColumn;
	@FXML private TableColumn<Gene, String> geneIdColumn;
	@FXML private TableColumn<Gene, String> urlColumn;
	@FXML private TableColumn<Gene, String> databaseColumn;
	@FXML private TableColumn<Gene, String> dbidColumn;
	@FXML private TableColumn<Gene, String> dataColumn;
	@FXML private TableColumn<Gene, String> termsColumn;
	
	@FXML private MenuBar menubar;
	@FXML private MenuItem info;
	@FXML private MenuItem addColumn;
	@FXML private MenuItem chart;
	@FXML private MenuItem groupSelectedGenes;
	@FXML private MenuItem browsePathways;
	//------------------------------------------------------
	private String state;

	public void setState(String s)
	{
		state = s;
	}
	public String getState()
	{
		return state;
	}

	private GeneListRecord geneListRecord;		// the model
	private List<Gene> allGenes = new ArrayList<Gene>();
//	GeneListTable geneTable;
	
	protected void createTableRecord()
	{
		tableRecord = geneListRecord = new GeneListRecord("GeneList");	
	}
	public static String tooltip = "GENELIST TOOLTIP";
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		super.initialize(location, resources);
		geneNameColumn.setUserData("T");	allColumns.add(geneNameColumn);
		termsColumn.setUserData("T");		allColumns.add(termsColumn);
		
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
		geneNameColumn.setText("Label");
		geneNameColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("name"));
		geneIdColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("ensembl"));
		urlColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("url"));
		locationColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("location"));
		databaseColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("database"));
		dbidColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("dbid"));
		dataColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("data"));
		termsColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("termSummary"));

		theTable.setRowFactory((a) -> {
		       return new DraggableTableRow<Gene>(theTable, GENE_MIME_TYPE, this, geneListRecord);
			    });
		
		search.setTooltip(new Tooltip(tooltip));
		searchBox.setTooltip(new Tooltip(tooltip));
		
		List<String> viscols = new ArrayList<String>();
		viscols.addAll(Arrays.asList(geneNameColumn.getText(), termsColumn.getText()));
		setVisColumns(viscols);
		columnTable.getItems().addAll(allColumns);
		resetTableColumns();
//		geneListTable.getColumns().addListener(columnDragged);		// reset our column side table if the columns change
		//-------
	// TODO Species selector
	}
	
	@Override public void reorderColumns(int a, int b) 
	{	
		TableColumn col = (TableColumn) allColumns.remove(a);
		allColumns.add(b, col);
	}

	protected void processFile(File f)
	{
		 if (FileUtil.isCDT(f))
			 geneListRecord = Document.readCDT(f, getSpecies());
		 else if (FileUtil.isTXT(f))
			 geneListRecord = Document.readCDT(f, getSpecies());
		 else if (FileUtil.isGPML(f))
			 geneListRecord = GPML.readGeneList(f, getSpecies());

		 if (geneListRecord != null)
		 {
			 theTable.getItems().addAll(geneListRecord.getGeneList());
			 allColumns.addAll(geneListRecord.getAllColumns());
			 resetTableColumns();
		 }
	}

	//------------------------------------------------------

	private GeneListRecord getGeneList() {		return geneListRecord;	}
	@FXML private void newGeneList()	{		App.doNewGeneList(null);	}
	@FXML private void getInfo()		{		System.out.println("getInfo");	}
	@FXML private void invert()		
	{		
		TableViewSelectionModel<Gene> sel = theTable.getSelectionModel();
		for (int i=0; i< geneListRecord.getRowCount(); i++)
		{
			if (sel.isSelected(i))
				sel.clearSelection(i);
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
		if (setAllGenes)
			allGenes = geneListRecord.getGeneList();
		theTable.getItems().addAll(geneListRecord.getGeneList());
//		allColumns = geneListRecord.getAllColumns();
		resetTableColumns();
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
		if (rec == null) 
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
		theTable.getSelectionModel().clearSelection();
		if (StringUtil.isEmpty(text)) 	
			return;
		for (int i =0; i<theTable.getItems().size(); i++)
		{
			Gene gene =  (Gene) theTable.getItems().get(i);
			if (gene.match(text.toUpperCase()))
			{
				System.out.println("hit: " + gene.getName());
				theTable.getSelectionModel().select(gene);
			}
		}
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
