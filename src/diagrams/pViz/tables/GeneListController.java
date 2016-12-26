package diagrams.pViz.tables;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import animation.BorderPaneAnimator;
import diagrams.pViz.app.Controller;
import diagrams.pViz.gpml.GPML;
import diagrams.pViz.model.Model;
import icon.FontAwesomeIcons;
import icon.GlyphIcon;
import icon.GlyphsDude;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import model.bio.Gene;
import util.StringUtil;


public class GeneListController  implements Initializable  {
	
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

	@FXML private TableView<ColumnRecord> columnTable;
	@FXML private TableColumn<ColumnRecord, String> typeColumn;
	@FXML private TableColumn<ColumnRecord, String> colNameColumn;
	@FXML private TableColumn<ColumnRecord, String> widthColumn;

	
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

	public static String tooltip = "GENELIST TOOLTIP";
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		Controller.setGraphic(westSidebar, FontAwesomeIcons.ARROW_CIRCLE_O_RIGHT);
		new BorderPaneAnimator(geneListContainer, westSidebar, Side.LEFT, false, 250);
		westSidebar.fire();		// start with columns hidden
		//-------
		typeColumn.setCellValueFactory(new PropertyValueFactory<ColumnRecord, String>("type"));
		colNameColumn.setCellValueFactory(new PropertyValueFactory<ColumnRecord, String>("name"));
		widthColumn.setCellValueFactory(new PropertyValueFactory<ColumnRecord, String>("colWidth"));
		columnTable.setRowFactory((a) -> {
		       return new DraggableTableRow<ColumnRecord>(columnTable, COLUMN_MIME_TYPE, parentController);
			    });
		for (TableColumn col : geneListTable.getColumns())
		{
			ColumnRecord rec = new ColumnRecord(col);
			columnTable.getItems().add(rec);
		}
		//-------
		geneNameColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("name"));
		geneIdColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("ensembl"));
		urlColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("url"));
		locationColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("location"));
		databaseColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("database"));
		dbidColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("dbid"));
		dataColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("data"));
		termsColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("termSummary"));

		geneListTable.setRowFactory((a) -> {
		       return new DraggableTableRow<Gene>(geneListTable, GENE_MIME_TYPE, parentController);
			    });
		
		//-------
		String[] organisms = { "Any", "Homo sapiens", "Mus musculus", "Rattus norvegicus", "Canis familiarus", "Box taurus", "Pan troglodytes", "Gallus gallus" };
		species.getItems().addAll(organisms);
		species.getSelectionModel().select(1);
		
		search.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.SEARCH, GlyphIcon.DEFAULT_ICON_SIZE));
		search.setText("");
		search.setTooltip(new Tooltip(tooltip));
		searchBox.setTooltip(new Tooltip(tooltip));
		searchBox.setText("*");
		searchBox.addEventHandler(KeyEvent.KEY_PRESSED, e -> {  if (e.getCode() == KeyCode.ENTER) doSearch();	} );
		
//		if (geneListTable != null)
//			setupGeneListTable();
		String url = "";				// TODO
//		findGenes(url);
	}

	//------------------------------------------------------
	private Controller parentController;
	public void setParentController(Controller inParent) {		parentController = inParent;	}

	private GPML gpmlReader;

	public void loadTables(Model model) {
		geneListTable.getItems().addAll(model.getGenes());
		
	}
	public void loadTables(List<Gene> genes) {
		geneListTable.getItems().addAll(genes);
	}

	public static String FIND_PATHWAYS_BASE = "http://webservice.wikipathways.org/findPathwaysByText?";
	@FXML private void doAddColumn()	
	{
		System.out.println("doAddColumn");
	}
	@FXML private void browsePathways()	
	{
		System.out.println("browsePathways");
		if (parentController != null)
			parentController.browsePathways();
	}
	@FXML private void doChart()	
	{
		System.out.println("doChart");
		long start = System.currentTimeMillis();
		VBox histos = parentController.getModel().buildHypercube();
		long complete = System.currentTimeMillis();
		System.out.println("took: " + (complete - start) + " ms");
		parentController.buildStage("Histogram List", histos);
	}
	@FXML private void getInfo()				{		System.out.println("getInfo");	}
	@FXML private void groupSelectedGenes()		{		System.out.println("groupSelectedGenes");	}
	//------------------------------------------------------
	
	@FXML private void doSearch()	
	{
		String text = searchBox.getText().trim();
		if (StringUtil.isEmpty(text)) 			return;
		
		String queryText = "", speciesComponent = "";
		
		if (StringUtil.hasText(text))
			queryText = "query=" + text;
		String selection = species.getSelectionModel().getSelectedItem();
		if ( !"Any".equals(selection))
			speciesComponent = "species=" + selection  + "&";
				
		String query = FIND_PATHWAYS_BASE;
		query += speciesComponent + queryText;
		System.out.println("SEARCH ACTION HERE");
//		findGenes(query);
	}
	
	public void openByReference(String ref) {
		for (Gene rec : geneListTable.getItems())
			if (ref.equals(rec.getId()))
			{
				System.out.println("openByReference " + ref);
			}
	}
   //---------------------------------------------------------------------------
}
