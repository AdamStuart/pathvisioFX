package diagrams.pViz.view;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import diagrams.pViz.app.Controller;
import diagrams.pViz.app.Selection;
import gui.Action.ActionType;
import icon.FontAwesomeIcons;
import icon.GlyphIcon;
import icon.GlyphsDude;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import model.AttributeMap;

public class Inspector extends HBox implements Initializable {
	Pasteboard pasteboard;
	Controller controller;
	Selection selection;
	
	@FXML private HBox inspectTop;
	@FXML private CheckBox resizable;
	@FXML private CheckBox movable;
	@FXML private CheckBox editable;
	@FXML private CheckBox connectable;

	@FXML private ColorPicker fillColor;
	@FXML private ColorPicker lineColor;
	@FXML private Label strokeLabel;
	@FXML private Label fillLabel;
	@FXML private Slider weight;
	@FXML private Slider scale;
	@FXML private Slider rotation;
	@FXML private Slider opacity;
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
		
		fillColor.setOnAction(evt -> fillChanged(true));
		lineColor.addEventHandler(ActionEvent.ACTION, evt -> strokeChanged(true));

		scale.valueProperty().addListener((ov, old, val) ->   {  	scaleChanged(true);        });	
		weight.valueProperty().addListener((ov, old, val) ->    {   weightChanged(false);   });	
		rotation.valueProperty().addListener((ov, old, val) ->  {   rotationChanged(false);  });	
		opacity.valueProperty().addListener((ov, old, val) ->  {   opacityChanged(false);  });	
		
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
	// **-------------------------------------------------------------------------------
	public void setParentController(Controller c)	
	{
		controller = c;
		selection = controller.getSelectionManager();
		pasteboard = controller.getPasteboard();
	}
	
	// **------------------------------------------------------------------------------

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
	private void fillChanged(boolean undoable)							
	{ 	 			
		if (undoable) 
			controller.getUndoStack().push(ActionType.Property); 
		selection.applyStyle(getStyleSettings(fillColor));	
		selection.putColor("Color", fillColor.getValue());	
	}
	
	private void weightChanged(boolean undoable)							
	{ 	 			
		if (undoable) 
			controller.getUndoStack().push(ActionType.Property); 
		selection.applyStyle(getStyleSettings(weight));	
		selection.putDouble("LineThickness", weight.getValue());	
	}
	
	
	private void scaleChanged(boolean undoable)							
	{ 	 			
		if (undoable) 
			controller.getUndoStack().push(ActionType.Property); 
		selection.applyStyle(getStyleSettings(weight));	
		selection.putDouble("Scale", weight.getValue());	
	}
	
	private void strokeChanged(boolean undoable)							
	{ 	 			
		if (undoable) 
			controller.getUndoStack().push(ActionType.Property); 
		selection.applyStyle(getStyleSettings(lineColor));	
		selection.putColor("LineColor", lineColor.getValue());	

	}
	
	private void opacityChanged(boolean undoable)							
	{ 	 			
//		if (undoable) 
//			controller.getUndoStack().push(ActionType.Opacity); 
//		selection.applyStyle(getStyleSettings(opacity));	
//		selection.putDouble("Opacity", opacity.getValue() / 100.);	
	}
	
	private void rotationChanged(boolean undoable)							
	{ 	 			
		if (undoable) 
			controller.getUndoStack().push(ActionType.Rotate); 
		selection.applyStyle(getStyleSettings(rotation));	
		selection.putDouble("Rotation", rotation.getValue() / 100.);	
	}
	
	// **-------------------------------------------------------------------------------
	public void applyLocks()
	{
		selection.applyLocks(movable.isSelected(), resizable.isSelected(), editable.isSelected(), connectable.isSelected());
	}
	
	@FXML private void setMovable(ActionEvent ev)	
	{	
		selection.setMovable(movable.isSelected());
	}	
	
	@FXML private void setResizable(ActionEvent ev)	
	{
		selection.setResizable(resizable.isSelected());		
	}
	
	@FXML private void setEditable(ActionEvent ev)	
	{	
		selection.setEditable(editable.isSelected());	
	}

	@FXML private void setConnectable(ActionEvent ev)	
	{	
		selection.setEditable(connectable.isSelected());	
	}

	// **-------------------------------------------------------------------------------
	public void syncInspector()		//Inspector
	{
		if(selection.count() == 1)
		{
			
			VNode firstNode = selection.first();
			
			resizable.setSelected(firstNode.canResize());
			movable.setSelected(firstNode.isMovable());
			editable.setSelected(firstNode.isEditable());
			connectable.setSelected(firstNode.isConnectable());

			Shape n = firstNode.getFigure();
			if  (n != null)
			{
				Paint fill = n.getFill();
				Paint stroke = n.getStroke();
				double wt = n.getStrokeWidth();
				double opac = n.getOpacity();
				double rot = n.getRotate();
				
				fillColor.setValue((Color) fill);
				lineColor.setValue((Color) stroke);
				weight.setValue(wt);
				opacity.setValue(100 * opac);
				rotation.setValue(rot);
			}
			if (firstNode instanceof StackPane)
			{
				StackPane stack = (StackPane) firstNode;
				String style = stack.getStyle();
				AttributeMap attr = new AttributeMap(style, true);
				Color fill = attr.getColor("-fx-background-color");
				Color stroke = attr.getColor("-fx-border-color");
				double opac = attr.getDouble("-fx-opacity", 0.5);
				double wt = attr.getDouble("-fx-border-width", 12);
				double rot = attr.getDouble("-fx-rotate", 0);			
				fillColor.setValue(fill);
				lineColor.setValue(stroke);
				weight.setValue(wt);
				rotation.setValue(rot);
				opacity.setValue(100 * opac);
			}
			
		}
	}
		public void start() {
// unused		
	}
		

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
