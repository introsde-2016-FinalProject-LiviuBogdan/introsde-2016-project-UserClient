package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ClientMain {
	
    private ClientRequests client;
    private List<String> users;
    private final String[] profileFields = {"firstname","lastname","birthdate","email"};
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
    public ClientMain(){		
		client = new ClientRequests();   
	}

    public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
    	ClientMain c = new ClientMain();
    	c.prelogin();
    }
    
    private void prelogin() throws IOException, JSONException {
		boolean run = true;
		
		while (run) {
			System.out.println("\n\t Welcome to Virtual LifeCoach!");
			System.out.println("");
			System.out.println("");
			
			System.out.println("1 - Choose Account");
			System.out.println("2 - Create Account");
			System.out.println("q - Exit");
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String option = reader.readLine();
			switch (option) {
			case "1":
				getUsers();
				String choice = reader.readLine();
				if(users.contains(choice)){
					menu(choice);
				}
				break;
				
			case "2":
				register(reader);
				break;
			case "q":
				run = false;
				break;
			default:
				break;
			}
		}
	}    
    
    private void menu(String choice) throws IOException, JSONException {
		boolean run = true;

		Response response = client.doGET("/profile/"+choice);
		JSONObject user = new JSONObject(response.readEntity(String.class));

		System.out.println("\n------ Logged in successful ------");
		while (run) {
			System.out.println("\n------ MENU ------");
			System.out.println("User: " + user.get("firstname") + " " + user.get("lastname"));
			System.out.println("\n1 - Profile");
			System.out.println("2 - Update Profile");
			System.out.println("3 - Add Measure");
			System.out.println("4 - Goal List");
			System.out.println("5 - Achievement List");
			System.out.println("6 - Set New Goal");
			System.out.println("q - Logout");
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String option = reader.readLine();
			switch (option) {
			case "1":
				printProfile(user);
				break;
			case "2":
				updateProfile(choice, reader);
				user = new JSONObject(client.doGET("/profile/"+choice).readEntity(String.class));
				break;
			case "3":
				addMeasure(choice,reader);
				user = new JSONObject(client.doGET("/profile/"+choice).readEntity(String.class));
				break;
			case "4":
				seeGoals(choice);
				break;
			case "5":
				seeAchievements(choice);
				break;	
			case "6":
					setGoal(choice,reader);
					break;
			case "q":
				run = false;
				break;
			default:
				break;
			}
		}
	}
    
    private void register(BufferedReader reader) throws JSONException, IOException{
    	JSONObject user = new JSONObject();
    	
    	for(String s : profileFields){
    		if(s.equals("birthdate"))
    			System.out.println("Insert "+s+"(yyyy-mm-dd): ");
    		else	
    			System.out.println("Insert "+s+": ");
        	user.put(s, (Object)reader.readLine());
        	
        	System.out.println();
    	}
    	user.put("currentHealth", new JSONObject());
    	
    	//System.out.println(prettyJSON(user.toString())); 	
    	Response response = client.doPOST("/profile", user.toString());
    	if(response.getStatus() == 200)
    			System.out.println("User created succesfully!");
		else
			System.out.println("User creation failed!");	
    	
    	System.out.println();
    }
    
    private void updateProfile(String userId, BufferedReader reader) throws JSONException, IOException{
    	JSONObject user = new JSONObject();
    	
    	for(String s : profileFields){
        	System.out.println("Update "+ s +"(y/n)");
        	if(reader.readLine().equals("y")){
        		if(s.equals("birthdate"))
        			System.out.println("Insert "+s+"(yyyy-mm-dd): ");
        		else
        			System.out.println("Insert "+s+": ");
            	user.put(s, (Object)reader.readLine());
        	}
        	System.out.println();
    	}
    	user.put("currentHealth", new JSONObject());
    	
    	//System.out.println(prettyJSON(user.toString())); 	
    	Response response = client.doPUT("/profile/"+userId, user.toString());
    	//System.out.println(response.getStatus());
    	if(response.getStatus() == 200)
    			System.out.println("User updated succesfully!");
		else
			System.out.println("User update failed!");	
    	
    	System.out.println();
    }    
    
    private void addMeasure(String userId, BufferedReader reader) throws JSONException, IOException{
    	JSONObject measure = new JSONObject();

    	Response response = client.doGET("/measureTypes");
    	System.out.println("Select one of the following types \n");
    	JSONArray typesJson = new JSONArray(response.readEntity(String.class));
    	List<String> types = new ArrayList<String>();
    	for(int i = 0; i<typesJson.length();i++){
    		types.add(typesJson.get(i).toString());
    		System.out.println(typesJson.get(i).toString());
    	}
    	System.out.println();
    	
    	String type = "notype";
    	
    	while(!types.contains(type)){
    		System.out.println("Select correct type: ");
    		type = reader.readLine();
    	}
    	
    	System.out.println("Insert measure value:");
    	measure.put("measureValue", (Object)reader.readLine());
    	measure.put("measureDefinition", new JSONObject());
    	measure.getJSONObject("measureDefinition").put("measureType", type);
    	
    	//System.out.println(prettyJSON(measure.toString()));
    	response = client.doPOST("/profile/"+userId+"/history"+"/"+type, measure.toString());
    	//System.out.println(response.getStatus());
    	if(response.getStatus() == 200){
    			System.out.println("Measure added succesfully!");
    			JSONObject feedback = new JSONObject(response.readEntity(String.class));
    			System.out.println(feedback.get("message"));
    			System.out.println(feedback.get("link"));
    	}
		else
			System.out.println("Measure insert failed!");	
    	
    	System.out.println();
    }
    
    private void seeGoals(String userId) throws JSONException, IOException{
    	JSONArray goals = new JSONArray(client.doGET("/profile/"+userId+"/goals")
    										.readEntity(String.class));
    	
    	System.out.println("\n\tCurrent goals!");
    	if(goals.length() == 0)
    		System.out.println("No achievements currently!");
    	for (int i = 0; i < goals.length(); i++)
    	{
    		JSONObject g = goals.getJSONObject(i);
    		System.out.println("\t ==> Gid: " + g.get("gid"));
			System.out.println("\t ==> Value: " + g.get("value"));
			System.out.println("\t ==> Measure Type: " + g.getJSONObject("measureDefinition").get("measureType"));
			
			System.out.println("");
    	}
    	
    	System.out.println();
    }
    
    private void seeAchievements(String userId) throws JSONException, IOException{
    	JSONArray goals = new JSONArray(client.doGET("/profile/"+userId+"/achievements")
    										.readEntity(String.class));
    	
    	System.out.println("\n\tLifetime Achievements!");
    	if(goals.length() == 0)
    		System.out.println("No achievements currently!");
    	for (int i = 0; i < goals.length(); i++)
    	{
    		JSONObject g = goals.getJSONObject(i);
    		System.out.println("\t ==> AchievementId: " + g.get("achievementId"));
			System.out.println("\t ==> Value: " + g.get("value"));
			System.out.println("\t ==> Date Completed: " + dateFormat.format(g.get("completed")));
			System.out.println("\t ==> Measure Type: " + g.getJSONObject("measureDefinition").get("measureType"));
			
			System.out.println("");
    	}
    	
    	System.out.println();
    }
    
    private void setGoal(String userId, BufferedReader reader) throws JSONException, IOException{
    	JSONObject goal = new JSONObject();

    	Response response = client.doGET("/measureTypes");
    	System.out.println("Select one of the following types \n");
    	JSONArray typesJson = new JSONArray(response.readEntity(String.class));
    	List<String> types = new ArrayList<String>();
    	for(int i = 0; i<typesJson.length();i++){
    		types.add(typesJson.get(i).toString());
    		System.out.println(typesJson.get(i).toString());
    	}
    	System.out.println();
    	
    	String type = "notype";
    	
    	while(!types.contains(type)){
    		System.out.println("Select correct type: ");
    		type = reader.readLine();
    	}
    	
    	System.out.println("Insert goal value:");
    	goal.put("value", (Object)reader.readLine());
    	goal.put("measureDefinition", new JSONObject());
    	goal.getJSONObject("measureDefinition").put("measureType", type);
    	
    	//System.out.println(prettyJSON(measure.toString()));
    	response = client.doPOST("/profile/"+userId+"/goals", goal.toString());
    	//System.out.println(response.getStatus());
    	if(response.getStatus() == 200){
    			System.out.println("Goal added succesfully!");
    	}
		else
			System.out.println("Goal insert failed!");	
    	
    	System.out.println();
    }
    
    
    private void getUsers() throws JsonParseException, JsonMappingException, IOException{   	
    	
    	Response response = client.doGET("/profile");
    	JSONArray people = new JSONArray(response.readEntity(String.class));
    	users = new ArrayList<String>();

		for(String s : users){
			System.out.println("UserId "+ s);
		}
    	for (int i = 0; i < people.length(); i++)
    	{
    		String userId = ""+people.getJSONObject(i).getLong("personId");
    		System.out.println(userId + " - "+people.getJSONObject(i).get("firstname")
    				+" "+ people.getJSONObject(i).get("lastname"));
    		users.add(userId);
    	}

    }
	
	public static void printProfile(JSONObject p) {
       	if(p != null){
			System.out.println("");
			if(!p.isNull("firstname"))
				System.out.println(" ==> Firstname: " + p.get("firstname"));
			if(!p.isNull("lastname"))
				System.out.println(" ==> Lastname: " + p.get("lastname"));
			if(!p.isNull("birthdate"))
				System.out.println(" ==> Birthdate: " + dateFormat.format(p.get("birthdate")));
			if(!p.isNull("email"))
				System.out.println(" ==> Email: " + p.get("email"));
			System.out.println(" ==> Current Health Profile:\n");

			if(!p.getJSONObject("currentHealth").isNull("measure")){
				JSONArray m = p.getJSONObject("currentHealth").getJSONArray("measure");

				for (int i = 0; i < m.length(); i++)
		    	{
		    		printMeasure(m.getJSONObject(i));
		    	}
			} 
			else {
				System.out.println("\t No Measures");
			}
			System.out.println("");
       	} else{
       		System.out.println("\tPerson not found.");
       	}
   	}
	
	public static void printMeasure(JSONObject m) {
		
		if(m != null){
			if(!m.isNull("mid"))
				System.out.println("\t ==> Mid: " + m.get("mid"));
			if(!m.isNull("dateRegistered"))
				System.out.println("\t ==> Date Registered: " + dateFormat.format(m.get("dateRegistered")));
			if(!m.isNull("measureValue"))
				System.out.println("\t ==> Measure Value: " + m.get("measureValue"));
			if(!m.isNull("measureDefinition"))
				System.out.println("\t ==> Measure Type: " + m.getJSONObject("measureDefinition").get("measureType"));
			
			System.out.println("");
	   	} else{
	   		System.out.println("\tNo measure");
	   	}
	}	
	
	public static String prettyJSON(String input) throws JsonParseException,
			JsonMappingException, IOException {

    	if(input != null && !input.isEmpty()){
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
	
			Object json = mapper.readValue(input, Object.class);
			String indented = mapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(json);
			return indented;
    	}
    	return " ";
	} 
}
