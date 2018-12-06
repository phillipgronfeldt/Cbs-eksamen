package controllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import model.User;
import org.apache.solr.common.util.Hash;
import utils.Hashing;
import utils.Log;

public class UserController {

  private static DatabaseController dbCon;

  public UserController() {
    dbCon = new DatabaseController();
  }

  public static User getUser(int id) {

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build the query for DB
    String sql = "SELECT * FROM user where id=" + id;

    // Actually do the query
    ResultSet rs = dbCon.query(sql);
    User user = null;

    try {
      // Get first object, since we only have one
      if (rs.next()) {
        user =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"));

        // return the create object
        return user;
      } else {
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return user;
  }

  /**
   * Get all users in database
   *
   * @return
   */
  public static ArrayList<User> getUsers() {

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL
    String sql = "SELECT * FROM user";

    // Do the query and initialyze an empty list for use if we don't get results
    ResultSet rs = dbCon.query(sql);
    ArrayList<User> users = new ArrayList<User>();

    try {
      // Loop through DB Data
      while (rs.next()) {
        User user =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"));

        // Add element to list
        users.add(user);
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return the list of users
    return users;
  }

  public static User createUser(User user) {

    //initialiserer hash
    Hashing hashname = new Hashing();

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), user, "Actually creating a user in DB", 0);

    // Set creation time for user.
    user.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Insert the user in the DB
    // TODO: Hash the user password before saving it. FIXED
    int userID = dbCon.insert(
            "INSERT INTO user(first_name, last_name, password, email, created_at) VALUES('"
                    + user.getFirstname()
                    + "', '"
                    + user.getLastname()
                    + "', '"
                    //Tilføjer hash til passwordet
                    + hashname.HashWithSalt(user.getPassword())
                    + "', '"
                    + user.getEmail()
                    + "', "
                    + user.getCreatedTime()
                    + ")");

    if (userID != 0) {
      //Update the userid of the user before returning
      user.setId(userID);
    } else {
      // Return null if user has not been inserted into database
      return null;
    }

    // Return user
    return user;
  }

  public static String loginUser(User user) {
    //Checks if there is database connection


    if (dbCon == null) {
      dbCon = new DatabaseController();

      ResultSet resultSet;
      User newUser;
      String token = null;

      try {
        PreparedStatement loginUser = dbCon.getConnection().prepareStatement("SELECT * FROM user WHERE email = ? AND password = ?");
        loginUser.setString(1, user.getEmail());
        loginUser.setString(2, user.getPassword());

        resultSet = loginUser.executeQuery();

        if (resultSet.next()) {
          newUser = new User(
                  resultSet.getInt("id"),
                  resultSet.getString("first_name"),
                  resultSet.getString("last_name"),
                  resultSet.getString("password"),
                  resultSet.getString("email"));

          if (newUser != null) {
            try {
              Algorithm algorithm = Algorithm.HMAC256("secret");
              token = JWT.create()
                      .withClaim("userID", newUser.getId())
                      .withIssuer("auth0")
                      .sign(algorithm);
            } catch (JWTCreationException exception) {

            } finally {
              return token;
            }
          }
        } else {
          System.out.println("No user found");
        }
      } catch (SQLException ex) {
        ex.printStackTrace();
      }

    }
    return "";
  }

  public static Boolean deleteUser(String token) {
    if (dbCon == null) {
      dbCon = new DatabaseController();

      try {
        DecodedJWT jwt = JWT.decode(token);
        int id = jwt.getClaim("userId").asInt();

        try {
          PreparedStatement deleteUser = dbCon.getConnection().prepareStatement("DELETE FROM USER WHERE id = ? ");

          deleteUser.setInt(1, id);

          int rowsAffected = deleteUser.executeUpdate();

          if (rowsAffected == 1) {
            return true;
          }
        } catch (SQLException sql) {
          sql.printStackTrace();
        }

      } catch (JWTCreationException exception) {
        exception.printStackTrace();
      }
      return false;

    }

    return null;
  }
  public static Boolean updateUser(User user, String token) {
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }
      try{ DecodedJWT jwt = JWT.decode(token);
        int id = jwt.getClaim("userId").asInt();

        try { PreparedStatement updateUser = dbCon.getConnection().prepareStatement("UPDATE USER SET" + "first_name = ?, last_name = ?, password = ?, email = ? WHERE id = ? ");
             updateUser.setString(1, user.getFirstname());
             updateUser.setString(2, user.getLastname());
             updateUser.setString(3, user.getPassword());
             updateUser.setString(4, user.getEmail());
             updateUser.setInt(5, id);
             int rowsaffected = updateUser.executeUpdate();

             if (rowsaffected == 1){
               return true;

             }
        }catch (SQLException sql)   {
               sql.printStackTrace();
          }
      }catch (JWTCreationException exception) {
      exception.printStackTrace();
      }
      return false;
          
  }
}