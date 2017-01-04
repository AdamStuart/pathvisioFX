package diagrams.pViz.view;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class KeyFrameRecord
{
	DoubleProperty hold = new SimpleDoubleProperty();
	DoubleProperty move = new SimpleDoubleProperty();
	StringProperty name = new SimpleStringProperty();
//	StringProperty path = new SimpleStringProperty();
//	BooleanProperty lock = new SimpleBooleanProperty();
	
	public KeyFrameRecord(boolean lck, String inName, double inHold, double inMove) {
		hold.set(inHold);
		move.set(inMove);
		name.set(inName);
//		path.set(inName);
//		lock.set(lck);
	}
	public DoubleProperty  moveProperty()  { return move;}
	public Double getMove()  { return move.get();}
	public void setMove(Double b)  { move.set(b);}

	public DoubleProperty  holdProperty()  { return hold;}
	public Double getHold()  { return hold.get();}
	public void setHold(Double b)  { hold.set(b);}

//	public BooleanProperty  lock()  { return lock;}
//	public Boolean getLock()  { return lock.get();}
//	public void setLock(Boolean b)  { lock.set(b);}
//	public boolean toggleLock() 	{ setLock(!getLock());	return getLock(); }
//
//	public StringProperty  pathProperty()  { return path;}
//	public String getPath()  { return path.get();}
//	public void setPath(String b)  { path.set(b);}
//
	public StringProperty  nameProperty()  { return name;}
	public String getName()  { return name.get();}
	public void setName(String b)  { name.set(b);}

	String state;
	public void setState(String xml)	{  state = xml;	}
	public String getState()	{  return state;	}
}
