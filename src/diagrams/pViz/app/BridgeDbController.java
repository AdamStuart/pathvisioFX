package diagrams.pViz.app;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import chart.usMap.ColorUtil;
import diagrams.pViz.tables.DraggableTableRow;
import gui.Backgrounds;
import gui.DropUtil;
import icon.FontAwesomeIcons;
import icon.GlyphIcon;
import icon.GlyphsDude;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import model.bio.DataSourceRecord;
import model.bio.PathwayRecord;
import model.bio.Species;
import model.dao.ResultsRecord;
import services.bridgedb.BridgeDbIdMapper;
import services.bridgedb.MappingSource;
import table.binder.tablecellHelpers.NumberColConverter;
import util.FileUtil;
import util.StringUtil;

public class BridgeDbController implements Initializable
{
	@FXML ChoiceBox<Species> organism;
	@FXML private Button search;
	@FXML private Button bridgeDB;
	@FXML private Button attributes;
	@FXML private Button sources;
	@FXML private Button targets;
	@FXML private CheckBox allRows;
	@FXML private CheckBox allColumns;
//	@FXML private TextArea inputText; 
	@FXML private TableView<ResultsRecord> resultsTable; 
//	@FXML private TableView<Pathway> pathwayTable; 
//	@FXML private TableView<Filter> filterTable; 

//	@FXML ChoiceBox<String> system;
//	@FXML TableView<DataSourceRecord> sourceTable;
	@FXML ListView<String> targetList;
//	@FXML TableColumn<DataSourceRecord, String> nameCol;
//	@FXML TableColumn<DataSourceRecord, String> systemCol;
//	@FXML TableColumn<DataSourceRecord, String> urlCol;
//	@FXML TableColumn<DataSourceRecord, String> usageCol;
//	@FXML TableColumn<DataSourceRecord, String> exampleCol;
//	@FXML TableColumn<DataSourceRecord, String> entityCol;
//	@FXML TableColumn<DataSourceRecord, String> speciesCol;
////	@FXML TableColumn<DataSourceRecord, String> entityTypeCol;
//	@FXML TableColumn<DataSourceRecord, String> uriCol;
//	@FXML TableColumn<DataSourceRecord, String> regexCol;
//	@FXML TableColumn<DataSourceRecord, String> officialNameCol;
//	@FXML TableColumn<DataSourceRecord, String> targetsCol;
//	@FXML TableColumn<DataSourceRecord, String> gravityStrCol;

	@FXML private TableView<PathwayRecord> pathwayTable;
	@FXML private Button searchPathways;
	@FXML private TextField searchPathwaysBox;
	@FXML private TableColumn<PathwayRecord, String> idColumn;
	@FXML private TableColumn<PathwayRecord, String> nameColumn;
	@FXML private TableColumn<PathwayRecord, String> revisionColumn;
	@FXML private TableColumn<PathwayRecord, String> urlColumn;
	@FXML private TableColumn<PathwayRecord, String> speciesColumn;

//	@FXML TableView<IDRecord> idTable;
//	@FXML TableColumn<IDRecord, String> idColumn;
//	@FXML TableColumn<IDRecord, String> sysColumn;
//	@FXML TableColumn<IDRecord, String> sysNameColumn;
	//----------------------------------------------------------------------------------
	Controller parentController;
	public void setParentController(Controller c)	{	parentController = c;	}
	public Controller getParentController( )		{	return parentController;	}
	//----------------------------------------------------------------------------------
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		organism.getItems().clear();	
		organism.getItems().addAll(Species.values());		//getSpeciesList()
		organism.getSelectionModel().select(Species.Human );
		organism.getSelectionModel().selectedIndexProperty().addListener((obs, old, val) -> 
		{ 
			Species species = organism.getItems().get(val.intValue());
			System.out.println("setting species to " + species);
			setSpecies(species); 
		}); 
		readTargetSources(Species.Human);
//		sourceTable.getSelectionModel().selectedIndexProperty().addListener((obs, old, val) -> { sourceChangeHandler(val); }); 
//		nameCol.setCellValueFactory( cell -> cell.getValue().nameProperty());
//		systemCol.setCellValueFactory( cell -> cell.getValue().systemProperty());
//		urlCol.setCellValueFactory( cell -> cell.getValue().siteProperty());
//		usageCol.setCellValueFactory( cell -> cell.getValue().usageProperty());
//		exampleCol.setCellValueFactory( cell -> cell.getValue().exampleProperty());
//		entityCol.setCellValueFactory( cell -> cell.getValue().entityProperty());
//		speciesCol.setCellValueFactory( cell -> cell.getValue().exclusiveSpeciesProperty());
//		uriCol.setCellValueFactory( cell -> cell.getValue().uriProperty());
//		regexCol.setCellValueFactory( cell -> cell.getValue().patternProperty());
//		officialNameCol.setCellValueFactory( cell -> cell.getValue().fullnameProperty());

//		nameCol.setPrefWidth(200);
//		speciesCol.setPrefWidth(100);
//		targetsCol.setPrefWidth(400);		// has a long string of target systems
		targetList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//		targetList.getSelectionModel().selectedItemProperty().
//			addListener(ev->{ targetSelectionChanged(ev);});
		targetList.setOnMouseClicked(ev -> 
		{ 
			if (ev.getClickCount() == 2) 
			{	
				String targ = targetList.getSelectionModel().getSelectedItem();
				addResultsTableColumn(targ);
				doSearch();
			}
		});
//		attributeCol.setCellValueFactory( cell -> cell.getValue().originalProperty());
//		valueCol.setCellValueFactory( cell -> cell.getValue().result0Property());
		DropUtil.makeFileDropPane(resultsTable, ev -> fileDroppedInResultsTable(ev));
		
//		idColumn.setCellValueFactory( cell -> cell.getValue().nameProperty());
//		sysColumn.setCellValueFactory( cell -> cell.getValue().systemProperty());
//		search.setDisable(true);
//		idTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		setupResultsTable();
		setupPathwaysTable();
	}
	
	public static String HUMAN_PATHWAYS = "http://webservice.wikipathways.org/listPathways?organism=Homo%20sapiens";
	public static String tooltip = "Combine terms with AND and OR. Combining terms with a space is equal to using OR ('p53 OR apoptosis' gives the same result as 'p53 apoptosis').\n" + 
			"Group terms with parentheses, e.g. '(apoptosis OR mapk) AND p53'\n";
			

	private void setupPathwaysTable() {
		
		searchPathways.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.SEARCH, GlyphIcon.DEFAULT_ICON_SIZE));
		searchPathways.setText("");
		searchPathways.setTooltip(new Tooltip(tooltip));
		searchPathwaysBox.setTooltip(new Tooltip(tooltip));
		searchPathwaysBox.setText("ATF2");
		searchPathwaysBox.addEventHandler(KeyEvent.KEY_PRESSED, e -> {  if (e.getCode() == KeyCode.ENTER) doSearch();	} );
		
		if (pathwayTable != null)
			setupPathwayTable();
		
		searchPathways(HUMAN_PATHWAYS);
		
	
}
	
	private static final DataFormat PATHWAY_MIME_TYPE = new DataFormat("application/x-java-serialized-pathway");
	static TableRow<PathwayRecord> thisRow = null;

	private void setupPathwayTable()
	{
//		System.out.println("setupPathwayTable");
//		TableColumn[] allCols = { idColumn, urlColumn, nameColumn, speciesColumn, revisionColumn };

		idColumn.setCellValueFactory(new PropertyValueFactory<PathwayRecord, String>("id"));
		urlColumn.setCellValueFactory(new PropertyValueFactory<PathwayRecord, String>("url"));
		nameColumn.setCellValueFactory(new PropertyValueFactory<PathwayRecord, String>("name"));
		speciesColumn.setCellValueFactory(new PropertyValueFactory<PathwayRecord, String>("species"));
		revisionColumn.setCellValueFactory(new PropertyValueFactory<PathwayRecord, String>("revision"));

		idColumn.setMaxWidth(80);
		idColumn.setMinWidth(80);
		revisionColumn.setVisible(false);
		speciesColumn.setVisible(false);
		urlColumn.setVisible(false);
		
		
		pathwayTable.setRowFactory((a) -> {
	       return new DraggableTableRow<PathwayRecord>(pathwayTable, PATHWAY_MIME_TYPE, parentController);
		    });
//
//	        row.setOnDragDetected(event -> {
//	            if (! row.isEmpty()) {
//	                Integer index = row.getIndex();
//	                Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
//	                db.setDragView(row.snapshot(null, null));
//	                ClipboardContent cc = new ClipboardContent();
//	                cc.put(PATHWAY_MIME_TYPE, index);
//	                db.setContent(cc);
//	                event.consume();
//	                thisRow = row;
//	            }
//	        });
//
//	        row.setOnDragEntered(event -> {
//	            Dragboard db = event.getDragboard();
//	            if (db.hasContent(PATHWAY_MIME_TYPE)) {
//	                if (row.getIndex() != ((Integer)db.getContent(PATHWAY_MIME_TYPE)).intValue()) {
//	                    event.acceptTransferModes(TransferMode.MOVE);
//	                    event.consume();
//	                    thisRow = row;
////	                  if (thisRow != null) 
////	                 	   thisRow.setOpacity(0.3);
//	                }
//	            }
//	        });
//
//	        row.setOnDragExited(event -> {
//	            if (event.getGestureSource() != thisRow &&
//	                    event.getDragboard().hasString()) {
////	               if (thisRow != null) 
////	            	   thisRow.setOpacity(1);
//	               thisRow = null;
//	            }
//	        });
//
//	        row.setOnDragOver(event -> {
//	            Dragboard db = event.getDragboard();
//	            if (db.hasContent(PATHWAY_MIME_TYPE)) {
//	                if (row.getIndex() != ((Integer)db.getContent(PATHWAY_MIME_TYPE)).intValue()) {
//	                    event.acceptTransferModes(TransferMode.MOVE);
//	                    event.consume();
//	                }
//	            }
//	        });
//
//	        row.setOnMouseClicked(event -> {
//	        	if (event.getClickCount() == 2)
//	            {
//	                int idx = row.getIndex();
//	        		getInfo(idx);
//	              event.consume();
//	            }
//	        });
//
//	        row.setOnDragDropped(event -> {
//	            Dragboard db = event.getDragboard();
//	            if (db.hasContent(PATHWAY_MIME_TYPE)) {
//	                int draggedIndex = (Integer) db.getContent(PATHWAY_MIME_TYPE);
//	                PathwayRecord draggedNode = pathwayTable.getItems().remove(draggedIndex);
//
//	                int  dropIndex = (row.isEmpty()) ? pathwayTable.getItems().size() : row.getIndex();
//	                pathwayTable.getItems().add(dropIndex, draggedNode);
//
//	                event.setDropCompleted(true);
//	                pathwayTable.getSelectionModel().select(dropIndex);
//	                event.consume();
////	                if (thisRow != null) 
////	             	   thisRow.setOpacity(1);
//	                thisRow = null;
//	            }
//	        });
//
//	        return row ;

		
	}
	// **-------------------------------------------------------------------------------
//	@FXML public  void browseGenes(String result)		
//	{
//		assert(parentController != null);
//		parentController.browseGenes(result);
//		
//	}

	private void searchPathways(String url) {
		url = url.replace(" ", "%20");
		String result = StringUtil.callURL(url, false);
		result = result.replaceAll("\t\t", "\n");
//		System.out.println(result);
	try {
		Document doc = FileUtil.convertStringToDocument(result);
		if (doc != null)
		{
			pathwayTable.getItems().clear();
			NodeList nodes = doc.getElementsByTagName("ns1:findPathwaysByTextResponse");
			int sz = nodes.getLength();
			for (int i=0; i<sz; i++)
			{
				Node node = nodes.item(i);
				NodeList paths = doc.getElementsByTagName("ns1:result");
				int nPaths = paths.getLength();
				for (int j=0; j<nPaths; j++)
				{
			
					PathwayRecord rec = new PathwayRecord(paths.item(j));
					pathwayTable.getItems().add(rec);
				}
			}
		}		
		}
	catch (Exception e) {}
	}

	   //---------------------------------------------------------------------------
	public static String FIND_PATHWAYS_BASE = "http://webservice.wikipathways.org/findPathwaysByText?";
	@FXML private void doSearchPathways()	
	{
		String text = searchPathwaysBox.getText().trim();
		if (StringUtil.isEmpty(text)) 			return;
		
		String queryText = "", speciesComponent = "";
		
		if (StringUtil.hasText(text))
			queryText = "query=" + text;
		Species selection = organism.getSelectionModel().getSelectedItem();
		if (Species.Unspecified != selection)
			speciesComponent = "species=" + selection.latin1token()  + "&";
				
		String query = FIND_PATHWAYS_BASE;
		query += speciesComponent + queryText;
		System.out.println(query);
		searchPathways(query);
	}
	
	public void openByReference(String ref) {
		for (PathwayRecord rec : pathwayTable.getItems())
			if (ref.equals(rec.getId()))
			{
			}
	}

	private void setupResultsTable()
	{
		resultsTable.getColumns().clear();
		TableColumn<ResultsRecord, String> col = new TableColumn<ResultsRecord, String>();
		col.setText("Identifier");
		col.setCellValueFactory( cell -> cell.getValue().originalProperty());
		
		TableColumn<ResultsRecord, String> source = new TableColumn<ResultsRecord, String>();
		source.setText("Source");
		source.setCellValueFactory( cell -> cell.getValue().sourceProperty());
		resultsTable.getColumns().addAll(col, source);
		
		
		
		TableColumn<ResultsRecord, Double> val0 = new TableColumn<ResultsRecord, Double>();
		val0.setText("Value 0");
		Callback<TableColumn<ResultsRecord, Double>, TableCell<ResultsRecord, Double>> factory 
			= TextFieldTableCell.<ResultsRecord, Double> forTableColumn(new NumberColConverter());
		TableCell<ResultsRecord, Double> dcell = factory.call(val0);
		val0.setCellFactory(factory);
//		val0.setCellValueFactory( cell -> cell.getValue().value0Property());

		
		source.setCellFactory(makeCallback());
	            
		boolean verbose = false;
		dcell.addEventFilter(KeyEvent.KEY_TYPED, event ->
		{	
			if (verbose ) System.out.println("KEY_TYPED: " + event.getCharacter());
			if (!Character.isDigit(event.getCharacter().charAt(0))) event.consume();	});
		
		dcell.addEventFilter(KeyEvent.KEY_PRESSED, event ->
		{	
			if (verbose) System.out.println("KEY_PRESSED: " + event.getCharacter());
			if (!Character.isDigit(event.getCharacter().charAt(0))) event.consume();	});
		
		dcell.addEventFilter(KeyEvent.KEY_RELEASED, event ->
		{	
			if (verbose) System.out.println("KEY_RELEASED: " + event.getCharacter());
			if (!Character.isDigit(event.getCharacter().charAt(0))) event.consume();	});
	
		dcell.getStyleClass().add("numeric");
//		dcell.setOnEditCommit((CellEditEvent<ResultsRecord, Double> t) ->	{	getResultsRecord(t).setValue0(prefix, t.getNewValue());		});

		TableColumn<ResultsRecord, Double> val1 = new TableColumn<ResultsRecord, Double>();
		val1.setText("Val1");
		val1.setCellFactory(makeHeatmapCellFactory());
		val1.setMaxWidth(40);
//		val1.setCellValueFactory( cell -> cell.getValue().value1Property());
		resultsTable.getColumns().addAll(val0, val1);
	
		
	}
	
	//----------------------------------
	private Callback<TableColumn<ResultsRecord, String>, TableCell<ResultsRecord, String>> makeCallback()
	{
		return new Callback<TableColumn<ResultsRecord, String>, TableCell<ResultsRecord, String>>() {

        @Override public TableCell<ResultsRecord, String> call(TableColumn<ResultsRecord, String> p) {
             return new TableCell<ResultsRecord, String>() {
	            @Override public void updateItem(String item, boolean empty) {
	                super.updateItem(item, empty);
	                if (isEmpty()) {
	                    setText(null);
	                    this.setStyle("");
	                }
	                else
	                {
	                	double value = getItem().startsWith("H") ? 0.3 : 0.9;
	                	setBackground(Backgrounds.colored(ColorUtil.blueYellow(value)));
//	                	this.setStyle("-fx-background-color:" + (empty ? "white" : "yellow"));
	                    setText(item);
	                }
//	                else
//	                    

	            } 
            } ;
         
        }
		};
	}
	//----------------------------------
    private Callback<TableColumn<ResultsRecord, Double>, TableCell<ResultsRecord, Double>> makeHeatmapCellFactory() {
          return  new Callback<TableColumn<ResultsRecord, Double>, TableCell<ResultsRecord, Double>>() {
                public TableCell call(TableColumn p) {
                    TableCell cell = new TableCell<ResultsRecord, Double>() {
                        @Override
                        public void updateItem(Double item, boolean empty) {
                        	super.updateItem(item, empty);
                            setText(empty ? null : getString());
                    		this.setAlignment(Pos.CENTER_RIGHT);
    	                    if (!empty)
    	                    {
    	                    	TableRow<ResultsRecord> row = getTableRow();
    	                    	ResultsRecord rec = row.getItem();
    	                    	if (rec != null)
    	                    	{
    	                    		double val = rec.getValue0() / 10;
    	                    		if (val >= 0 && val <= 1)
    	                    			setBackground(Backgrounds.colored(ColorUtil.blueYellow(val)));
    	                    	}
    	                    }
                        }

                        private String getString() {
//                        	Color c = ColorUtil.blueYellow(Math.random());
//                    		setStyle("-fx-background-color:"+c.toString());
                        	Double d = getItem();
                            if (d == null) return "";
                            return String.format("%5.2f", d);
                        }
                    };


                    return cell;
                }
            };
    } 

	private ResultsRecord getResultsRecord(CellEditEvent<ResultsRecord, Double> t) 
	{ 
		try
		{
			int position = t.getTablePosition().getRow();
			return ((ResultsRecord) t.getTableView().getItems().get(position));
		}
		catch (Exception e)	{ return null;	}
	}
	
	private void addResultsTableColumn(String targ) {
		TableColumn existing = findResultsColumn(targ);
		if (existing != null) return;
		TableColumn<ResultsRecord, String> col = new TableColumn<ResultsRecord, String>();
		col.setText(targ);
		col.setCellValueFactory( cell -> cell.getValue().resultProperties(targ));
		col.setContextMenu(makeContextMenu());
		resultsTable.getColumns().add(col);
	}

//	private void addValue0TableColumn(String targ) {
//		TableColumn existing = findResultsColumn(targ);
//		if (existing != null) return;
//		TableColumn<ResultsRecord, Double> col = new TableColumn<ResultsRecord, Double>();
//		col.setText(targ);
//		col.setCellValueFactory( dcell -> dcell.getValue().value0Property());
//		col.setContextMenu(makeContextMenu());
//		resultsTable.getColumns().add(col);
//	}

	private ContextMenu makeContextMenu() {
		ContextMenu menu = new ContextMenu();
		menu.getItems().add(new MenuItem("Remove"));
		return menu;
	}

	private TableColumn findResultsColumn(String targ) {
		for (TableColumn col : resultsTable.getColumns())
			if (col.getText().equals(targ)) return col;
		return null;
	}
	
	// rebuild the columns in the resultsTable based on the selected items in the targetList
//	private void targetSelectionChanged(Observable ev)
//	{
////		resultsTable.getColumns().clear();
//	
//		List<String> targs = targetList.getSelectionModel().getSelectedItems();
//		int ct = 0;
//		search.setDisable(targs.isEmpty());
//		for (String target : targs)
//		{
//			col = new TableColumn<ResultsRecord, String>();
//			col.setText(target);
//			col.setCellValueFactory( cell -> cell.getValue().resultProperties(target));
//			resultsTable.getColumns().add(col);
//		}
//	}
	private Object fileDroppedInResultsTable(DragEvent ev) {
		
		Dragboard dboard = ev.getDragboard();
		if (dboard.hasFiles())
		{
			List<File> files = dboard.getFiles();
			for (File file : files)
			{
				if (FileUtil.isDataFile(file))
					addDataToIdTable(file);
			}
		}
		System.out.println(ev);
		return null;
	}
	
	private void addDataToIdTable(File file) {
		List<String> strs = FileUtil.readFileIntoStringList(file.getAbsolutePath());
		for (String s : strs)
		{
			String[] tokens = s.split(TAB);
			ResultsRecord rec = new ResultsRecord(getSpecies(), tokens[0]);
			resultsTable.getItems().add(rec);
		}
		
	}
	//----------------------------------------------------------------------------------
	public void start()
	{ 
	}
//----------------------------------------------------------------------------------
	public static String BDB = "http://webservice.bridgedb.org/";
//	public ObservableList<String> getSpeciesList()
//	{
//		ObservableList<String> speciesList = FXCollections.observableArrayList();
//		String urlStr = BDB + "contents";
//		String response = StringUtil.callURL(urlStr, true);
//		System.out.println(response);
//		for (String s : response.split("\n"))
//		{
//			if (s.length() > 0)
//			{
//				String[] flds = s.split("\t");
//				speciesList.add(flds[0] + " (" + flds[1] + ")");
//			}
//		}
//		return speciesList;
//	}
	//--------------------------------------------------------------------------------
	SimpleStringProperty speciesName = new SimpleStringProperty();
	Species currentSpecies;
	private void setSpecies(Species newSpecies)
	{
		currentSpecies = newSpecies;
		speciesName.set(newSpecies.common());
		readDataSources();	
		readTargetSources(newSpecies);
		resetIDguesses(newSpecies);
	}

	private void setSpeciesInfo(String newSpecies)
	{
		Species sp = Species.lookup(newSpecies);
		setSpecies(sp);
	}

	@FXML private void doSourceLinkout()
	{
		StringUtil.launchURL("https://github.com/bridgedb/BridgeDb/blob/master/org.bridgedb.bio/resources/org/bridgedb/bio/datasources.txt");
	}
	//--------------------------------------------------------------------------------
//	private void sourceChangeHandler(Number newSelectedIndex)
//	{
//		int idx = newSelectedIndex.intValue();
//		if (idx < 0) return;
//		targetList.getItems().clear();
//		DataSourceRecord rec = sourceTable.getItems().get(idx);
//		rec.checkSupportMap(getSpeciesShort(), this);
//		String supp = rec.getSupportedSystemsStr();
//		System.out.println("supported targets: " + supp);
//		for (String s : supp.split(" "))
//			targetList.getItems().add(systemToNameLookup.get(s));
//	}
	
//	//--------------------------------------------------------------------------------

	@FXML public void deleteSelectedRows()
	{
		List<Integer> ids = resultsTable.getSelectionModel().getSelectedIndices();
		int sz = ids.size();
		for (int i= sz-1; i >= 0; i--)
			resultsTable.getItems().remove(i);
	}
	
	@FXML public void addRow()
	{
		TextInputDialog dialog = new TextInputDialog("LPK");
		dialog.setTitle("ID Entry");
		dialog.setHeaderText("This is the name of the new row");
		dialog.setContentText("Please enter your gene:");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent())
		{
			System.out.println("Your gene: " + result.get());
		}

		// The Java 8 way to get the response value (with lambda expression).
		result.ifPresent(name -> resultsTable.getItems().add(
				new ResultsRecord(getSpecies(), name)));
	}
	
	private void resetIDguesses(Species newSpecies) {
		for (ResultsRecord rec : resultsTable.getItems())
			rec.reguess(newSpecies);
	}
	
	@FXML public void doSearch()
	{
		Species species = getSpecies();				//if (StringUtil.isEmpty(species)) return;
		if (species == null) 
			species = Species.Human;
		String idList = buildIdSysList();
		try
		{
			List<String> output = BridgeDbIdMapper.post(BDB, species.common(), "xrefsBatch", "", idList);
			fillTable(output);
		}
		catch(Exception ex) 
		{ 
			System.err.println(ex.getMessage());	
		}
	}
	
	private List<String> getTargetList() {
		List<TableColumn<ResultsRecord, ?>> cols = resultsTable.getColumns();
		List<String> targs = new ArrayList<String>();
		int len = cols.size();
		for (int i=2; i<len; i++)		// reserve two cols for original and source
		{	
			String colHeader = cols.get(i).getText();
			MappingSource targ = MappingSource.nameLookup(colHeader);
			if (targ != null)
				targs.add(targ.system());
		}
		return targs;
	}
	
	private ResultsRecord findRecord(String id)
	{
		if (id == null) return null;
		for (ResultsRecord rec : resultsTable.getItems())
			if (id.equals(rec.getOriginal()))
				return rec;
		return null;
	}
	
	private void fillTable(List<String> results)
	{
		List<String> targList = getTargetList();
		resultsTable.getItems().clear();
		for (int i=0; i < results.size(); i++)
		{
			String line = results.get(i);	
			System.out.println(line);	
			String [] flds = line.split("\t");
			if (flds.length == 3)
			{
				String id = flds[0];
				String source = flds[1];
				String allrefs = flds[2];
				ResultsRecord resultsRec = findRecord(id);
				if (resultsRec == null)
				{
					resultsRec = new ResultsRecord(targList);
					resultsTable.getItems().add(resultsRec);
				}
				resultsRec.setOriginal(id);
				resultsRec.setSource(source);
				for (String target : targList)
				{
					int idx = allrefs.indexOf(target + ":");
					if (idx >= 0)
					{
						int start = idx + target.length() + 1;
						int end = allrefs.indexOf(",", start );
						if (end < 0) end = allrefs.length();
						String mappedId = allrefs.substring(start, end);
						resultsRec.setByName(target, mappedId);
					}
				}
			}
		}

	}
	
	String TAB = "\t";
	String NL = "\n";

	
	private String buildIdSysList() {
		List<ResultsRecord> items = resultsTable.getItems();
		StringBuilder builder = new StringBuilder();
		for (ResultsRecord rec : items)
		{
			String sys = rec.getSource();
			if (!StringUtil.isEmpty(sys))
				builder.append( rec.getOriginal() + TAB + sys +  NL); 
		}
		return builder.toString();
	}
	
//	private String getSelectedTarget() {
//		return targetList.getSelectionModel().getSelectedItem();
//	}
	void testStringOnAllPatterns(String test)
	{
		for (DataSourceRecord rec : allDataSources)
			rec.patternMatch(test);
	}
	 

//	private Map<String, String> parseSearchResponse(String inID, String inTarget, String response)
//	{
//		Map<String, String> results = new HashMap<String, String>();
//		for (String line : response.split("\n"))
//		{
//			String [] parts = line.split("\t");
//			if (parts.length == 2)			// expecting 2 columns back: id + src
//			{
//				String id = parts[0];
//				String src = parts[1];
//				if (src.equals(inTarget))
//					results.put(inID, id);
//			}
//		}
//		return results;
//	}

	//----------------------------------------------------------------------------------
	ObservableList<DataSourceRecord> allDataSources = FXCollections.observableArrayList();
	ObservableList<DataSourceRecord> minimalDataSources = FXCollections.observableArrayList();
	ObservableList<DataSourceRecord> matchingDataSources = FXCollections.observableArrayList();
	
	public void readDataSources()
	{
		allDataSources.clear();
		minimalDataSources.clear();
		matchingDataSources.clear();
		List<String> lines = FileUtil.readFileIntoStringList("/Users/adamtreister/Desktop/datasourcesSubset.txt");
		for (String line : lines)			// read in all DataSourceRecords unfiltered
		{
			if (line.trim().length() == 0) continue;
			if (line.startsWith("name\tsystem")) continue;		// ignore first line as column heads
			DataSourceRecord rec= new DataSourceRecord(line);
			allDataSources.add(rec);
		}
		buildSourceMap(allDataSources);		// be able to convert system <-> name
//		loadSourceTable();					// filter full set down to a few and populate the table
	}
//	private void generateMatchingSourcesFields()
//	{
//		ObservableList<DataSourceRecord> visibleDataSources = FXCollections.observableArrayList();
//		visibleDataSources.addAll(sourceTable.getItems());
//		int i = 0;
//		StringBuilder builder = new StringBuilder();
//		for (DataSourceRecord rec : visibleDataSources)
//		{
//			System.err.println("checkSupportMapping " + i++);
//			rec.checkSupportMap(getSpeciesShort(), this);
//			builder.append(rec.getSystem() + "\t" + rec.getSupportedSystemsStr() + "\n");
//		}
//		FileUtil.writeTextFile(new File("/Users/adamtreister/Desktop/"), 
//				"join.txt", builder.toString());
//	}
	
	//--------------------------------------------------------------------------------
	// builds a string of all targets supporting this source
//	
//	public String targetsForRec(DataSourceRecord dataSource, String species) {
//		String systemSrc = dataSource.getSystem();
//		StringBuilder builder = new StringBuilder();		//systemSrc + "\t"
//		for (String targ : speciesTargets)
//		{
//			String targSys = nameToSystemLookup.get(targ);
//			if (targSys == null) continue;
//			String sys = dataSource.getSystem();
////			if (targSys.equals(systemSrc)) continue;		if this is removed its harder to compare the targets for any given source
//			String compatReq = "isMappingSupported/" + systemSrc + "/" + sys;
//			String response = bridgeDbcall(compatReq);
//			boolean supported = response.toLowerCase().contains("true");
//			if (supported) 
//				builder.append(targSys + " ");
//		}
//		System.out.println(systemSrc + " -> " + builder.toString());
//		return builder.toString();
//	}
	//--------------------------------------------------------------------------------
//	private String bridgeDbcall(String command)
//	{
////		System.out.println(command);
//		String species = organism.getValue().common();
////		int idx = species.indexOf(" (");
////		if (idx > 0) species = species.substring(0, idx);
//		String urlStr =BDB + species + "/" + command;
//		return StringUtil.callURL(urlStr, true);
//	}
//
	//--------------------------------------------------------------------------------
	HashMap<String, String> systemToNameLookup;
	HashMap<String, String> nameToSystemLookup;
	
	private void buildSourceMap(List<DataSourceRecord> sources) {
		systemToNameLookup= new HashMap<String, String>();
		nameToSystemLookup= new HashMap<String, String>();
		for (DataSourceRecord rec : sources)
		{
			String name = rec.getName();
			String sys=rec.getSystem();
			systemToNameLookup.put(sys, name);
			nameToSystemLookup.put(name, sys);
		}
	}
	//--------------------------------------------------------------------------------
//	private boolean allRowsVisible() 	{ return allRows.isSelected();  }
//	private boolean allColumns() 		{ return allColumns.isSelected();  }
//	@FXML private void showAllRows() 	{ loadSourceTable();  }
//	@FXML private void showAllColumns() { loadSourceTable();   }
	
	//--------------------------------------------------------------------------------
//	private void loadSourceTable() 
//	{   
//		ObservableList<DataSourceRecord> items = sourceTable.getItems();
//		items.clear();
////		items.addAll(allRows() ? allDataSources : minimalDataSources);
//
//		String input =  buildIdSysList();   // inputText.getText().trim();
//		if (input.length() > 0)
//			items.addAll(allDataSources.stream()
//				.filter(isSpeciesSpecific(getSpeciesLatin()))
////				.filter(positveGravity(allRowsVisible()))
//				.filter(inputMatchesPattern(input))
////				.filter(hasAvailableTargets())
//				.collect(Collectors.toList()));
//
//		if (items.size() > 0) 
//			sourceTable.getSelectionModel().select(0);
//
//		boolean showAll = allColumns();
//		TableColumn<DataSourceRecord, String>[] optionalCols = 
//				new TableColumn[]{urlCol, usageCol, entityCol, uriCol, regexCol,officialNameCol,gravityStrCol, targetsCol };
//		for (TableColumn col : optionalCols)
//			col.setVisible(showAll);
//	}
	Predicate<DataSourceRecord> nameEmpty = p -> p.getName() != null;
	public static Predicate<DataSourceRecord> isSpeciesSpecific() {
	    return rec -> StringUtil.isEmpty(rec.getExclusiveSpecies()); }

	public static Predicate<DataSourceRecord> isSpeciesSpecific(String species) {
	    return rec -> {
	    	String excl = rec.getExclusiveSpecies();
	    	return StringUtil.isEmpty(excl) || species.contains(excl);   };
	}
	
//	private static Predicate<DataSourceRecord> positveGravity(boolean allRows) {
//		return rec -> allRows || rec.gravity() > 0;
//	}
	
	private static Predicate<DataSourceRecord> inputMatchesPattern(String input) {
		return rec -> {
			if (input.trim().isEmpty()) return true;
			return rec.patternMatch(input);
		};
	}
//	private static Predicate<DataSourceRecord> hasAvailableTargets() {
//		return rec -> rec.anySupportedSystems();
//	}

	//--------------------------------------------------------------------------------
	// called when the organism is set, filters down targets for that species
	ObservableList<String> speciesTargets = FXCollections.observableArrayList();
	
	public void readTargetSources(Species species)
	{
		if ( null == species) return;
		String urlStr = BDB + species + "/targetDataSources";
		String response = StringUtil.callURL(urlStr, true);
		System.out.println(urlStr + "\n\n" + response+ "\n\n");
		speciesTargets.clear();
		targetList.getItems().clear();
		for (String s : response.split("\n"))
			if (!s.trim().isEmpty() && MappingSource.nameLookup(s) != null)
				speciesTargets.add(s);
		targetList.getItems().addAll(speciesTargets.sorted());
	}

	//--------------------------------------------------------------------------------
	public String getSpeciesName()		{ 		return speciesName.get();} 
	public Species getSpecies()			{ 		return Species.lookup(getSpeciesName());	} 

	public String getSpeciesShort()		{ 	
		String s = getSpeciesName(); 
		if (s == null) return "";
		int idx = s.indexOf(" (");
		if (idx >= 0)
			return s.substring(0, idx);
		return(s);
	} 
	
	public String getSpeciesLatin()		{ 	
		String s = getSpeciesName(); 
		if (s == null) return "";
		int idx = s.indexOf(" (");
		return s.substring(idx, s.indexOf(")"));
	} 

}
