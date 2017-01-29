package client;

import java.io.IOException;
import java.net.URI;    
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class ClientRequests {
	
	private ClientConfig clientConfig;
	private Client client;
	private WebTarget service;
	
	
	public ClientRequests() {
		clientConfig = new ClientConfig();
        client = ClientBuilder.newClient(clientConfig);
        service = client.target(getBaseURI());
	}

    protected static URI getBaseURI() {
        //return UriBuilder.fromUri("http://127.0.1.1:5701/gateway").build();
        return UriBuilder.fromUri("https://virtual-lifecoach-pc.herokuapp.com/gateway").build();
    }
    
	public Response doGET(String path) throws JsonParseException, JsonMappingException, IOException{
		Response response = null;
		
		response = service.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON).get();
		
    	return response;
    }	
	
	public Response doPUT(String path, String request) throws JsonParseException, JsonMappingException, IOException{
		Response response = null;

		response = service.path(path).
			request(MediaType.APPLICATION_JSON_TYPE).put(Entity.json(request));
		
    	return response;
    }
	
	public Response doPOST(String path, String request) throws JsonParseException, JsonMappingException, IOException{
		Response response = null;

		response = service.path(path).
			request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(request));
		
    	return response;
    }
	
	public Response doDELETE(String path) throws JsonParseException, JsonMappingException, IOException{
		Response response = null;

		response = service.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON).delete();
		
    	return response; 	
    }
}