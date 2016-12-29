package diagrams.pViz.view;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import diagrams.pViz.app.Controller;
import diagrams.pViz.app.IController;
import diagrams.pViz.tables.DraggableTableRow;
import icon.FontAwesomeIcons;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import table.binder.tablecellHelpers.BadgeTableCell;
import util.StringUtil;

public class LayerController implements IController, Initializable {

	@FXML private TableView<LayerRecord> layersTable;
	@FXML private TableColumn<LayerRecord, Boolean> layerVisColumn;
	@FXML private TableColumn<LayerRecord, Boolean> layerLockColumn;
	@FXML private TableColumn<LayerRecord, String> layerNameColumn;
	@FXML private TableColumn<LayerRecord, Integer> layerCountColumn;

	@Override public void initialize(URL location, ResourceBundle resources)
	{
		setupLayerTable();
	}
	
	@Override
	public void getInfo(DataFormat fmt, String a, String colname, MouseEvent ev) {
		//System.out.println("getInfo");
		int idx = StringUtil.toInteger(a);
		try
		{
			LayerRecord rec = layersTable.getItems().get(idx);
			if (rec != null)
			{
				if ("layerVisColumn".equals(colname))
				{	
					boolean vis = rec.toggleVisible();	
					refresh();
					if ("Background".equals(rec.getName()))
						parentController.getPasteboard().getBackgroundLayer().setVisible(vis);
					else if ("Grid".equals(rec.getName()))
						parentController.getPasteboard().getGridLayer().setVisible(vis);
					else if (parentController!= null)
						parentController.getPasteboard().resetLayerVisibility(rec.getName(), vis);
				}
				if ("layerLockColumn".equals(colname))
				{	
					boolean lock = rec.toggleLock();		
					if (parentController!= null)
						parentController.getPasteboard().resetLayerLock(rec.getName(), lock);
					refresh();
				}
				if ("layerNameColumn".equals(colname))
				{	
					//TODO edit column name
					
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
	public void setParentController(Controller c)	{ parentController = c;	}

	public static final DataFormat LAYER_MIME_TYPE = new DataFormat("application/x-java-serialized-layer");
	private void setupLayerTable()
	{
//		System.out.println("setupPathwayTable");
//		TableColumn[] allCols = { idColumn, urlColumn, nameColumn, speciesColumn, revisionColumn };
		layersTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		layerVisColumn.setCellValueFactory(new PropertyValueFactory<LayerRecord, Boolean>("vis"));
		layerLockColumn.setCellValueFactory(new PropertyValueFactory<LayerRecord, Boolean>("lock"));
		layerNameColumn.setCellValueFactory(new PropertyValueFactory<LayerRecord, String>("name"));
		layerCountColumn.setCellValueFactory(new PropertyValueFactory<LayerRecord, Integer>("count"));

		// date picker
		layerVisColumn.setCellFactory(p -> { return new BadgeTableCell<LayerRecord>(FontAwesomeIcons.EYE);     });
		layerLockColumn.setCellFactory(p -> { return new BadgeTableCell<LayerRecord>(FontAwesomeIcons.LOCK);     });
		layerNameColumn.setOnEditCommit(event -> event.getRowValue().setName(event.getNewValue()));
		layerNameColumn.setEditable(true);
		layersTable.setEditable(true);
		
		layersTable.setRowFactory((a) -> {
		       return new DraggableTableRow<LayerRecord>(layersTable, LAYER_MIME_TYPE, this);
			    });
		
		LayerRecord backgroundLayer = new LayerRecord(true, true, "Background", 0);
		LayerRecord gridLayer = new LayerRecord(true, true, "Grid", 0);
		LayerRecord contentLayer = new LayerRecord(true, false, "Content", 0);
		layersTable.getItems().addAll(backgroundLayer, gridLayer, contentLayer);
		
	}

	public void addLayer() {
		LayerRecord newRec = new LayerRecord(true, false, "New Layer", 0);
		layersTable.getItems().add(newRec);
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
	private Stage stage;
	public void setStage(Stage value) {		stage = value;	}
	public Stage getStage() {		return stage;	}

}
