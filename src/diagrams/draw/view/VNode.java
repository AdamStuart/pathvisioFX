package diagrams.draw.view;

import java.io.File;
import java.util.List;

import diagrams.draw.app.Controller;
import diagrams.draw.app.Tool;
import diagrams.draw.model.MNode;
import gui.Borders;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import model.AttributeMap;
import model.CSVTableData;
import util.FileUtil;
import util.MacUtil;

public class VNode extends StackPane {

	MNode model;
	Shape shape;
	Text text;
	Pasteboard pasteboard = null;
	
	public VNode(MNode modelNode, Pasteboard p)
	{
		super();
		model = modelNode;
		pasteboard = p;
		createShape();
//		if (shape != null)
//			ShapeFactory.setAttributes(shape, model.getAttributeMap());
		addText(modelNode.getId());
		setBorder(Borders.blueBorder1);
	}
	
	public void setText(String s)	{ 	text.setText(s);	}
	public String getText()			{ 	return text.getText();	}
	public Shape getShapeLayer()	{	return shape;	}
	public void setShapeLayer(Shape s){	shape = s;	}
	public MNode getModel()			{	return model;	}
	private String gensym(String s)	{	return model.getModel().gensym(s);	}
	public AttributeMap getAttributes() {		return model.getAttributeMap();	}
	public void setAttributes(AttributeMap attrs)	{	model.getAttributeMap().addAll(attrs);	}

	// **-------------------------------------------------------------------------------
	private void createShape() 
	{	
		String type = model.getShapeType();
		Tool tool = Tool.lookup(type);
		if (tool == null) return ;
		if (tool.isControl())
			addNewNode(tool, model);
		shape = pasteboard.getShapeFactory().makeNewShape(type, model, this);
//		setLayoutX(getAttributes().getDouble("X"));
//		setLayoutY(getAttributes().getDouble("Y"));
		setWidth(getAttributes().getDouble("Width", 10));
		setHeight(getAttributes().getDouble("Height", 10));
		
	}

	// **-------------------------------------------------------------------------------
	private void addNewNode(Tool type, MNode model)
	{
		switch (type)
		{
			case Browser:	makeBrowser();		break;
			case Text:		makeTextArea();		break;
			case Table:		makeTableView();	break;
			case Image:		makeImageView();	break;	
			case SVGPath:	makeSVGPath();		break;
			default:							break;
		}
	}
	// **-------------------------------------------------------------------------------
	public String toString()
	{
		Shape shape = getShapeLayer();
		String shapeName = shape == null ? "n/a" : shape.getClass().getSimpleName();
		return String.format("(%d) %s %s", getChildren().size(), shapeName, getId());
	}

	public Point2D center() {
    	Bounds bounds = getLayoutBounds();
    	double x = (bounds.getMinX() + bounds.getWidth()  / 2);
    	double y = (bounds.getMinY() + bounds.getHeight() / 2);
    	return new Point2D(x,y);
	}
	// **-------------------------------------------------------------------------------
	public void addContent(Node content)
	{
		getChildren().add(content);
		setAlignment(content, Pos.BOTTOM_CENTER);
	    setMargin(content, new Insets(25,0,0,0));
	}

	public void addText(String text)
	{
		final Label label = new Label(text);
		label.setFont(new Font(12));
		label.setMouseTransparent(true);
		getChildren().add(label);
		setAlignment(label, Pos.CENTER);
	}
	// **-------------------------------------------------------------------------------
	public void fill(AttributeMap attrMap, Node content)
	{
		String id = attrMap.getId();
		double x = attrMap.getDouble("X");
		double y = attrMap.getDouble("Y");
		double w = attrMap.getDouble("Width");
		double h = attrMap.getDouble("Height");
		String title = attrMap.get("name");
		fill( x,  y,  w,  h,  title,  id);
		addContent(content);
	}

	public void fill(double x, double y, double w, double h, String title, String id)
	{
		setId(id);
		if (Double.isNaN(w)) w = 400;
		if (Double.isNaN(h)) h = 300;
	
		setLayoutX(x - w/2);
		setLayoutY(y - h/2);
		prefWidth(w);	setMinWidth(w);  
		prefHeight(h);	setMinHeight(h); 
	   
		setStyle("-fx-border-color: green; -fx-border-width: 3; -fx-background-color: beige; -fx-opacity: 1.0;");	    
		HBox titleBar = new HBox(50);
	    titleBar.setMaxHeight(25);
	    Label idLabel = new Label(id);
	    idLabel.setMinWidth(50);
	    getChildren().addAll(titleBar);
	    StackPane.setAlignment(titleBar, Pos.TOP_CENTER);
	    Label titleLabel = new Label(title);
		titleBar.getChildren().addAll(idLabel, titleLabel);
	    titleBar.setMouseTransparent(true);
	}
	
	// **-------------------------------------------------------------------------------
	public VNode clone(String newId)
	{
		Object ob = getModel().getResource(getId());
		if (ob != null && ob instanceof VNode)
		{
			MNode newModel = new MNode(new AttributeMap(getAttributes()), getModel().getModel(), pasteboard);
			VNode copy = newModel.getStack();
			copy.setId(newId);
			return copy;
		}
		return null;
	}
	
	// **-------------------------------------------------------------------------------
	private void makeBrowser()
	{
		AttributeMap attrMap = model.getAttributeMap();
		attrMap.put("ShapeType","Browser");
		String url = attrMap.get("url");
		if (url == null) 
		{
			String filepath = attrMap.get("file");		// f.getAbsolutePath()
			url = MacUtil.urlFromPlist(filepath);
		}
		if (url == null) return ;
		WebView webView = new WebView();
		webView.setZoom(0.4);
		webView.getEngine().load(url);
		getChildren().add(webView);
	}
	
	// **-------------------------------------------------------------------------------
	private void makeImageView()
	{
		AttributeMap attrMap = model.getAttributeMap();
		attrMap.put("ShapeType","Image");
		String filepath = attrMap.get("file");		// f.getAbsolutePath()
		if (filepath == null) return;
		Image img = new Image("file:" + filepath);
		if (img.isError())
			System.out.println("makeImageView error");
		ImageView imgView = new ImageView(img);
		if (attrMap.getId() == null) 
			attrMap.put("GraphId", gensym("I"));
		
		imgView.prefWidth(200); 	imgView.prefHeight(200);
		imgView.setFitWidth(200); 	imgView.setFitHeight(200);
		attrMap.put("name", filepath);
//		Label imgView = new Label("TODO FIXME");
		
	    imgView.setMouseTransparent(true);
	    imgView.fitWidthProperty().bind(Bindings.subtract(widthProperty(), 20));
	    imgView.fitHeightProperty().bind(Bindings.subtract(heightProperty(), 40));
	    imgView.setTranslateY(-10);
		fill(attrMap, imgView);
	}
	
	// **-------------------------------------------------------------------------------
	private void makeSVGPath() {
		AttributeMap attrMap = model.getAttributeMap();
		attrMap.put("ShapeType","SVGPath");

		String path = attrMap.get("file");
		if (path != null)
		{
			String s = FileUtil.readFileIntoString(path);
			if (s != null)
			{
				SVGPath svg = new SVGPath();
				int idx1 = s.indexOf("<g>");
				int idx2 = s.indexOf("</g>") + 4;
				if (idx1 >0 && idx2 > idx1)
					s = s.substring(idx1, idx2);
				svg.setContent(s);
				fill(attrMap, svg);
			}
		}
	}
	// **-------------------------------------------------------------------------------
	private void makeTableView()
	{
		AttributeMap attrMap = model.getAttributeMap();
		attrMap.put("ShapeType","Table");
		TableView<ObservableList<StringProperty>> table = new TableView<ObservableList<StringProperty>>();
		if (attrMap.getId() == null)
			attrMap.put("GraphId", gensym("T"));
		CSVTableData data = FileUtil.openCSVfile(attrMap.get("file"), table);		// TODO THIS CURRENTLY ASSUMES ALL INTS!!
		attrMap.put("name", attrMap.get("file"));
		if (data == null) return;
		fill(attrMap, table);
	}
	// **-------------------------------------------------------------------------------
	public void makeTextArea()
	{
		AttributeMap attrMap = model.getAttributeMap();
		attrMap.put("ShapeType","Text");
		String text = attrMap.get("text");
		if (text == null)
		{
			String name = attrMap.get("file");
			StringBuilder buffer = new StringBuilder();
			attrMap.put("name", name);
			FileUtil.readFile(new File(name), buffer);
			text = buffer.toString();
			attrMap.put("text", text);
		}
		TextArea textArea = new TextArea(text);
	    textArea.setPrefColumnCount(60);
	    textArea.setPrefRowCount(20);
	    fill(attrMap, textArea);
	}
	// **-------------------------------------------------------------------------------
	public List<MenuItem> getMenuItems(MouseEvent event) {
			
		ObservableList<MenuItem> list = FXCollections.observableArrayList();
		//System.out.println("ContextMenu");
		int nKids = getChildren().size();
		Controller controller = pasteboard.getController();
		if (nKids == 2)
		{
			Node content = getChildren().get(1);
			if (content instanceof TableView)
			{
				MenuItem scatter = makeItem("Make Scatter Chart", e -> {});
				MenuItem timeseries = makeItem("Make Time Series", e -> {});
				SeparatorMenuItem sep = new SeparatorMenuItem();
				list.addAll(new MenuItem[] {scatter, timeseries, sep});
			}
			MenuItem toFront = makeItem("Bring To Front", e -> {   	controller.toFront();   });
			MenuItem toBack = makeItem("Send To Back", e -> {   	controller.toBack();   });
			MenuItem group = makeItem("Group", e ->  		{		controller.group();    });
			MenuItem ungroup = makeItem("Ungroup", e -> 	{ 		controller.ungroup();  });
			list.addAll(toFront, toBack, group, ungroup);
		}
		return list;
	}
	
	private MenuItem makeItem(String name, EventHandler<ActionEvent> foo) {
		MenuItem item = new MenuItem(name);
		item.setOnAction(foo);
		return item;
	}
}
