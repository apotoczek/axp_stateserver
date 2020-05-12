import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.seisw.util.geom.Point2D;
import com.sun.net.httpserver.*;

/**
* The StateServer HTTPServer calculates which US state(s)
* contain the supplied longitude/latitude point input.
* Simplified state boundaries are supplied in states.json
* and sometimes overlap due to the simplified geometry.
* (some states are missing altogether)
*
* Example map of states.json geometries:
* http://tondogroup.com/gmap.php
*
* Tested on:
* $ java -version
* java version "1.8.0_31"
* Java(TM) SE Runtime Environment (build 1.8.0_31-b13)
* Java HotSpot(TM) 64-Bit Server VM (build 25.31-b07, mixed mode)
*
* Compile:
* $ javac -cp .:\* StateServer.java
*
* Run:
* $ java -cp .:\* StateServer
*
* Example input/output (single state):
* $ curl -d "longitude=-78.953149&latitude=39.137085" http://localhost:8081
* [West Virginia]
*
* Example input/output (example of overlap):
* $ curl -d "longitude=-78.353149&latitude=39.437085" http://localhost:8081
* [West Virginia, Maryland, Virginia]
*/


public class StateServer {
    public static void main(String[] args) throws IOException {
        InetSocketAddress port = new InetSocketAddress(8081);
        HttpServer server = HttpServer.create(port, 0);
        server.createContext("/", new StateHandler());  
        server.start();
    }
}


class StateHandler implements HttpHandler {

    public void handle(HttpExchange exchange) throws IOException {
        double longitude = 0.0;
        double latitude = 0.0;
    	
    	String requestMethod = exchange.getRequestMethod();
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(200, 0);
        PrintStream response = new PrintStream(exchange.getResponseBody());
        
        if(requestMethod.equalsIgnoreCase("POST")) {
        	BufferedReader body = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        	String query = body.readLine();
        	String[] params = query.split("&");
        	
        	if(params.length==2) {
        		longitude =  Double.parseDouble((params[0].split("="))[1]);
        		latitude =  Double.parseDouble((params[1].split("="))[1]);
        	}
        	
            try{
            	response.println(isInStates(longitude,latitude));
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
        response.close();
    }


    public static List<String> isInStates(double lng, double lat) throws IOException, JSONException {
    	List<String> matchList = new ArrayList<String>();
		List<String> statesList = Files.readAllLines(Paths.get("./states.json"), Charset.defaultCharset());

		for (int i = 0; i < statesList.size(); i++) {
			if(!statesList.get(i).isEmpty()) {
				String result = isInState(statesList.get(i),lng,lat);
				if(result!=null) {
					matchList.add(result);
				}
			}
		}
    	return matchList;
    }


    public static String isInState(String state, double lng, double lat) throws JSONException {
    	Point2D p = new Point2D(lng, lat);
    	JSONObject js = new JSONObject(state);

    	// state border with double precision
		Point2D[] points = new Point2D[js.getJSONArray("border").length()];
		for(int i = 0; i < js.getJSONArray("border").length(); i++) {
    		points[i] = new Point2D(
    			js.getJSONArray("border").getJSONArray(i).getDouble(0), 
    			js.getJSONArray("border").getJSONArray(i).getDouble(1)
    		);
		}

		// check if drawing a line from the test point to the right intersects any state border
		int i, j;
		boolean result = false;
		for (i = 0, j = points.length - 1; i < points.length; j = i++) {
			// if number of intersections is odd, then the point must be inside of state border
			if ((points[i].getY() > p.getY()) != (points[j].getY() > p.getY()) &&
				(p.getX() < (points[j].getX() - points[i].getX()) * (p.getY() - points[i].getY()) / (points[j].getY()-points[i].getY()) + points[i].getX())
				) { result = !result; }
		}

		if(result) return js.getString("state");
		return null;
    }

}
