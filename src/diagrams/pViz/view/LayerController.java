package diagrams.pViz.view;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import diagrams.pViz.app.Controller;
import diagrams.pViz.app.IController;
import diagrams.pViz.app.Tool;
import diagrams.pViz.tables.DraggableTableRow;
import icon.FontAwesomeIcons;
import icon.GlyphIcon;
import icon.GlyphIcons;
import icon.GlyphsDude;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.stat.Unit;
import table.binder.Rect;
import table.binder.tablecellHelpers.BadgeTableCell;
import table.binder.tablecellHelpers.ChoiceBoxTableCell;
import table.binder.tablecellHelpers.NumberColConverter;
import util.StringUtil;

public class LayerController implements IController, Initializable {

	@FXML private TableView<LayerRecord> layersTable;
	@FXML private TableColumn<LayerRecord, Boolean> layerVisColumn;
	@FXML private TableColumn<LayerRecord, Boolean> layerLockColumn;
	@FXML private TableColumn<LayerRecord, String> layerNameColumn;
	@FXML private TableColumn<LayerRecord, Integer> layerCountColumn;
	@FXML private Button addLayer;
	@FXML private Button removeLayer;
	
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		setGraphic(addLayer, FontAwesomeIcons.PLUS_CIRCLE);
		setGraphic(removeLayer, FontAwesomeIcons.MINUS_CIRCLE);
		layersTable.getItems().addListener(resetLayerOrder);
	}
	ListChangeListener<LayerRecord> resetLayerOrder = new ListChangeListener<LayerRecord>()
	{
		@Override public void onChanged(Change<? extends LayerRecord> c)	
		{ 
//			System.out.println("resetLayerOrder");
			parentController.getPasteboard().resetLayerOrder(layersTable.getItems());
		}
	};

	static public void setGraphic(Button b, GlyphIcons i)
	{
		b.setGraphic(GlyphsDude.createIcon(i, GlyphIcon.DEFAULT_ICON_SIZE));
		b.setText("");
	}

	@Override
	public void getInfo(DataFormat fmt, String a, String colname, MouseEvent ev) {
		//System.out.println("getInfo");
		int idx = StringUtil.toInteger(a);
		try
		{
			Pasteboard pasteboard = parentController.getPasteboard();
			LayerRecord rec = layersTable.getItems().get(idx);
			if (rec != null)
			{
				if ("layerVisColumn".equals(colname))
				{	
					boolean vis = rec.toggleVisible();	
					refresh();
					if ("Background".equals(rec.getName()))
						pasteboard.getBackgroundLayer().setVisible(vis);
					else if ("Grid".equals(rec.getName()))
						pasteboard.getGridLayer().setVisible(vis);
					else 
						pasteboard.resetLayerVisibility(rec.getName(), vis);
				}
				if ("layerLockColumn".equals(colname))
				{	
					boolean lock = rec.toggleLock();		
						pasteboard.resetLayerLock(rec.getName(), lock);
					refresh();
				}
				if ("layerNameColumn".equals(colname))
				{	
					layersTable.edit(idx,  layerNameColumn);
				}
			}
		}
		catch (IndexOutOfBoundsException ex) 
		{
			// how could a bad index get here?
		}
		
	}
	// HACK to refresh table
	//http://stackoverflow.com/questions/11272395/javafx-2-1-2-2-tableview-update-issue
	public void refresh()
	{
		TableColumn<?,?> frst = layersTable.getColumns().get(0);
		if (frst != null)
		{
			frst.setVisible(false);		frst.setVisible(true);
		}
	}
	public Scene getScene()				{ 		return layersTable.getScene();	}
	private Controller parentController;
	public void setParentController(Controller c)	
	{ 
		parentController = c;	
		setupLayerTable(parentController.getPasteboard());
	}

	public static final DataFormat LAYER_MIME_TYPE = new DataFormat("application/x-java-serialized-layer");
	private void setupLayerTable(Pasteboard pasteboard)
	{
		assert(pasteboard != null);
		System.out.println("setupLayerTable");

//		Layer backgd = new Layer("Background");
//		Layer grd = new Layer("Grid");
//		Layer cntnt = new Layer("Content");
//		LayerRecord backgroundLayer = new LayerRecord(backgd, true, true,0);
//		LayerRecord gridLayer = new LayerRecord(grd, true, true, 0);
//		LayerRecord contentLayer = new LayerRecord(cntnt, true, false, 0);
//		pasteboard.getChildren().addAll(backgd, grd, cntnt);
	
		layersTable.setEditable(true);
		layersTable.getItems().addAll(pasteboard.getBackgroundLayerRecord(), 
				pasteboard.getGridLayerRecord(), 
				pasteboard.getContentLayerRecord());

		layersTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		layerVisColumn.setCellValueFactory(new PropertyValueFactory<LayerRecord, Boolean>("vis"));
		layerLockColumn.setCellValueFactory(new PropertyValueFactory<LayerRecord, Boolean>("lock"));
		layerNameColumn.setCellValueFactory(new PropertyValueFactory<LayerRecord, String>("name"));
		layerCountColumn.setCellValueFactory(new PropertyValueFactory<LayerRecord, Integer>("count"));
		layerNameColumn.setCellFactory(TextFieldTableCell.<LayerRecord>forTableColumn());

		// date picker
		layerVisColumn.setCellFactory(p -> { return new BadgeTableCell<LayerRecord>(FontAwesomeIcons.EYE);     });
		layerLockColumn.setCellFactory(p -> { return new BadgeTableCell<LayerRecord>(FontAwesomeIcons.LOCK);     });

		layerNameColumn.setOnEditStart(event -> 
		{
//			System.out.println("setOnEditStart");
//			event.getRowValue().setName(event.getNewValue());
		});
		layerNameColumn.setOnEditCommit(event -> event.getRowValue().setName(event.getNewValue()));
		layerNameColumn.setEditable(true);
		
		layersTable.setRowFactory((a) -> {
		       return new DraggableTableRow<LayerRecord>(layersTable, LAYER_MIME_TYPE, this);
			    });
		
		boolean verbose = false;
	//
//			cell.addEventFilter(KeyEvent.KEY_TYPED, event ->
//			{	
//				if (verbose) System.out.println("KEY_TYPED: " + event.getCharacter());
//				if (!Character.isDigit(event.getCharacter().charAt(0))) event.consume();	});
//			
//			cell.addEventFilter(KeyEvent.KEY_PRESSED, event ->
//			{	
//				if (verbose) System.out.println("KEY_PRESSED: " + event.getCharacter());
//				if (!Character.isDigit(event.getCharacter().charAt(0))) event.consume();	});
//			
//			cell.addEventFilter(KeyEvent.KEY_RELEASED, event ->
//			{	
//				if (verbose) System.out.println("KEY_RELEASED: " + event.getCharacter());
//				if (!Character.isDigit(event.getCharacter().charAt(0))) event.consume();	});
//		
//			colVal.getStyleClass().add("numeric");
//			colVal.setOnEditCommit((CellEditEvent<Rect, Double> t) ->	{	getRect(t).setVal(prefix, t.getNewValue());		});
			
//			colUnits.setCellValueFactory(new PropertyValueFactory<>(prefix +"Units"));
//			colUnits.setCellFactory(col -> new ChoiceBoxTableCell(colUnits, units, true));
//			colUnits.setOnEditCommit((CellEditEvent<Rect, Unit> t) ->	{	getRectUnit(t).setUnits(prefix, t.getNewValue());});
		
	}

	public void addLayer() {
		LayerRecord newRec = new LayerRecord("New Layer");
		int rowNum = layersTable.getItems().size();
		layersTable.getItems().add(newRec);
		parentController.getPasteboard().addLayer(newRec);
		layersTable.edit(rowNum, layerNameColumn);
	}
	public List<LayerRecord> getLayers()	{	return layersTable.getItems();	}
	public LayerRecord getLayer(String name) {
		for (LayerRecord rec : layersTable.getItems())
			if (rec.getName().equals(name))
				return rec;
		return null;
	}
	public void removeLayer() {
		LayerRecord r = layersTable.getSelectionModel().getSelectedItem();
		if (r != null)
			layersTable.getItems().remove(r);
	}
	//----------------------------------------------------------
	// TODO share one Layer window among all drawings
	private Stage stage;
	public void setStage(Stage value) 	{		stage = value;	}
	public Stage getStage() 			{		return stage;	}

}
