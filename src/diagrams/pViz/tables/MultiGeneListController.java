package diagrams.pViz.tables;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import animation.BorderPaneAnimator;
import diagrams.pViz.app.Controller;
import diagrams.pViz.app.Document;
import diagrams.pViz.app.GeneListRecord;
import diagrams.pViz.gpml.GPML;
import gui.DropUtil;
import icon.FontAwesomeIcons;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import model.bio.Gene;
import util.FileUtil;


public class MultiGeneListController  implements Initializable  {
	
//	public static final DataFormat COLUMN_MIME_TYPE = new DataFormat("application/x-java-serialized-column");
//	public static final DataFormat GENE_MIME_TYPE = new DataFormat("application/x-java-serialized-gene");
	@FXML private BorderPane borderpane;
	@FXML private ScrollPane scrollpane;
	@FXML private SplitPane splitpane;

	@FXML private TableView<Gene> firstTable;
	@FXML private TableColumn<Gene, String> flagColumn1;
	@FXML private TableColumn<Gene, String> geneNameColumn1;

	@FXML private TableView<Gene> secondTable;
	@FXML private TableColumn<Gene, String> flagColumn2;
	@FXML private TableColumn<Gene, String> geneNameColumn2;

	@FXML private TableView<Gene> thirdTable;
	@FXML private TableColumn<Gene, String> flagColumn3;
	@FXML private TableColumn<Gene, String> geneNameColumn3;

	@FXML private VBox west;
	@FXML private MenuBar menubar;
	@FXML private Button westSidebar;
//	@FXML private MenuItem info;
//	@FXML private MenuItem addColumn;
//	@FXML private MenuItem chart;
//	@FXML private MenuItem groupSelectedGenes;
//	@FXML private MenuItem browsePathways;
	//------------------------------------------------------

	public static String tooltip = "GENELIST TOOLTIP";
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		Controller.setGraphic(westSidebar, FontAwesomeIcons.ARROW_CIRCLE_O_RIGHT);
		new BorderPaneAnimator(borderpane, westSidebar, Side.LEFT, false, 120);
		westSidebar.fire();		// start with columns hidden
		//-------
		//-------
		flagColumn1.setCellValueFactory(new PropertyValueFactory<Gene, String>("flag"));
		geneNameColumn1.setCellValueFactory(new PropertyValueFactory<Gene, String>("name"));
		flagColumn2.setCellValueFactory(new PropertyValueFactory<Gene, String>("flag"));
		geneNameColumn2.setCellValueFactory(new PropertyValueFactory<Gene, String>("name"));
		flagColumn3.setCellValueFactory(new PropertyValueFactory<Gene, String>("flag"));
		geneNameColumn3.setCellValueFactory(new PropertyValueFactory<Gene, String>("name"));

		firstTable.setRowFactory((a) -> {
		       return new DraggableTableRow<Gene>(firstTable, GeneListController.GENE_MIME_TYPE, parentController);
			    });
		secondTable.setRowFactory((a) -> {
		       return new DraggableTableRow<Gene>(secondTable, GeneListController.GENE_MIME_TYPE, parentController);
			    });
		thirdTable.setRowFactory((a) -> {
		       return new DraggableTableRow<Gene>(thirdTable, GeneListController.GENE_MIME_TYPE, parentController);
			    });
		
		DropUtil.makeDropPane(firstTable, e -> { handleDropEvent(e);}	);
		DropUtil.makeDropPane(secondTable, e -> { handleDropEvent(e);}	);
		DropUtil.makeDropPane(thirdTable, e -> { handleDropEvent(e);}	);
	}

	private void handleDropEvent(DragEvent e) 
	{
			System.out.println("handleDropEvent");
			Dragboard db = e.getDragboard();
			if (db.hasFiles())
			{
				TableView targetTable = null;
				EventTarget targ = e.getTarget();
				if (targ instanceof Node)
				{
					if (targ instanceof TableView)
						targetTable= (TableView)targ;
					else
					{
					Parent parent = ((Node)targ).getParent();
					if (parent instanceof TableView)
						targetTable = (TableView)parent;
					System.out.println((Node) targ);
				}
				if (targetTable == null)
				{
					System.err.println("No Target Table");
					return;
				}
				List<File> files = db.getFiles();
				for (File file : files)
				{
					GeneListRecord rec = null;
					if (FileUtil.isCDT(file))
						rec = Document.readCDT(file, parentController.getSpecies());
					else if (FileUtil.isGPML(file))
						rec = GPML.readGeneList(file, parentController.getSpecies());

					if (rec != null) targetTable.getItems().addAll(rec.getGeneList());

				}
			}
		}
	}

	//------------------------------------------------------
	private Controller parentController;
	public void setParentController(Controller inParent) {		parentController = inParent;	}

	private GPML gpmlReader;
	public static String FIND_PATHWAYS_BASE = "http://webservice.wikipathways.org/findPathwaysByText?";

	@FXML private void doUnion()	
	{
		System.out.println("doAddColumn");
	}

	@FXML private void doIntersection()	
	{
		System.out.println("doAddColumn");
	}

	@FXML private void doDifference()	
	{
		System.out.println("doAddColumn");
	}

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
}
