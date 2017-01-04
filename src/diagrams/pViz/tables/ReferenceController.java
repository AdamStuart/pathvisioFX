package diagrams.pViz.tables;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import database.forms.EntrezQuery;
import diagrams.pViz.app.Controller;
import diagrams.pViz.app.IController;
import diagrams.pViz.app.ISpeciesSpecific;
import icon.FontAwesomeIcons;
import icon.GlyphIcon;
import icon.GlyphsDude;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import model.bio.BiopaxRecord;
import model.bio.PathwayRecord;
import model.bio.Species;
import util.FileUtil;
import util.StringUtil;

//http://www.wikipathways.org/index.php/Help:WikiPathways_Webservice/API

public class ReferenceController implements Initializable, IController, ISpeciesSpecific  {

    private static final DataFormat BIOPAX_MIME_TYPE = new DataFormat("application/x-java-biopax");

    @FXML private TableView<BiopaxRecord> refTable;
	@FXML private TableColumn<BiopaxRecord, String> refCol;
	@FXML private TableColumn<BiopaxRecord, String> idCol;
	@FXML private TableColumn<BiopaxRecord, String> dbCol;
	@FXML private TableColumn<BiopaxRecord, String> authorCol;
	@FXML private TableColumn<BiopaxRecord, String> titleCol;
	@FXML private TableColumn<BiopaxRecord, String> sourceCol;
	@FXML private TableColumn<BiopaxRecord, String> yearCol;

	@FXML private Button search;
	@FXML private TextField searchBox;

	static TableRow<PathwayRecord> thisRow = null;
	private Controller parentController;
	public void setParentController(Controller c)	{ parentController = c;	}

	static public void showPMIDInfo(String pmid)
	{
	   String text = EntrezQuery.getPubMedAbstract(pmid);
	   if (StringUtil.hasText(text))
	   {  
		   Alert a = new Alert(AlertType.INFORMATION, text);
		   a.setHeaderText("PubMed Abstract");
		   a.getDialogPane().setMinWidth(600);
		   a.setResizable(true);
		   a.showAndWait();
	   }
	}
	//---------------------------------------------------------------------------
	public static String HUMAN_PATHWAYS = "http://webservice.wikipathways.org/listPathways?organism=Homo%20sapiens";
	
	public static String tooltip = " ";
	//---------------------------------------------------------------------------
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		search.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.SEARCH, GlyphIcon.DEFAULT_ICON_SIZE));
		search.setText("");
		search.setTooltip(new Tooltip(tooltip));
		searchBox.setTooltip(new Tooltip(tooltip));
		searchBox.setText("ATF2");
		searchBox.addEventHandler(KeyEvent.KEY_PRESSED, e -> {  if (e.getCode() == KeyCode.ENTER) doSearch();	} );
		searchBox.selectAll();
		if (refTable != null)
			setupReferenceTable();
		
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
	
	private List<BiopaxRecord> getScoredReferences(String url) {
		List<BiopaxRecord> list = new ArrayList<BiopaxRecord>();
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
					BiopaxRecord rec = new BiopaxRecord(nodes.item(i));
					list.add(rec);
				}
			}		
		}
		catch (Exception e) {}
		return list;
	}
	
	public static List<BiopaxRecord> getRefs(String url) {
		List<BiopaxRecord> list = new ArrayList<BiopaxRecord>();
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
					BiopaxRecord rec = new BiopaxRecord(nodes.item(i));
					list.add(rec);
				}
			}		
		}
		catch (Exception e) {}
		return list;
	}

	public void viewReferenceByIndex(String idx) {
		if (StringUtil.isInteger(idx))
		{
			int i = StringUtil.toInteger(idx);
			BiopaxRecord rec = refTable.getItems().get(i);
			viewReference(rec, false);
		}
	}
	
	public void viewReference(BiopaxRecord rec, boolean edit) { 	showPMIDInfo(rec.getId());	}
	
	public void getInfo(DataFormat mimetype, String a, String colname, MouseEvent ev) {
//		System.out.println("getInfo: " + a + " Fetching: " + ev);	
		if (StringUtil.isInteger(a))
		{
			int idx = StringUtil.toInteger(a);
			BiopaxRecord rec = refTable.getItems().get(idx);
			boolean edit = ev.isShiftDown();
			viewReference(rec, edit);
		}
	}

	
	   //---------------------------------------------------------------------------
	public static String REFERENCE_QUERY = "https://";
//	public static String FIND_PATHWAYS_BASE = "http://webservice.wikipathways.org/findPathwaysByText?";
	@FXML public void doSearch()	
	{

		String text = searchBox.getText().trim();
		if (StringUtil.isEmpty(text)) 			return;
//		String queryText = "", speciesComponent = "";
//		if (StringUtil.hasText(text))
//			queryText = "query=" + text;
//		String selection = species.getSelectionModel().getSelectedItem();
//		if ( !"Any".equals(selection))
//			speciesComponent = "species=" + selection.replace(" ", "%20")  + "&";
				
		String query = REFERENCE_QUERY;
//		query += speciesComponent + queryText;
		System.out.println(query);
		refTable.getItems().clear();
		List<BiopaxRecord> paths = getScoredReferences(query);
		refTable.getItems().addAll(paths);
		
	}
	
	public void openByReference(String ref) {
		for (BiopaxRecord rec : refTable.getItems())
			if (ref.equals(rec.getId()))
			{
			}
	}
   //---------------------------------------------------------------------------
	
	private void setupReferenceTable()
	{
//		System.out.println("setupPathwayTable");

		refCol.setCellValueFactory(new PropertyValueFactory<BiopaxRecord, String>("xrefid"));
		idCol.setCellValueFactory(new PropertyValueFactory<BiopaxRecord, String>("id"));
		dbCol.setCellValueFactory(new PropertyValueFactory<BiopaxRecord, String>("db"));
		authorCol.setCellValueFactory(new PropertyValueFactory<BiopaxRecord, String>("authors"));
		titleCol.setCellValueFactory(new PropertyValueFactory<BiopaxRecord, String>("title"));
		sourceCol.setCellValueFactory(new PropertyValueFactory<BiopaxRecord, String>("source"));
		yearCol.setCellValueFactory(new PropertyValueFactory<BiopaxRecord, String>("year"));
		refTable.setRowFactory((a) -> {  return new DraggableTableRow<BiopaxRecord>(refTable, BIOPAX_MIME_TYPE, this);   });
	}
	@Override
	public Species getSpecies() {
		return parentController == null ? Species.Human : parentController.getSpecies();		// TODO should assertNonNull parentController?
	}

}
