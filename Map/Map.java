import java.util.List;
import java.util.Collections;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Path;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDGS;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.algorithm.Dijkstra.Element;

public class Map extends HttpServlet {

	 private static final long serialVersionUID = 1;

	 private Graph graph;

	 public Map() {

		  this.graph = new DefaultGraph("autoroutes");

		  FileSource fs = new FileSourceDGS();
		  fs.addSink(this.graph);
		  try {
				fs.readAll("autoroutes.dgs");
		  }
		  catch(Exception e) {
				System.out.println(e);
		  }
	 }

	 public void doGet( HttpServletRequest req, HttpServletResponse res )
		  throws ServletException, IOException
	 {
		  try {
				String from = req.getParameter("from");
				String to = req.getParameter("to");

				Dijkstra dijkstra = new Dijkstra(Element.edge, "weight", from);
				dijkstra.init(this.graph);
				dijkstra.compute();

				Path path = dijkstra.getShortestPath(this.graph.getNode(to));
				List<Node> nodes = path.getNodePath();
				List<Edge> edges = path.getEdgePath();

				Collections.reverse(nodes);
				Collections.reverse(edges);

				StringBuilder sb = new StringBuilder();
				sb.append("[");

				for(int i = 0; i < path.size(); ++i) {

					 sb.append("{\"city\":\"");
					 sb.append(nodes.get(i).getId());
					 sb.append("\"");

					 if(i < path.size() - 1) {

						  sb.append(",\"route\":\"");
						  sb.append(edges.get(i).getId());
						  sb.append("\"");

						  String d = edges.get(i).getAttribute("weight").toString();
						  sb.append(",\"distance\":\"");
						  sb.append(d.substring(0, d.length()-2));
						  sb.append("\"");
					 }
				
					 sb.append("}");

					 if(i < path.size() - 1)
						  sb.append(",");
				}

				sb.append("]");
		  
				res.setContentType("text/plain");
				res.setHeader("Cache-Control", "no-cache");
				res.getWriter().write(sb.toString());
		  }
		  catch(Exception e) {

				res.setContentType("text/plain");
				res.setHeader("Cache-Control", "no-cache");
				res.getWriter().write(e.toString());
		  }
	 }
}
