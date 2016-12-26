package diagrams.pViz.tables;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import diagrams.pViz.app.Controller;
import diagrams.pViz.app.IController;
import icon.FontAwesomeIcons;
import icon.GlyphIcon;
import icon.GlyphsDude;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import model.bio.PathwayRecord;
import util.FileUtil;
import util.StringUtil;

//http://www.wikipathways.org/index.php/Help:WikiPathways_Webservice/API

public class PathwayController implements Initializable, IController  {

	@FXML private TableView<PathwayRecord> pathwayTable;
	@FXML private TableColumn<PathwayRecord, String> idColumn;
	@FXML private TableColumn<PathwayRecord, String> nameColumn;
	@FXML private TableColumn<PathwayRecord, String> revisionColumn;
	@FXML private TableColumn<PathwayRecord, String> urlColumn;
	@FXML private TableColumn<PathwayRecord, String> speciesColumn;

	@FXML private ChoiceBox<String> species;
	@FXML private Button search;
	@FXML private TextField searchBox;

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
//	String allPathwaysCache;
//	private void getAllPathways(String url) {
//		pathwayTable.getItems().clear();
//		List<PathwayRecord> paths = getPathways(url);
//		pathwayTable.getItems().addAll(paths);
//	}
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
	
	public static List<PathwayRecord> getPathways(String url) {
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
					PathwayRecord rec = new PathwayRecord(nodes.item(i));
					list.add(rec);
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
	
	public static final DataFormat PATHWAY_MIME_TYPE = new DataFormat("application/x-java-serialized-pathway");

	private void setupPathwayTable()
	{
//		System.out.println("setupPathwayTable");
//		TableColumn[] allCols = { idColumn, urlColumn, nameColumn, speciesColumn, revisionColumn };
		pathwayTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		idColumn.setCellValueFactory(new PropertyValueFactory<PathwayRecord, String>("id"));
		urlColumn.setCellValueFactory(new PropertyValueFactory<PathwayRecord, String>("url"));
		nameColumn.setCellValueFactory(new PropertyValueFactory<PathwayRecord, String>("name"));
		speciesColumn.setCellValueFactory(new PropertyValueFactory<PathwayRecord, String>("species"));
		revisionColumn.setCellValueFactory(new PropertyValueFactory<PathwayRecord, String>("revision"));

//		speciesColumn.setVisible(false);
		urlColumn.setVisible(false);
		
		pathwayTable.setRowFactory((a) -> {
		       return new DraggableTableRow<PathwayRecord>(pathwayTable, PATHWAY_MIME_TYPE, this);
			    });
		
	}
	public void getInfo(DataFormat mimetype, String a, String b) {
		System.out.println("getInfo: " + a + " Fetching: " + b);	
		if (StringUtil.isInteger(a))
		{
			int idx = StringUtil.toInteger(a);
			PathwayRecord rec = pathwayTable.getItems().get(idx);
			boolean edit = true;
			if (parentController != null)
			{
				if (edit)  parentController.viewPathway(rec);
				else parentController.viewPathwayAsImage(rec);
			}
		}
//		return;
//		System.out.println("getInfo: " + rec.getId() + " Fetching: " + url);					//TODO
//		else 
//			System.err.println("launching the gene list controller is deprecated");
	}
}
