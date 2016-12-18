package diagrams.pViz.tables;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import diagrams.pViz.app.Controller;
import icon.FontAwesomeIcons;
import icon.GlyphIcon;
import icon.GlyphsDude;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import model.bio.PathwayRecord;
import util.FileUtil;
import util.StringUtil;

//http://www.wikipathways.org/index.php/Help:WikiPathways_Webservice/API

public class PathwayController implements Initializable  {

	@FXML private TableView<PathwayRecord> pathwayTable;
	@FXML private ChoiceBox<String> species;
	@FXML private Button search;
	@FXML private TextField searchBox;
	@FXML private TableColumn<PathwayRecord, String> idColumn;
	@FXML private TableColumn<PathwayRecord, String> nameColumn;
	@FXML private TableColumn<PathwayRecord, String> revisionColumn;
	@FXML private TableColumn<PathwayRecord, String> urlColumn;
	@FXML private TableColumn<PathwayRecord, String> speciesColumn;

	static TableRow<PathwayRecord> thisRow = null;
	private Controller parentController;
	public void setController(Controller c)	{ parentController = c;	}

	//---------------------------------------------------------------------------
	public static String HUMAN_PATHWAYS = "http://webservice.wikipathways.org/listPathways?organism=Homo%20sapiens";
	
	public static String tooltip = "Combine terms with AND and OR. Combining terms with a space is equal to using OR ('p53 OR apoptosis' gives the same result as 'p53 apoptosis').\n" + 
			"Group terms with parentheses, e.g. '(apoptosis OR mapk) AND p53'\n" +
			" You can use wildcards * and ?. * searches for one or more characters, ? searchers for only one character.\n" +
			" Use quotes to escape special characters. E.g. 'apoptosis*' will include the * in the search and not use it as wildcard.";
	
	//---------------------------------------------------------------------------
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		String[] organisms = { "Any", "Homo sapiens", "Mus musculus", "Rattus norvegicus", "Canis familiarus", "Box taurus", "Pan troglodytes", "Gallus gallus" };
		species.getItems().addAll(organisms);
		species.getSelectionModel().select(1);
		
		search.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.SEARCH, GlyphIcon.DEFAULT_ICON_SIZE));
		search.setText("");
		search.setTooltip(new Tooltip(tooltip));
		searchBox.setTooltip(new Tooltip(tooltip));
		searchBox.setText("ATF2");
		searchBox.addEventHandler(KeyEvent.KEY_PRESSED, e -> {  if (e.getCode() == KeyCode.ENTER) doSearch();	} );
		searchBox.selectAll();
		if (pathwayTable != null)
			setupPathwayTable();
		
//		getAllPathways(PathwayController.HUMAN_PATHWAYS);
		
		
	}
	//---------------------------------------------------------------------------
	String allPathwaysCache;
	private void getAllPathways(String url) {
		pathwayTable.getItems().clear();
		List<PathwayRecord> paths = getPathways(url);
		pathwayTable.getItems().addAll(paths);
	}
	//---------------------------------------------------------------------------
	
	private List<PathwayRecord> getScoredPathways(String url) {
		List<PathwayRecord> list = new ArrayList<PathwayRecord>();
		url = url.replace(" ", "%20");
		String result = StringUtil.callURL(url, false);
		result = result.replaceAll("\t\t", "\n");

		try {
			Document doc = FileUtil.convertStringToDocument(result);
			if (doc != null)
			{
				NodeList nodes = doc.getElementsByTagName("ns1:result");
				int sz = nodes.getLength();
				for (int i=0; i<sz; i++)
				{
					PathwayRecord rec = new PathwayRecord(nodes.item(i));
					list.add(rec);
				}
			}		
		}
		catch (Exception e) {}
		return list;
	}
	
	private List<PathwayRecord> getPathways(String url) {
		List<PathwayRecord> list = new ArrayList<PathwayRecord>();
		url = url.replace(" ", "%20");
		String result = StringUtil.callURL(url, false);
		result = result.replaceAll("\t\t", "\n");

		try {
			Document doc = FileUtil.convertStringToDocument(result);
			if (doc != null)
			{
				NodeList nodes = doc.getElementsByTagName("ns1:pathways");
				int sz = nodes.getLength();
				for (int i=0; i<sz; i++)
				{
	//				Node node = nodes.item(i);
//					NodeList paths = doc.getElementsByTagName("ns1:result");
//					int nPaths = paths.getLength();
//					for (int j=0; j<nPaths; j++)
//					{
						PathwayRecord rec = new PathwayRecord(nodes.item(i));
						list.add(rec);
//					}
				}
			}		
		}
		catch (Exception e) {}
		return list;
	}

	
	
	   //---------------------------------------------------------------------------
	public static String FIND_PATHWAYS_BASE = "http://webservice.wikipathways.org/findPathwaysByText?";
	@FXML public void doSearch()	
	{
		String text = searchBox.getText().trim();
		if (StringUtil.isEmpty(text)) 			return;
		
		String queryText = "", speciesComponent = "";
		
		if (StringUtil.hasText(text))
			queryText = "query=" + text;
		String selection = species.getSelectionModel().getSelectedItem();
		if ( !"Any".equals(selection))
			speciesComponent = "species=" + selection.replace(" ", "%20")  + "&";
				
		String query = FIND_PATHWAYS_BASE;
		query += speciesComponent + queryText;
		System.out.println(query);
		pathwayTable.getItems().clear();
		List<PathwayRecord> paths = getScoredPathways(query);
		pathwayTable.getItems().addAll(paths);
		
	}
	
	public void openByReference(String ref) {
		for (PathwayRecord rec : pathwayTable.getItems())
			if (ref.equals(rec.getId()))
			{
			}
	}
   //---------------------------------------------------------------------------
	
	public static final DataFormat PATHWAY_MIME_TYPE = new DataFormat("application/x-java-serialized-object2");

	private void setupPathwayTable()
	{
//		System.out.println("setupPathwayTable");
//		TableColumn[] allCols = { idColumn, urlColumn, nameColumn, speciesColumn, revisionColumn };

		idColumn.setCellValueFactory(new PropertyValueFactory<PathwayRecord, String>("id"));
		urlColumn.setCellValueFactory(new PropertyValueFactory<PathwayRecord, String>("url"));
		nameColumn.setCellValueFactory(new PropertyValueFactory<PathwayRecord, String>("name"));
		speciesColumn.setCellValueFactory(new PropertyValueFactory<PathwayRecord, String>("species"));
		revisionColumn.setCellValueFactory(new PropertyValueFactory<PathwayRecord, String>("revision"));

//		speciesColumn.setVisible(false);
		urlColumn.setVisible(false);
		
		
		pathwayTable.setRowFactory(tv -> {
	        TableRow<PathwayRecord> row = new TableRow<>();

	        row.setOnDragDetected(event -> {
	            if (! row.isEmpty()) {
	                Integer index = row.getIndex();
	                Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
	                db.setDragView(row.snapshot(null, null));
	                ClipboardContent cc = new ClipboardContent();
	                cc.put(PATHWAY_MIME_TYPE, index);
	                db.setContent(cc);
	                event.consume();
	                thisRow = row;
	            }
	        });

	        row.setOnDragEntered(event -> {
	            Dragboard db = event.getDragboard();
	            if (db.hasContent(PATHWAY_MIME_TYPE)) {
	                if (row.getIndex() != ((Integer)db.getContent(PATHWAY_MIME_TYPE)).intValue()) {
	                    event.acceptTransferModes(TransferMode.MOVE);
	                    event.consume();
	                    thisRow = row;
//	                  if (thisRow != null) 
//	                 	   thisRow.setOpacity(0.3);
	                }
	            }
	        });

	        row.setOnDragExited(event -> {
	            if (event.getGestureSource() != thisRow &&
	                    event.getDragboard().hasString()) {
//	               if (thisRow != null) 
//	            	   thisRow.setOpacity(1);
	               thisRow = null;
	            }
	        });

	        row.setOnDragOver(event -> {
	            Dragboard db = event.getDragboard();
	            if (db.hasContent(PATHWAY_MIME_TYPE)) {
	                if (row.getIndex() != ((Integer)db.getContent(PATHWAY_MIME_TYPE)).intValue()) {
	                    event.acceptTransferModes(TransferMode.MOVE);
	                    event.consume();
	                }
	            }
	        });

	        row.setOnMouseClicked(event -> {
	        	if (event.getClickCount() == 2)
	            {
	                int idx = row.getIndex();
	        		getInfo(idx);
	              event.consume();
	            }
	        });

	        row.setOnDragDropped(event -> {
	            Dragboard db = event.getDragboard();
	            if (db.hasContent(PATHWAY_MIME_TYPE)) {
	                int draggedIndex = (Integer) db.getContent(PATHWAY_MIME_TYPE);
	                PathwayRecord draggedNode = pathwayTable.getItems().remove(draggedIndex);

	                int  dropIndex = (row.isEmpty()) ? pathwayTable.getItems().size() : row.getIndex();
	                pathwayTable.getItems().add(dropIndex, draggedNode);

	                event.setDropCompleted(true);
	                pathwayTable.getSelectionModel().select(dropIndex);
	                event.consume();
//	                if (thisRow != null) 
//	             	   thisRow.setOpacity(1);
	                thisRow = null;
	            }
	        });

	        return row ;
	    });

		
	}
	private void getInfo(int idx) {
		PathwayRecord rec = pathwayTable.getItems().get(idx);
		String url = rec.getUrl();
		System.out.println("getInfo: " + rec.getId() + " Fetching: " + url);					//TODO
		String result = StringUtil.callURL(url, false);
		if (parentController != null)
			parentController.viewPathway(result);
		else 
			System.err.println("launching the gene list controller is deprecated");
	}
}
