package diagrams.pViz.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import diagrams.pViz.model.nodes.DataNode;
import util.StringUtil;

public class Reference  {

	//---------------------------------------------------------------------------------------------
	private	 String graphId;
	private	 String db;
	private	 String dbid;
		 
	public	 Reference(String a, String b, String c)
	{
		super();
		graphId = a;
		db = b;
		dbid =c ;
	}
		 
//---------------------------------------------------------------------------------------------
 
	public static void annotateIdentifiers(Map<String, DataNode> dataNodeMap) {
		 System.out.println("Annotate");
		 Set<String> dbs = new HashSet<String>();		
		 List<Reference> refs = new ArrayList<Reference>();		
		 for (DataNode n : dataNodeMap.values())
		 {
			 String type = n.getType();		if (StringUtil.isEmpty(type)) continue;
			 String db = n.getDatabase();	if (StringUtil.isEmpty(db)) continue;
			 String dbid = n.getDbid();	 	if (StringUtil.isEmpty(dbid)) continue;
			 String id = n.getGraphId();	if (StringUtil.isEmpty(id)) continue;
			 refs.add(new Reference(id, db, dbid));
			 dbs.add(db);
		 }
		Iterator<String> iter = dbs.iterator();
		while (iter.hasNext())
		{
			String db = iter.next();
			List<Reference> match = filterByDB(refs, db);
			String idlist = idListToString(match);
		}
	}
	static  List<Reference> filterByDB(List<Reference> inList, String db)
	 {
		 List<Reference> subset = new ArrayList<Reference>();		
		 for (Reference r : inList)
			 if (db.equals(r.db))
				 subset.add(r);
		 return subset;
	 }
	 
	static  String idListToString(List<Reference> inList)
	 {
		 StringBuilder build = new StringBuilder();
		 for (Reference r : inList)
			 build.append(r.graphId).append("\t");
		 return StringUtil.chopLast(build.toString());
	 }
	}
