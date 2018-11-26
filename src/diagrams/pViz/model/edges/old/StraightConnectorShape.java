package diagrams.pViz.model.edges.old;
//// PathVisio,
//// a tool for data visualization and analysis using Biological Pathways
//// Copyright 2006-2011 BiGCaT Bioinformatics
////
//// Licensed under the Apache License, Version 2.0 (the "License");
//// you may not use this file except in compliance with the License.
//// You may obtain a copy of the License at
////
//// http://www.apache.org/licenses/LICENSE-2.0
////
//// Unless required by applicable law or agreed to in writing, software
//// distributed under the License is distributed on an "AS IS" BASIS,
//// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//// See the License for the specific language governing permissions and
//// limitations under the License.
////
//package diagrams.pViz.model.edges;
//
//import diagrams.pViz.gpml.AbstractConnector;
//import javafx.geometry.Point2D;
//import javafx.scene.shape.Line;
//import javafx.scene.shape.Shape;
//import util.LineUtil;
///**
// * Implements a straight connector Shape, i.e. a Connector with
// * only 90-degree angles.
// */
//public class StraightConnectorShape extends AbstractConnector {
//
//	public void recalculateShape(ConnectorRestrictions restrictions) {
//		setSegments(new Segment[] {
//				new Segment(restrictions.getStartPoint(), restrictions.getEndPoint())
//		});
//		setShape(new Line(
//				restrictions.getStartPoint().getX(),restrictions.getStartPoint().getY(),
//				restrictions.getEndPoint().getX(),restrictions.getEndPoint().getY()
//		));
//		setWayPoints(new WayPoint[0]);
//	}
//
//
//
//	/**
//	 *  Calculate shape from the width of the line endings
//	 *  This gets
//	 */
//	 protected Shape calculateShape(Segment[] segments)
//	 {
//		 Point2D start = segments[0].getMStart();
//		 Point2D end = segments[segments.length - 1].getMEnd();
//		 return (new Line(start.getX(), start.getY(),end.getX(), end.getY()));
//	 }
//
//
//	public boolean hasValidWaypoints(ConnectorRestrictions restrictions) {
//		return false;
//	}
//
//	public Point2D fromLineCoordinate(double l) {
//		Segment[] segments = getSegments();
//		Point2D start = segments[0].getMStart();
//		Point2D end = segments[segments.length - 1].getMEnd();
//
//		double vsx = start.getX();
//		double vsy = start.getY();
//		double vex = end.getX();
//		double vey = end.getY();
//
//		int dirx = vsx > vex ? -1 : 1;
//		int diry = vsy > vey ? -1 : 1;
//
//		return new Point2D(
//			vsx + dirx * Math.abs(vsx - vex) * l,
//			vsy + diry * Math.abs(vsy - vey) * l
//		);
//	}
//
//	public double toLineCoordinate(Point2D v) {
//		Segment[] segments = getSegments();
//		return LineUtil.toLineCoordinates(
//				new Point2D(segments[0].getMStart().getX(), segments[0].getMStart().getY()),
//				new Point2D(segments[segments.length - 1].getMEnd().getX(),segments[segments.length - 1].getMEnd().getY() ),
//				new Point2D(v.getX(),v.getY())
//		);
//	}
//}
