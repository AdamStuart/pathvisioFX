package diagrams.pViz.tables;

import java.util.HashMap;
import java.util.Map;

import diagrams.pViz.app.Controller;
import diagrams.pViz.app.GPMLRecord;
import diagrams.pViz.model.Edge;
import diagrams.pViz.model.Interaction;
import diagrams.pViz.model.DataNode;
import diagrams.pViz.model.Model;
import diagrams.pViz.view.VNode;
import gui.Backgrounds;
import gui.Borders;
import gui.Fonts;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import model.AttributeMap;
import model.bio.BiopaxRecord;
import model.bio.MIM;

public class LegendRecord implements GPMLRecord {

	StringProperty id = new SimpleStringProperty();
	StringProperty type = new SimpleStringProperty();
	StringProperty name = new SimpleStringProperty();
	ObjectProperty image = new SimpleObjectProperty();

	public StringProperty  idProperty()  { return id;}
	public String getId()  { return id.get();}
	public void setId(String s)  { id.set(s);}

	public StringProperty  typeProperty()  { return type;}
	public String getType()  { return  type.get();		}
	public void setType(String s)  { type.set(s);}

	public StringProperty  nameProperty()  { return name;}
	public String getName()  { return name.get();}
	public void setName(String s)  { name.set(s);}

	public ObjectProperty  imageProperty()  { return image;}
	public Object getImage()  { return image.get();}
	public void setImage(Object s)  { image.set(s);}


	
	@Override
	public void getInfo(DataFormat mimetype, String a, String b) {

	}
	public static DataNode makeLegend(ObjectProperty resultProperty, Model model, Controller ctrlr) {
		String reslt = resultProperty.getValue().toString();
		String[] flds = reslt.split("\t");
		if (flds.length != 5) return null;
		String title = flds[0];
		String comment = flds[1];
		boolean addRefs = "TRUE".equals(flds[2]);
		boolean addNodeTypes = "TRUE".equals(flds[3]);
		boolean addEdgeTypes = "TRUE".equals(flds[4]);
		return makeLegend(title, comment, addNodeTypes, addEdgeTypes, addRefs, model, ctrlr, false);

	}
	public static DataNode makeLegend(String title, String comment, boolean addNodeTypes, boolean addEdgeTypes, boolean addRefs, Model model, Controller ctrlr, boolean smallVersion) {

		int lab1wid = 120;
		int lab2wid = 150;
		int lineHeight = 36;
		int reflineHeight = 24;
		int lineCt = 2;
		int SPACING = 8;
		boolean showLabel3 = !smallVersion;
		int lab3wid = (showLabel3) ? 150 : 0;
		int WIDTH = SPACING + SPACING + lab1wid + SPACING + lab2wid + SPACING + lab3wid;
		if (smallVersion)
			WIDTH -= 100;

		AttributeMap attrs = new AttributeMap();
		attrs.put("ShapeType", "Legend");
		attrs.put("Id", "Legend");
		attrs.put("X", "50");
		attrs.put("Y", "50");
		attrs.put("Width", "" + WIDTH);
		attrs.put("Height", "300");
		attrs.put("CenterX", "350");
		attrs.put("CenterY", "200");
		DataNode node = new DataNode(attrs, model);
		model.getDataNodeMap().put(node.getGraphId(), node);
		VNode stack = node.getStack();
		VBox box = new VBox();
		Label titleLabel = new Label(title);
		HBox titlebox = new HBox(titleLabel);
		titleLabel.setMinWidth(WIDTH); 
		titlebox.setMinWidth(WIDTH); 
		titleLabel.setFont(new Font("SansSerif", smallVersion ? 24 : 34));
		titleLabel.setAlignment(Pos.CENTER);
		Label commentLabel = new Label(comment);
		commentLabel.setMinWidth(WIDTH); 
		commentLabel.setAlignment(Pos.CENTER);
		commentLabel.setFont(new Font("SansSerif", 12));
//				HBox.setHgrow(titleLabel, Priority.ALWAYS);
		titlebox.setMinWidth(WIDTH);
		box.getChildren().add(titlebox);
		box.getChildren().add(commentLabel);
		Line separatorLine = new Line(0,20, WIDTH, 20);
		box.getChildren().add(separatorLine);
	
		box.setBorder(Borders.greenBorder);
		stack.getChildren().add(box);
		box.setBackground(Backgrounds.white);
		ctrlr.getPasteboard().add(stack);
		
		if (addNodeTypes)
		{
			Map<String, DataNode> nodeTypeMap = new HashMap<String, DataNode>();
			for (DataNode eachnode : model.getDataNodeMap().values())
			{
				String type = eachnode.getType();
				DataNode extant = nodeTypeMap.get(type);
				if (extant == null)
					nodeTypeMap.put(type,  eachnode);
			}
			for (String key : nodeTypeMap.keySet())
			{
				if (key == null) continue;
				DataNode legendEntry = nodeTypeMap.get(key);
				LegendRecord rec = new LegendRecord();
				rec.setType(key);
				rec.setName(legendEntry.getInfoStr());
				
				Label spacer = new Label();	spacer.setMinWidth(SPACING); spacer.prefWidth(SPACING);
				Label label1 = new Label();
				double width =legendEntry.getStack().getWidth();
				double height =legendEntry.getStack().getHeight();
				WritableImage img = new WritableImage((int) width, (int) height);
//				SnapshotParameters params = new SnapshotParameters();
//				params.setTransform(new Translate(90, 20));
				legendEntry.getStack().snapshot(null, img);
				
//				Alert alert = new Alert(AlertType.INFORMATION);
//				alert.setGraphic(new ImageView(img));
//				alert.showAndWait();		 
//				
				label1.setGraphic(new ImageView(img));
			
				Label label2 = new Label(key);
				Label label3 = new Label(legendEntry.getLabel());
				label1.setMinWidth(lab1wid); label1.prefWidth(lab1wid);	label1.setMinHeight(lineHeight);
				label2.setMinWidth(lab2wid); label2.prefWidth(lab1wid);	label2.setMinHeight(lineHeight);
				label3.setMinWidth(lab3wid); label3.prefWidth(lab1wid);	label3.setMinHeight(lineHeight);
				HBox hbox = new HBox(SPACING, spacer, label1, label2);
				if (showLabel3) 
					hbox.getChildren().add(label3);
				box.getChildren().add(hbox);
				lineCt++;
			}
		}
		if (addEdgeTypes)
		{
			separatorLine = new Line(0,20, WIDTH, 20);
			box.getChildren().add(separatorLine);
			Map<MIM, Edge> edgeTypeMap = new HashMap<MIM, Edge>();
			for (Interaction edge : model.getEdges())
			{
				MIM type = edge.getInteractionType();
				Edge extant = edgeTypeMap.get(type);
				if (extant == null)
					edgeTypeMap.put(type,  edge);
			}
			for (MIM mim : edgeTypeMap.keySet())
			{
				if (mim == null) continue;
				Edge legendEntry = edgeTypeMap.get(mim);
				LegendRecord rec = new LegendRecord();
				rec.setType(mim.getDescription());
//				rec.setName(legendEntry.getGraphId());
				
				Label spacer = new Label();	spacer.setMinWidth(12); spacer.prefWidth(12);
				Label label1 = new Label();
				
				Group g = new Group();
				Line lin = new Line(0,0, 28, 0);
				lin.setStyle("-fx-stroke-width: 2");
				g.getChildren().add(lin);
				Shape shape =  mim.getShape();
				if (shape != null) 
				{
					shape.setRotate(180);
					shape.setStyle("-fx-stroke-width: 2");
					g.getChildren().add(shape) ;
				}
				label1.setGraphic(g);
				
				
				Label label2 = new Label(mim.getDescription());
				Label label3 = new Label("");
				label1.setMinWidth(lab1wid); label1.prefWidth(lab1wid);	label1.setMinHeight(lineHeight);
				label2.setMinWidth(lab2wid); label2.prefWidth(lab1wid);	label2.setMinHeight(lineHeight);
				label3.setMinWidth(lab3wid); label3.prefWidth(lab1wid);	label3.setMinHeight(lineHeight);
				HBox hbox = new HBox(SPACING, spacer, label1, label2, label3);
				box.getChildren().add(hbox);
				lineCt++;
			}
		}
		int refCt = 0;
		if (addRefs && !smallVersion)
		{
			separatorLine = new Line(0,20, WIDTH, 20);
			box.getChildren().add(separatorLine);
			for (BiopaxRecord ref : model.getReferences())
			{
				Label label1 = new Label();
				label1.setFont(Fonts.regularSerif);
				label1.setMinHeight(reflineHeight);
				Label label2 = new Label();
				label2.setFont(Fonts.smallSans);
				label2.setWrapText(true);
				int w = 80;
				label1.setMinWidth(w);			label1.prefWidth(w);
				label2.setMinWidth(WIDTH-w);	label2.prefWidth(WIDTH-w);
				label2.setPadding(new Insets(2,2,2,2));
				String authYr = ref.getFirstAuthor() + ref.getYear(); 
				label1.setText(authYr);
				label2.setText(ref.getTitle());
				Label spacer = new Label();	spacer.setMinWidth(SPACING); spacer.prefWidth(SPACING);
				HBox hbox = new HBox(SPACING,spacer, label1, label2 );
				if (refCt % 2 == 0)
					hbox.setBackground(Backgrounds.whitesmoke);
				box.getChildren().add(hbox);
				refCt++;
			}
		}
		stack.setHeight(lineHeight * lineCt + reflineHeight * refCt);
		return node;
	}

}
