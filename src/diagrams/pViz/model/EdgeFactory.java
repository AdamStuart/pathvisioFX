//package diagrams.pViz.model;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.w3c.dom.NamedNodeMap;
//import org.w3c.dom.NodeList;
//
//import diagrams.pViz.app.Controller;
//import diagrams.pViz.app.UndoStack;
//import diagrams.pViz.view.Pasteboard;
//import diagrams.pViz.view.ShapeFactory;
//import javafx.scene.Node;
//import model.AttributeMap;
//import model.bio.GPMLPoint;
//import util.StringUtil;
//
//public class EdgeFactory {
//
//	private Pasteboard drawLayer;
//	private UndoStack undoStack;
//	private ShapeFactory shapeFactory;	
//
//	public EdgeFactory(Pasteboard pasteboard) {
//		drawLayer = pasteboard;
//		undoStack = drawLayer.getController().getUndoStack();
//		shapeFactory = new ShapeFactory(drawLayer, undoStack);
//	}
//	private Controller getController()		{ 	return drawLayer.getController();	}
//	private Model getModel()				{ 	return getController().getDrawModel();	}
//	public ShapeFactory getShapeFactory()	{ 	return shapeFactory; }
//
//	public Edge parseGPML(org.w3c.dom.Node edgeML) {
//		AttributeMap attrMap = new AttributeMap();
//		attrMap.add(edgeML.getAttributes());
//		List<GPMLPoint> points = new ArrayList<GPMLPoint>();
//		NodeList elems = edgeML.getChildNodes();
//		String startId="", endId="";
//		for (int i=0; i<elems.getLength(); i++)
//		{
//			org.w3c.dom.Node n = elems.item(i);
//			String name = n.getNodeName();
//			if ("Graphics".equals(name))
//			{
//				attrMap.add(n.getAttributes());
//				NodeList pts = n.getChildNodes();
//				for (int j=0; j<pts.getLength(); j++)
//				{
//					org.w3c.dom.Node pt = pts.item(j);
//					if ("Point".equals(pt.getNodeName()))
//						points.add(new GPMLPoint(pt));
//				}
//			}
//			if ("Xref".equals(name))	
//				attrMap.add(n.getAttributes());
//			if ("BiopaxRef".equals(name))	
//				attrMap.put("BiopaxRef", n.getTextContent());
//		}
//		int z = points.size();
//		if (z > 1)
//		{
//			startId = points.get(0).getGraphRef();
//			MNode startNode = getModel().getResource(startId);
//			attrMap.put("start", startId);
//			GPMLPoint lastPt = points.get(z-1);
//			endId = lastPt.getGraphRef();
//			attrMap.put("end", endId);
//			MNode endNode = getModel().getResource(endId);
//			if (endNode != null) 
//				return new Edge(startNode.getStack(), endNode.getStack(), attrMap, points);			
//		}
//
//		return new Edge(attrMap, getModel());
//	}
//	//--------------------------------------------
//	private String getStr(NamedNodeMap map, String key) {
//		org.w3c.dom.Node node = map.getNamedItem(key);
//		return 	node == null ? null : node.getNodeValue();
//	}
//	private double getVal(NamedNodeMap map, String key) {
//		org.w3c.dom.Node node = map.getNamedItem(key);
//		return 	node == null ? Double.NaN : StringUtil.toDouble(node.getNodeValue());
//	}
//	//--------------------------------------------
//}
