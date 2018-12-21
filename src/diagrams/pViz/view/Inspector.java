package diagrams.pViz.view;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import diagrams.pViz.app.Controller;
import diagrams.pViz.app.Selection;
import diagrams.pViz.model.nodes.DataNode;
import gui.Action.ActionType;
import gui.Borders;
import icon.FontAwesomeIcons;
import icon.GlyphIcon;
import icon.GlyphsDude;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import model.AttributeMap;

public class Inspector extends HBox implements Initializable {
	Pasteboard pasteboard;
	Controller controller;
	Selection selection;
	
	@FXML private HBox inspectTop;	
	@FXML private CheckBox resizable;
	@FXML private CheckBox movable;
	@FXML private CheckBox selectable;
	@FXML private CheckBox connectable;

	// CV stands for Container VBox - it has a label and slider in it
	@FXML private VBox fillCV;
	@FXML private VBox opacCV;
	@FXML private VBox strokeCV;
	@FXML private VBox rotatCV;
	@FXML private VBox weightCV;
	@FXML private VBox scaleCV;

	@FXML private ColorPicker fillColor;
	@FXML private ColorPicker lineColor;
	@FXML private Label strokeLabel;
	@FXML private Label fillLabel;
	@FXML private Slider weight;
	@FXML private Slider scale;
	@FXML private Slider rotation;
	@FXML private Slider opacity;
	@FXML private Slider fillSlider;
	@FXML private Slider strokeSlider;
//	@FXML private Label status1;
//	@FXML private Label status2;
//	@FXML private Label status3;
//	@FXML private Button addKeyFrame;
//	@FXML private Button removeKeyFrame;
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		for (Node n : getChildren())
			System.out.println(n.toString());
		Parent p = strokeLabel.getParent();
		strokeLabel.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.PENCIL, GlyphIcon.DEFAULT_ICON_SIZE));
		fillLabel.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.CAR, GlyphIcon.DEFAULT_ICON_SIZE));
		fillLabel.setText("");		fillLabel.setMinWidth(30);		fillLabel.setAlignment(Pos.BASELINE_CENTER);
		strokeLabel.setText(""); 	strokeLabel.setMinWidth(30);	strokeLabel.setAlignment(Pos.BASELINE_CENTER);
		
		fillColor.setOnAction(evt -> fillChanged());
		lineColor.addEventHandler(ActionEvent.ACTION, evt -> strokeChanged());

		scale.valueProperty().addListener((ov, old, val) ->   	{  	scaleChanged();   	});	
		weight.valueProperty().addListener((ov, old, val) ->    {   weightChanged();   	});	
		rotation.valueProperty().addListener((ov, old, val) ->  {   rotationChanged();  });	
		opacity.valueProperty().addListener((ov, old, val) ->  	{   opacityChanged();  	});	
		fillSlider.valueProperty().addListener((ov, old, val) ->  	{   fillChanged();  	});	
		strokeSlider.valueProperty().addListener((ov, old, val) ->  	{   strokeChanged();  	});	
		
		fillCV.setBorder(Borders.thinEtchedBorder);
		fillCV.setOnMouseClicked(e -> { if (e.getClickCount() == 2) showSettings(fillCV); }); 
		opacCV.setBorder(Borders.thinEtchedBorder);
		opacCV.setOnMouseClicked(e -> { if (e.getClickCount() == 2) showSettings(opacCV); }); 
		rotatCV.setBorder(Borders.thinEtchedBorder);
		rotatCV.setOnMouseClicked(e -> { if (e.getClickCount() == 2) showSettings(rotatCV); }); 
		scaleCV.setBorder(Borders.thinEtchedBorder);
		scaleCV.setOnMouseClicked(e -> { if (e.getClickCount() == 2) showSettings(scaleCV); }); 
		weightCV.setBorder(Borders.thinEtchedBorder);
		weightCV.setOnMouseClicked(e -> { if (e.getClickCount() == 2) showSettings(weightCV); }); 
		strokeCV.setBorder(Borders.thinEtchedBorder);
		strokeCV.setOnMouseClicked(e -> { if (e.getClickCount() == 2) showSettings(strokeCV); }); 
		// sliders don't record undoable events (because they make so many) so snapshot the state on mousePressed
		EventHandler<Event> evH = event -> {	controller.getUndoStack().push(ActionType.Property);  };
//		opacity.setOnMousePressed(evH); 
//		addKeyFrame.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.PLUS_CIRCLE, GlyphIcon.DEFAULT_ICON_SIZE));
//		addKeyFrame.setText(null);
//		removeKeyFrame.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.MINUS_CIRCLE, GlyphIcon.DEFAULT_ICON_SIZE));
//		removeKeyFrame.setText(null);

		weight.setOnMousePressed(evH);
		rotation.setOnMousePressed(evH);
		opacity.setOnMousePressed(evH);
		setMinHeight(150); setPrefHeight(150); setMinWidth(600); 
//		setupKeyFrameTable();
		getChildren().add (inspectTop);		//new Button("ANYTHING")
//		getChildren().addAll( fillLabel, fillColor, strokeLabel, lineColor);
//		getChildren().addAll( scale, weight, rotation);
//		setBorder(Borders.redBorder);
	}
	private void showSettings(VBox vbox) {
		   Alert a = new Alert(AlertType.INFORMATION, vbox.getId());
		   a.setHeaderText("Settings for Mapping Attributes to Visualzation");
		   a.getDialogPane().setMinWidth(600);
		   a.setResizable(true);
		   a.showAndWait();
	
	
}
	// **-------------------------------------------------------------------------------
	public void setParentController(Controller c)	
	{
		controller = c;
		selection = controller.getSelectionManager();
		pasteboard = controller.getPasteboard();
	}
	
	// **------------------------------------------------------------------------------
	@FXML private void fillClick(MouseEvent ev)
	{
		if (ev.getClickCount() > 1)
		{
			Dialog alert = new Dialog();
			DialogPane dpane = alert.getDialogPane();
			alert.setWidth(240);
			alert.setHeight(240);
			ChoiceBox<String> choices =  new ChoiceBox<String> ();
			List<String> numCols = controller.getTreeTableView().getNumericColumns();
			choices.getItems().addAll(numCols);
			choices.getSelectionModel().select(0);
			Label label = new Label("Map a column to a grayscale");
			VBox vbox = new VBox(label, choices );
			label.setMinWidth(200);
			dpane.setMinHeight(200);
			dpane.setMinWidth(240);
			choices.setMinHeight(30);
			choices.setMinWidth(200);
			vbox.setMinHeight(200);
			vbox.setPrefHeight(200);
			dpane.getChildren().add(vbox);
			
			dpane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK)
			{
				System.out.println("MAP " + choices.getSelectionModel().getSelectedItem());
			}
		}
	}
	// **------------------------------------------------------------------------------

	private String getStyleSettings(Control src)
	{
		Color fill = fillColor.getValue();
		Color stroke = lineColor.getValue();
		if (fill == null) fill = Color.GRAY;
		if (stroke == null) stroke = Color.DARKGRAY;
		String fillHex = "-fx-fill: #" + fill.toString().substring(2, 8) + ";\n";
		String strokeHex = "-fx-stroke: #" + stroke.toString().substring(2, 8) + ";\n";
		String wtStr = String.format("-fx-stroke-width: %.1f;\n", weight.getValue());
		String opacStr = String.format("-fx-opacity: %.2f;\n", opacity.getValue() / 100.);
		String rotStr = String.format("-fx-rotate: %.1f;\n", rotation.getValue());

		StringBuilder buff = new StringBuilder();
		buff.append(fillHex).append(strokeHex).append(wtStr).append(opacStr).append(rotStr);
		return buff.toString();
	}
	// **-------------------------------------------------------------------------------
	private void fillChanged()							
	{ 	 			
		if (controller.getUndoStack().peek() != ActionType.Property ) 
			controller.getUndoStack().push(ActionType.Property); 
		selection.applyStyle(getStyleSettings(fillColor));	
		selection.putColor("Color", fillColor.getValue());	
	}
	
	private void weightChanged()							
	{ 	 			
		if (controller.getUndoStack().peek() != ActionType.Property ) 
			controller.getUndoStack().push(ActionType.Property); 
		selection.applyStyle(getStyleSettings(weight));	
		selection.putDouble("LineThickness", weight.getValue());	
	}
	
	
	private void scaleChanged()							
	{ 	 			
		if (controller.getUndoStack().peek() != ActionType.Scale ) 
			controller.getUndoStack().push(ActionType.Scale); 
		selection.applyStyle(getStyleSettings(scale));	
		selection.putDouble("Scale", scale.getValue());	
		selection.resetScale(scale.getValue());	
	}
	
	private void strokeChanged()							
	{ 	 			
		if (controller.getUndoStack().peek() != ActionType.Property ) 
			controller.getUndoStack().push(ActionType.Property); 
		selection.applyStyle(getStyleSettings(lineColor));	
		selection.putColor("LineColor", lineColor.getValue());	

	}
	
	private void opacityChanged()							
	{ 	 			
		if (controller.getUndoStack().peek() != ActionType.Opacity ) 
			controller.getUndoStack().push(ActionType.Opacity); 
		selection.applyStyle(getStyleSettings(opacity));	
		selection.putDouble("Opacity", opacity.getValue() / 100.);	
	}
	
	private void rotationChanged()							
	{ 	 			
		if (controller.getUndoStack().peek() != ActionType.Rotate ) 
			controller.getUndoStack().push(ActionType.Rotate); 
		selection.applyStyle(getStyleSettings(rotation));	
		selection.putDouble("Rotation", rotation.getValue() / 100.);	
	}
	
	
	//-------------------------------------------------------------
	@FXML private CheckBox showGraphId;	
	@FXML private CheckBox snapToGrid;	
	@FXML private CheckBox showZOrder;	
	@FXML private CheckBox showAnchors;	
	@FXML private CheckBox showLocks;	
	@FXML private void	showZOrder() 	{		zOrderVisible.set(showZOrder.isSelected());	}
	@FXML private void	setSnapToGrid() {		snapToGridProperty.set(snapToGrid.isSelected());  }
	@FXML private void	showGraphId() 	{		graphIdsVisible.set(showGraphId.isSelected());	} 
	@FXML private void	showAnchors() 	{		anchorVisibleProperty.set(showAnchors.isSelected());	}
	@FXML private void	showLocks() 	{		lockVisibleProperty.set(showLocks.isSelected());	}

	SimpleBooleanProperty graphIdsVisible = new SimpleBooleanProperty(false);
	public SimpleBooleanProperty graphIdsVisibleProperty() { return graphIdsVisible; }
	SimpleBooleanProperty zOrderVisible = new SimpleBooleanProperty(false);
	public SimpleBooleanProperty zOrderVisibleProperty() { return zOrderVisible; }
	SimpleBooleanProperty snapToGridProperty = new SimpleBooleanProperty(false);
	public SimpleBooleanProperty snapToGridProperty() { return snapToGridProperty; }
	SimpleBooleanProperty anchorVisibleProperty = new SimpleBooleanProperty(false);
	public SimpleBooleanProperty anchorVisibleProperty() { return anchorVisibleProperty; }
	SimpleBooleanProperty lockVisibleProperty = new SimpleBooleanProperty(false);
	public SimpleBooleanProperty lockVisibleProperty() { return lockVisibleProperty; }
	// **-------------------------------------------------------------------------------
	public void applyLocks()
	{
		selection.applyLocks(movable.isSelected(), resizable.isSelected(), selectable.isSelected(), connectable.isSelected());
	}
	
	@FXML private void setMovable(ActionEvent ev)		{	 selection.setMovable(movable.isSelected());	}	
	
	@FXML private void setResizable(ActionEvent ev)	
	{
		selection.setResizable(resizable.isSelected());		
	}
	
	@FXML private void setSelectable(ActionEvent ev)	
	{	
		selection.setSelectable(selectable.isSelected());	
	}

	@FXML private void setConnectable(ActionEvent ev)	
	{	
		selection.setSelectable(connectable.isSelected());	
	}

	// **-------------------------------------------------------------------------------
	public void syncInspector()		//Inspector
	{
		if(selection.count() == 1)
		{
			
			VNode firstNode = selection.first();
			DataNode dataNode = firstNode.modelNode();
			resizable.setSelected(dataNode.isResizable());
			movable.setSelected(dataNode.isMovable());
			selectable.setSelected(dataNode.isSelectable());
			connectable.setSelected(dataNode.isConnectable());

			Shape n = firstNode.getFigure();
			boolean rectangular = (n instanceof Rectangle);
			if  (n != null)
			{
//				String shapeType = firstNode.modelNode().get("ShapeType");
				Paint fill = n.getFill();
				Paint stroke = n.getStroke();
				double wt = n.getStrokeWidth();
				double opac = n.getOpacity();
				double rot = n.getRotate();
				
				fillColor.setValue((Color) fill);
				opacity.setValue(100 * opac);
				rotation.setValue(rot);
				if (rectangular)
				{
					lineColor.setValue((Color) stroke);
					weight.setValue(wt);
				}
			}
			if (firstNode instanceof StackPane)
			{
				StackPane stack = (StackPane) firstNode;
				String style = stack.getStyle();
				AttributeMap attr = new AttributeMap(style, true);
				Color fill = attr.getColor("-fx-background-color");
				Color stroke = attr.getColor("-fx-border-color");
				double opac = attr.getDouble("-fx-opacity", 1.0);
				double wt = attr.getDouble("-fx-border-width", 12);
				double rot = attr.getDouble("-fx-rotate", 0);			
				fillColor.setValue(fill);
				rotation.setValue(rot);
				opacity.setValue(100 * opac);
				if (rectangular)
				{
					lineColor.setValue(stroke);
					weight.setValue(wt);
				}
			}
			
		}
	}
		public void start() {
// unused		
	}
		
// KeyFrame list is removed for now
//		@FXML private TableView<KeyFrameRecord> keyFramesTable;
//		@FXML private TableColumn<KeyFrameRecord, String> keyFrameNameColumn;
//		@FXML private TableColumn<KeyFrameRecord, Double> keyFrameHoldColumn;
//		@FXML private TableColumn<KeyFrameRecord, Double> keyFrameMoveColumn;
//
//		public static final DataFormat KEYFRAME_MIME_TYPE = new DataFormat("application/x-java-serialized-keyframe");
//		private void setupKeyFrameTable()
//		{
////			System.out.println("setupPathwayTable");
////			TableColumn[] allCols = { idColumn, urlColumn, nameColumn, speciesColumn, revisionColumn };
//			keyFramesTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
//			keyFrameNameColumn.setCellValueFactory(new PropertyValueFactory<KeyFrameRecord, String>("name"));
//			keyFrameHoldColumn.setCellValueFactory(new PropertyValueFactory<KeyFrameRecord, Double>("hold"));
//			keyFrameMoveColumn.setCellValueFactory(new PropertyValueFactory<KeyFrameRecord, Double>("move"));
//
//			//all columns are editable
//			keyFrameNameColumn.setOnEditCommit(event -> event.getRowValue().setName(event.getNewValue()));
//			keyFrameNameColumn.setEditable(true);
//			keyFrameHoldColumn.setOnEditCommit(event -> event.getRowValue().setHold(event.getNewValue()));
//			keyFrameHoldColumn.setEditable(true);
//			keyFrameMoveColumn.setOnEditCommit(event -> event.getRowValue().setMove(event.getNewValue()));
//			keyFrameMoveColumn.setEditable(true);
//			keyFramesTable.setEditable(true);
//			
////			keyFrameTable.setRowFactory((a) -> {
////			       return new DraggableTableRow<KeyFrameRecord>(keyFrameTable, LAYER_MIME_TYPE, this);
////				    });
//			
////			keyFramesTable.getItems().addAll();
//			
//		}
//
//		@FXML public void addKeyFrame() {
//			KeyFrameRecord newRec = new KeyFrameRecord(true, "New KeyFrame", 2, 1);
//			keyFramesTable.getItems().add(newRec);
//			newRec.setState(controller.getState());
//		}
//		public List<KeyFrameRecord> getLayers()	{	return keyFramesTable.getItems();	}
//		public KeyFrameRecord getLayer(String name) {
//			for (KeyFrameRecord rec : keyFramesTable.getItems())
//				if (rec.getName().equals(name))
//					return rec;
//			return null;
//		}
//		@FXML public void removeKeyFrame() {
//			KeyFrameRecord r = keyFramesTable.getSelectionModel().getSelectedItem();
//			if (r != null)
//				keyFramesTable.getItems().remove(r);
//		}
//
}
