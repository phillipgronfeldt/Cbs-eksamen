package com.cbsexam;

import cache.UserCache;
import com.google.gson.Gson;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.xpath.internal.operations.Bool;
import controllers.UserController;
import java.util.ArrayList;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.User;
import utils.Encryption;
import utils.Log;

@Path("user")
public class UserEndpoints {

  /**
   * @param idUser
   * @return Responses
   */
  @GET
  @Path("/{idUser}")
  public Response getUser(@PathParam("idUser") int idUser) {

    // Use the ID to get the user from the controller.
    User user = UserController.getUser(idUser);

    // TODO: Add Encryption to JSON - FIXED
    // Convert the user object to json in order to return the object
    String json = new Gson().toJson(user);
    json = Encryption.encryptDecryptXOR(json);

    // Return the user with the status code 200
    // TODO: What should happen if something breaks down? - FIXED
    //Tjekker om der hentes en bruger fra det ID som er indtastet i databasen, ved at lave user != null. Hvis der er hentet en bruger; status 200.
    //Hvis ikke en bruger er returneret, fejl 400 og fejlmeddelse
    if (user != null) {
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Couldnt find user").build();
    }
  }

  /** @return Responses */
  @GET
  @Path("/")
  public Response getUsers() {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(), this, "Get all users", 0);

    // Get a list of users
    //Tilføjer cache til vores getusers, sætter forceupdate til false
    UserCache userCache = new UserCache();
    ArrayList<User> users = userCache.getUsers(false);

    // TODO: Add Encryption to JSON - FIXED
    // Transfer users to json in order to return it to the user
    String json = new Gson().toJson(users);
    json = Encryption.encryptDecryptXOR(json);

    // Return the users with the status code 200
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createUser(String body) {

    // Read the json from body and transfer it to a user class
    User newUser = new Gson().fromJson(body, User.class);

    // Use the controller to add the user
    User createUser = UserController.createUser(newUser);

    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(createUser);

    // Return the data to the user
    if (createUser != null) {
      // Hvis token != null, returneres en status 200, altså en succes. Hvis token derimod ikke indeholder noget, eller er blevet lavet (= null)
      //fås fejl 400 og en fejlmeddelse
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not create user").build();
    }
  }

  // TODO: Make the system able to login users and assign them a token to use throughout the system. - FIXED
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response loginUser(String body) {

    //Body from json and put into user class
    User user = new Gson().fromJson(body, User.class);

    // Gets the user with the ID and return it
    String token = UserController.loginUser(user);

    //Returns data to user
    if (token != ""){
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("Your token" + token).build(); }
      else {
      return Response.status(400).entity("Could not login user.").build();
    }

  }

  // TODO: Make the system able to delete users - FIXED
  @DELETE
  @Path("/{userId}/{token}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response deleteUser(@PathParam("userId") int userId, @PathParam("token") String token) {

    Boolean deleted = UserController.deleteUser(token);

    if(deleted){

    // Return a response with status 200 and JSON as type
    return Response.status(200).entity("User deleted").build();
  } else
      return Response.status(400).entity("Couldnt deleted user").build();}

  // TODO: Make the system able to update users - FIXED
  @PUT
  @Path("/{userId}/{token}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateUser(@PathParam("token") String token, String body) {

    User user1 = new Gson().fromJson(body, User.class);

    Boolean opdated = UserController.updateUser(user1,token);

    if (opdated){
    return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("User is updated").build();
    } else
    // Return a response with status 200 and JSON as type
    return Response.status(400).entity("Endpoint not implemented yet").build();
  }
}

