package diagrams.pViz.tables;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import diagrams.pViz.app.App;
import diagrams.pViz.app.Controller;
import diagrams.pViz.app.Document;
import diagrams.pViz.model.Model;
import gui.DraggableTableRow;
import gui.DropUtil;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import model.bio.Gene;
import model.bio.GeneListRecord;
import util.FileUtil;
import util.StringUtil;

public class GeneListTable extends TableView<Gene> {
//	private static final DataFormat GENE_MIME_TYPE = GeneListController.GENE_MIME_TYPE;
	static TableRow<Gene> thisRow = null;
	Controller controller;

	public GeneListTable(Controller c) {
		controller = c;
		setRowFactory((a) -> {
		       return new DraggableTableRow<Gene>(this, GeneListController.GENE_MIME_TYPE, controller, controller.getModel().getGeneList());
			    });
		setupGeneListTable();
		VBox.setVgrow(this, Priority.ALWAYS);
	}
	private TableColumn<Gene, String> idColumn = new TableColumn<Gene, String>("ID");
	private TableColumn<Gene, String> typeColumn = new TableColumn<Gene, String>("Type");
	private TableColumn<Gene, String> nameColumn = new TableColumn<Gene, String>("Name");
	private TableColumn<Gene, String> urlColumn = new TableColumn<Gene, String>("URL");
	private TableColumn<Gene, String> speciesColumn = new TableColumn<Gene, String>("Species");
	private TableColumn<Gene, String> dbColumn = new TableColumn<Gene, String>("Database");
	private TableColumn<Gene, String> dbidColumn = new TableColumn<Gene, String>("DatabaseId");
	private TableColumn<Gene, String> termsColumn = new TableColumn<Gene, String>("Terms");
	
	private Button draggable;
	
		
	private void setupGeneListTable()
	{
//		System.out.println("setupPathwayTable");
		getColumns().addAll(nameColumn, idColumn, termsColumn, typeColumn, urlColumn, speciesColumn);
		idColumn.setPrefWidth(200);
		typeColumn.setPrefWidth(28);
		nameColumn.setPrefWidth(100);
		speciesColumn.setMaxWidth(100);
		idColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("ensembl"));
		typeColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("type"));
		urlColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("url"));
		nameColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("name"));
		speciesColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("species"));
		dbColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("datbase"));
		dbidColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("dbid"));
		termsColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("termSummary"));

		speciesColumn.setVisible(false);
		urlColumn.setVisible(false);
		DropUtil.makeDropPane(this, e -> { doDrag(e); });
		draggable = new Button();
		draggable.setOnDragDetected(ev -> dragStart(ev));
//		draggable.setOnMouseDragged(ev -> doDrag(ev));
//		draggable.setOnMouseReleased(ev -> dragReleased(ev));
//		getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//		addEventHandler(KeyEvent.KEY_RELEASED, new KeyHandler());
		
	}
	String delimiter = "\t";
	public void doDrag(DragEvent e)
	{
		Dragboard db = e.getDragboard();
		Set<DataFormat> formats = db.getContentTypes();
		formats.forEach(a -> System.out.println("getContentTypes " + a.toString()));
		 for (File f : db.getFiles())
		 {
			 GeneListRecord genes = Document.readTabularText(f, controller.getSpecies());
			 if (genes != null)
				 addGeneList(genes);
		 }
		 e.consume();
	}
	void addColumn(String colName)
	{
		
	}
	public TableColumn findColumn(String fld)
	{
		for (TableColumn col : getColumns())
			if (col.getText().equals(fld))
				return col;
		return null;
	}
	static boolean ENSEMBL_REQD = false;
	public void populateTable(Model m, List<Gene> genes)
	{
		m.fillIdlist();
		if (genes != null) 
		{
			getItems().clear();
			for (Gene g : genes)
			{
				if (!ENSEMBL_REQD || StringUtil.hasText(g.getEnsembl()))
					getItems().add(g);
			}
		}
		getSortOrder().add(nameColumn);
		nameColumn.setSortType(SortType.ASCENDING);
		nameColumn.setSortable(true); // This performs a sort
//		setBorder(Borders.redBorder);
	}

	private static double yStart;	// where the mouse went down
	private static double yRef;		// the coordinates we watch for the next swap
	private static double yBase;		// top left of hit row		
	private static double yMin, yMax;		// top and bottom allowable y values	
	private static double yOffset = -1;	// difference between mouse location and nodes's top-left

	private void dragStart(MouseEvent ev)
	{
//		imgView.setX(0);
		double ystart = yBase; //  getTranslateY();
//		imgView.setY(ystart);
		Dragboard db = draggable.startDragAndDrop(TransferMode.MOVE);
		InputStream input = App.class.getResourceAsStream("drag.png");
		if (input != null)
		{
			Image IMAGE = new Image(input);
			db.setDragView(IMAGE );
		}
		ClipboardContent content = new ClipboardContent();				/* Put a string on a dragboard */
		content.put(DataFormat.PLAIN_TEXT, "GENE " + draggable.toString());
		db.setContent(content);
//		draggable.setVisible(false);
		ev.consume();
		System.out.println("startGeneDrag");
	}

	
	private void addGeneList(GeneListRecord genes) {
		 if (genes == null) return;
		controller.getModel().add(genes);
		
	}

	private void dragReleased(DragEvent e)
	{
		if (yOffset < 0) return;
		yRef = yStart = e.getSceneY(); //  - getTranslateY();
		double dy = e.getSceneY() - yStart;
////		if (imgView != null && e.getSceneY() > yMin  && e.getSceneY() < yMax)  //  )
//			imgView.setTranslateY(dy);
//		draggable.setVisible(true);
	}
	
	
	private void getInfo(int idx) {
		Gene gene = getItems().get(idx);
		gene.getInfo();					//TODO
		
		
//		String url = rec.getUrl();
//		String result = StringUtil.callURL(url, false);
//		App app = App.getInstance();
//		if (app != null)
//			getGeneList(result);
//			app.doNew(result);
		
	}
	private void removeSelectedRows()
	{
	// SAVE FOR UNDO
		List<Gene> items = getItems();
		int sz = items.size();
		for (int i = sz-1; i>= 0; i--)
		{	
			if (getSelectionModel().isSelected(i))
			items.remove(i);
		}
	}
	
}
