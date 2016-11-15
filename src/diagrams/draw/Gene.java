package diagrams.draw;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Gene {

	public Gene(String inName)
	{
		this(inName, null);
	}
	public Gene(String inName, String ensm)
	{
		name.set(inName);
		ensembl.set(ensm);
	}
	
	private SimpleStringProperty name = new SimpleStringProperty();		// HGNC
	public StringProperty  nameProperty()  { return name;}
	public String getName()  { return name.get();}
	public void setName(String s)  { name.set(s);}
	
	private SimpleStringProperty ensembl = new SimpleStringProperty();
	public StringProperty  ensemblProperty()  { return ensembl;}
	public String getEnsembl()  { return ensembl.get();}
	public void setEnsembl(String s)  { ensembl.set(s);}
	
}
