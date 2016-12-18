package diagrams.pViz.tables;

import java.net.URL;
import java.util.ResourceBundle;

import diagrams.pViz.app.Controller;
import diagrams.pViz.gpml.GPML;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import model.bio.Gene;


public class GeneListController  implements Initializable  {

	@FXML private TableView<Gene> geneListTable;
//	@FXML private ChoiceBox<String> species;
//	@FXML private Button search;
//	@FXML private TextField searchBox;
	@FXML private MenuItem info;
	@FXML private MenuItem addColumn;
	@FXML private MenuItem chart;
	@FXML private MenuItem groupSelectedGenes;

	public static String tooltip = "GENELIST TOOLTIP";
	@Override public void initialize(URL location, ResourceBundle resources)
	{
//		String[] organisms = { "Any", "Homo sapiens", "Mus musculus", "Rattus norvegicus", "Canis familiarus", "Box taurus", "Pan troglodytes", "Gallus gallus" };
//		species.getItems().addAll(organisms);
//		species.getSelectionModel().select(1);
//		
//		search.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.SEARCH, GlyphIcon.DEFAULT_ICON_SIZE));
//		search.setText("");
//		search.setTooltip(new Tooltip(tooltip));
//		searchBox.setTooltip(new Tooltip(tooltip));
//		searchBox.setText("*");
//		searchBox.addEventHandler(KeyEvent.KEY_PRESSED, e -> {  if (e.getCode() == KeyCode.ENTER) doSearch();	} );
		
//		if (geneListTable != null)
//			setupGeneListTable();
		String url = "";				// TODO
//		findGenes(url);
	}

	private Controller parentController;
	public void setParentController(Controller inParent) {
		parentController = inParent;
	
	}

	GPML gpmlReader;

//	public GeneList convertGPMLToGeneList(String gpml)
//	{
//		try
//		
//		{
//			gpml =StringUtil.readTag(gpml, "ns2:gpml");
//			Decoder decoder = Base64.getDecoder();
//			byte[] cleanXML  = decoder.decode(gpml);
//			String str = new String(cleanXML);
////		System.out.println(str);
//		
//			if (gpmlReader == null && parentController != null)
//				gpmlReader = new GPML(parentController.getDrawModel());
//			Document doc = FileUtil.convertStringToDocument(str);
//			if (doc != null)
//			{
//				GeneList genes = gpmlReader.readGeneList(doc);
//				return genes;
//			}
//		}
//		catch (Exception e)
//		{
//			System.out.println("populateTable failed.  Handle parse errors.");
//		}
//		return null;
//	}
//	public void populateTable(String results)
//	{
//		GeneList genes = convertGPMLToGeneList(results);
//		populateTable(genes);
//	}
//
	public static String FIND_PATHWAYS_BASE = "http://webservice.wikipathways.org/findPathwaysByText?";
	@FXML private void doAddColumn()	
	{
		System.out.println("doAddColumn");
	}
	@FXML private void doChart()	
	{
		System.out.println("doChart");
	}
	@FXML private void getInfo()	
	{
		System.out.println("getInfo");
	}
	@FXML private void groupSelectedGenes()	
	{
		System.out.println("groupSelectedGenes");
	}
	
//	@FXML private void doSearch()	
//	{
//		String text = searchBox.getText().trim();
//		if (StringUtil.isEmpty(text)) 			return;
//		
//		String queryText = "", speciesComponent = "";
//		
//		if (StringUtil.hasText(text))
//			queryText = "query=" + text;
//		String selection = species.getSelectionModel().getSelectedItem();
//		if ( !"Any".equals(selection))
//			speciesComponent = "species=" + selection  + "&";
//				
//		String query = FIND_PATHWAYS_BASE;
//		query += speciesComponent + queryText;
//		System.out.println("SEARCH ACTION HERE");
////		findGenes(query);
//	}
	
	public void openByReference(String ref) {
		for (Gene rec : geneListTable.getItems())
			if (ref.equals(rec.getId()))
			{
				System.out.println("openByReference " + ref);
			}
	}
   //---------------------------------------------------------------------------

//	private ImageView imgView = null;
	private void getGeneList(String result) {
		System.out.println("getGeneList");
		
	}
}
