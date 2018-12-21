package diagrams.pViz.tables;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import diagrams.pViz.app.App;
import diagrams.pViz.app.Controller;
import gui.DraggableTableRow;
import icon.FontAwesomeIcons;
import icon.GlyphIcon;
import icon.GlyphsDude;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import javafx.scene.image.WritableImage;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Pair;
import model.IController;
import model.bio.ISpeciesSpecific;
import model.bio.PathwayRecord;
import model.bio.Species;
import util.FileUtil;
import util.StringUtil;

//http://www.wikipathways.org/index.php/Help:WikiPathways_Webservice/API

public class PathwayController implements Initializable, IController, ISpeciesSpecific  {

	@FXML private TableView<PathwayRecord> pathwayTable;
	@FXML private TableColumn<PathwayRecord, String> idColumn;
	@FXML private TableColumn<PathwayRecord, String> nameColumn;
	@FXML private TableColumn<PathwayRecord, String> revisionColumn;
	@FXML private TableColumn<PathwayRecord, String> urlColumn;
	@FXML private TableColumn<PathwayRecord, String> speciesColumn;

	@FXML private ChoiceBox<String> species;
	@FXML private Button search;
	@FXML private TextField searchBox;
	@FXML private Button cancel;
	@FXML private Button preview;
	@FXML private Button view;
	@FXML private Button edit;

	static TableRow<PathwayRecord> thisRow = null;
	private Controller parentController;
	public void setParentController(Controller c)	{ parentController = c;	}
	private String state;
	public void setState(String s) { 	state = s; }
	public String getState() 	{ 	return state; 	}

	//---------------------------------------------------------------------------
	public static String HUMAN_PATHWAYS = "http://webservice.wikipathways.org/listPathways?organism=Homo%20sapiens";
	
	public static String tooltip = "Combine terms with AND and OR. Combining terms with a space is equal to using OR ('p53 OR apoptosis' gives the same result as 'p53 apoptosis').\n" + 
			"Group terms with parentheses, e.g. '(apoptosis OR mapk) AND p53'\n" +
			" You can use wildcards * and ?. * searches for one or more characters, ? searchers for only one character.\n" +
			" Use quotes to escape special characters. E.g. 'apoptosis*' will include the * in the search and not use it as wildcard.";
	
	//---------------------------------------------------------------------------
	static String lastSearch = "ATF2";
			
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		String[] organisms = { "Any", "Homo sapiens", "Mus musculus", "Rattus norvegicus", "Canis familiarus", "Box taurus", "Pan troglodytes", "Gallus gallus" };
		species.getItems().addAll(organisms);
		species.getSelectionModel().select(1);
		
		search.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.SEARCH, GlyphIcon.DEFAULT_ICON_SIZE));
		search.setText("");
		search.setTooltip(new Tooltip(tooltip));
		searchBox.setTooltip(new Tooltip(tooltip));
		searchBox.setText(lastSearch);
		searchBox.addEventHandler(KeyEvent.KEY_PRESSED, e -> {  if (e.getCode() == KeyCode.ENTER) doSearch();	} );
		searchBox.selectAll();
		if (pathwayTable != null)
			setupPathwayTable();
		
//		getAllPathways(PathwayController.HUMAN_PATHWAYS);
		
		
	}
	static public void viewPathway(PathwayRecord rec)		
	{ 
		String url = rec.getUrl();
		String result = StringUtil.callURL(url, false);   // TODO threaded
		viewPathway(result);
	}	
	static public void downloadathway(PathwayRecord rec, File output)		
	{ 
		String url = rec.getUrl();
		String result = StringUtil.callURL(url, false);   // TODO threaded
		viewPathway(result);
	}
	static public void viewPathwayAsImage(PathwayRecord rec)		
	{ 
		String url = rec.getPngUrl();
//		Thread thread = new Thread() {
		String result = StringUtil.callURL(url, false);   // TODO threaded
		String png64  =StringUtil.readTag( result, "ns1:data");
		Decoder decoder = Base64.getDecoder();
		byte[] imageBytes  = decoder.decode(png64);
		InputStream stream = new ByteArrayInputStream(imageBytes);
		WritableImage wimg;
		BufferedImage bufferedimage = null;
		try {
			 bufferedimage = ImageIO.read(stream);
			 wimg = new WritableImage(bufferedimage.getWidth(),  bufferedimage.getHeight());
			wimg = SwingFXUtils.toFXImage(bufferedimage, wimg);
			App.showImage(rec.getName(), wimg);
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	static private void viewPathway(String inputStr)		
	{ 		
//		String pathwayFxml = "../gpml/GeneList.fxml";
//		String fullname = "../gpml/GeneList.fxml";
		Pair<FXMLLoader, Stage> pair = App.buildStage("Pathway", "PViz.fxml", 1200, 800);
		Controller newController = pair.getKey().getController();
		try
		{
			String gpml  =StringUtil.readTag( inputStr, "ns2:gpml");
			Decoder decoder = Base64.getDecoder();
			byte[] cleanXML  = decoder.decode(gpml);
			String str = new String(cleanXML);
			newController.open(str);
			pair.getValue().show();
		}
		catch (Exception e) {}
	}
	public void getPathwayAt(Integer o) {
	}
	static private void downloadPathway(String inputStr, File output)		
	{ 		
		String gpml  =StringUtil.readTag( inputStr, "ns2:gpml");
		byte[] cleanXML  = Base64.getDecoder().decode(gpml);
		String str = new String(cleanXML);
		FileUtil.writeTextFile(output, "out", str);
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

	public void viewPathwayByIndex(String idx) {
		if (StringUtil.isInteger(idx))
		{
			int i = StringUtil.toInteger(idx);
			PathwayRecord rec = pathwayTable.getItems().get(i);
			viewPathway(rec, false);
		}
	}
	
	public void viewPathway(PathwayRecord rec, boolean edit) {
//		if (parentController != null)
//		{
			if (edit) viewPathway(rec);
			else viewPathwayAsImage(rec);
//		}
//		else System.err.println("no parentController in viewPathway");
	}
	
	public void getInfo(DataFormat mimetype, String a, String colname, MouseEvent ev) {
//		System.out.println("getInfo: " + a + " Fetching: " + ev);	
		if (StringUtil.isInteger(a))
		{
			int idx = StringUtil.toInteger(a);
			PathwayRecord rec = pathwayTable.getItems().get(idx);
			boolean edit = !ev.isShiftDown();
			viewPathway(rec, edit);
		}
	}
	@FXML public void cancel()	
	{
		
	}
	//---------------------------------------------------------------------------
	@FXML public void preview()		{	processSelection("PREVIEW");}
	@FXML public void download()	{	processSelection("DOWNLOAD");}
	@FXML public void edit	()		{   processSelection("EDIT");	}
	
	private void processSelection(String verb)
	{
		List<Integer> selected = pathwayTable.getSelectionModel().getSelectedIndices();
		for (int idx : selected)
		{
			PathwayRecord rec = pathwayTable.getItems().get(idx);
			boolean download =  "DOWNLOAD".equals(verb);
			boolean editable = "EDIT".equals(verb);
			if (download)
			{
				String url = rec.getUrl();
				String result = StringUtil.callURL(url, false);   // TODO threaded
				downloadPathway(result, new File("~/test.output.gpml"));
			}
			else 	viewPathway( rec, editable);
		}
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
		       return new DraggableTableRow<PathwayRecord>(pathwayTable, PATHWAY_MIME_TYPE, this, null);
			});
		}
	@Override
	public Species getSpecies() {
		return parentController == null ? Species.Human : parentController.getSpecies();		// TODO should assertNonNull parentController?
	}
	@Override public void resetTableColumns() {	}
	@Override public void reorderColumns(int a, int b) {	}

}
