package diagrams.pViz.tables;

import java.io.InputStream;
import java.util.List;

import diagrams.pViz.app.App;
import diagrams.pViz.app.Controller;
import gui.DropUtil;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.bio.Gene;
import model.bio.GeneList;
import util.StringUtil;

public class GeneListTable extends TableView<Gene> {
	private static final DataFormat GENE_MIME_TYPE = new DataFormat("application/x-java-serialized-gene");
	static TableRow<Gene> thisRow = null;
	Controller controller;

	public GeneListTable(Controller c) {
		controller = c;
		setRowFactory(tv -> {
			TableRow<Gene> row = new TableRow<>();

			row.setOnDragDetected(event -> {
				if (!row.isEmpty()) {
					Integer index = row.getIndex();
					Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
					db.setDragView(row.snapshot(null, null));
					ClipboardContent cc = new ClipboardContent();
					cc.put(GENE_MIME_TYPE, index);
					db.setContent(cc);
					event.consume();
					thisRow = row;
				}
			});

			row.setOnDragEntered(event -> {
				Dragboard db = event.getDragboard();
				if (db.hasContent(GENE_MIME_TYPE)) {
					if (row.getIndex() != ((Integer) db.getContent(GENE_MIME_TYPE)).intValue()) {
						event.acceptTransferModes(TransferMode.MOVE);
						event.consume();
						thisRow = row;
						// if (thisRow != null)
						// thisRow.setOpacity(0.3);
					}
				}
			});

			row.setOnDragExited(event -> {
				if (event.getGestureSource() != thisRow && event.getDragboard().hasString()) {
					// if (thisRow != null)
					// thisRow.setOpacity(1);
					thisRow = null;
				}
			});

			row.setOnDragOver(event -> {
				Dragboard db = event.getDragboard();
				if (db.hasContent(GENE_MIME_TYPE)) {
					if (row.getIndex() != ((Integer) db.getContent(GENE_MIME_TYPE)).intValue()) {
						event.acceptTransferModes(TransferMode.MOVE);
						event.consume();
					}
				}
			});

			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2) {
					int idx = row.getIndex();
					getInfo(idx);
					event.consume();
				}
			});

			row.setOnDragDropped(event -> {
				Dragboard db = event.getDragboard();
				if (db.hasContent(GENE_MIME_TYPE)) {
					getSelectionModel().clearSelection();
					int draggedIndex = (Integer) db.getContent(GENE_MIME_TYPE);
					Gene draggedNode = getItems().remove(draggedIndex);

					int dropIndex = (row.isEmpty()) ? getItems().size() : row.getIndex();
					getItems().add(dropIndex, draggedNode);
					getSelectionModel().select(dropIndex);

					event.setDropCompleted(true);
					getSelectionModel().select(dropIndex);
					event.consume();
					// if (thisRow != null)
					// thisRow.setOpacity(1);
					thisRow = null;
				}
			});

			return row;
		});
		setupGeneListTable();
		VBox.setVgrow(this, Priority.ALWAYS);
	}
	private TableColumn<Gene, String> idColumn = new TableColumn<Gene, String>("ID");
	private TableColumn<Gene, String> nameColumn = new TableColumn<Gene, String>("Name");
	private TableColumn<Gene, String> urlColumn = new TableColumn<Gene, String>("URL");
	private TableColumn<Gene, String> speciesColumn = new TableColumn<Gene, String>("Species");
	private TableColumn<Gene, String> dbColumn = new TableColumn<Gene, String>("Database");
	private TableColumn<Gene, String> dbidColumn = new TableColumn<Gene, String>("DatabaseId");
	private Button draggable;
	
		
	private void setupGeneListTable()
	{
//		System.out.println("setupPathwayTable");
		getColumns().addAll(nameColumn, idColumn, urlColumn, speciesColumn);
		idColumn.setPrefWidth(200);
		nameColumn.setPrefWidth(130);
		speciesColumn.setMaxWidth(100);
		idColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("ensembl"));
		urlColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("url"));
		nameColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("name"));
		speciesColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("species"));
		dbColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("datbase"));
		dbidColumn.setCellValueFactory(new PropertyValueFactory<Gene, String>("dbid"));

		speciesColumn.setVisible(false);
		urlColumn.setVisible(false);
		DropUtil.makeDropPane(this, e -> { doDrag(e); });
		draggable = new Button();
		draggable.setOnDragDetected(ev -> dragStart(ev));
//		draggable.setOnMouseDragged(ev -> doDrag(ev));
//		draggable.setOnMouseReleased(ev -> dragReleased(ev));
		getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		addEventHandler(KeyEvent.KEY_RELEASED, new KeyHandler());
		
	}
	private final class KeyHandler implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent event) {

 			KeyCode key = event.getCode();
 			
// 			if (key.isArrowKey())	getSelectionModel().translate(key);
//			else 
				if (KeyCode.DELETE.equals(key)) 	removeSelectedRows();		// create an undoable action
			else if (KeyCode.BACK_SPACE.equals(key)) removeSelectedRows();
//			
// 			else if (KeyCode.R.equals(key)) 	setTool(Tool.Rectangle);
//			else if (KeyCode.C.equals(key)) 	setTool(Tool.Circle);
//			else if (KeyCode.P.equals(key)) 	setTool(Tool.Polygon);
//			else if (KeyCode.L.equals(key)) 	setTool(Tool.Line);
//			else if (KeyCode.W.equals(key)) 	setTool(Tool.Polyline);
//			
////			else if (KeyCode.X.equals(key)) 	setTool(Tool.Xhair);
//			else if (KeyCode.ESCAPE.equals(key)) {		terminatePoly();  	removeDragLine(); }
//			else if (KeyCode.SPACE.equals(key)) {		terminatePoly();	removeDragLine(); }
		}
	}
	static boolean ENSEMBL_REQD = false;
	public void populateTable(GeneList genes)
	{
		genes.fillIdlist();
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

	private void doDrag(DragEvent e)
	{
//		Dragboard db = e.getDragboard();
//		Set<DataFormat> formats = db.getContentTypes();
//		formats.forEach(a -> System.out.println("getContentTypes " + a.toString()));
//		e.consume();
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
