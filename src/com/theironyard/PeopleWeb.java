package com.theironyard;

import spark.ModelAndView;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class PeopleWeb {

    public static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS people");
        stmt.execute("CREATE TABLE IF NOT EXISTS people (id IDENTITY, first_name VARCHAR, last_name VARCHAR, email VARCHAR, country VARCHAR, ip VARCHAR)");
    }

    public static void insertPerson(Connection conn, String firstName, String lastName, String email, String country, String ipAddress) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO people VALUES (NULL, ?, ?, ?, ?, ?)");
        stmt.setString(1, firstName);
        stmt.setString(2, lastName);
        stmt.setString(3, email);
        stmt.setString(4, country);
        stmt.setString(5, ipAddress);
        stmt.execute();
    }

    public static Person selectPerson(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM people WHERE id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            int personId = results.getInt("id");
            String firstName = results.getString("first_name");
            String lastName = results.getString("last_name");
            String email = results.getString("email");
            String country = results.getString("country");
            String ipAddress = results.getString("ip");
            return new Person(personId, firstName, lastName, email, country, ipAddress);
        }
        return null;
    }

    public static void populateDatabase(Connection conn) throws SQLException, FileNotFoundException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO people VALUES (NULL, ?, ?, ?, ?, ?)");
        File f = new File("persons.csv");
        Scanner scanner = new Scanner(f);
        scanner.nextLine();
        while (scanner.hasNextLine()) {
            String[] columns = scanner.nextLine().split(",");
            insertPerson(conn, columns[1], columns[2], columns[3], columns[4], columns[5]);
        }
    }

    public static ArrayList<com.theironyard.Person> selectPeople(Connection conn, int offset) throws SQLException {
        ArrayList<com.theironyard.Person> p = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM people LIMIT 20 OFFSET ?");
        stmt.setInt(1, offset);
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            int id = results.getInt("id");
            String firstName = results.getString("first_name");
            String lastName = results.getString("last_name");
            String email = results.getString("email");
            String country = results.getString("country");
            String ipAddress = results.getString("ip");
            Person person = new Person(id, firstName, lastName, email, country, ipAddress);
            p.add(person);
        }
        return p;
    }

//    static ArrayList<Person> persons = new ArrayList<>();

    public static void main(String[] args) throws IOException, SQLException {

        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);
        populateDatabase(conn);


//        File f = new File("persons.csv");
//        Scanner scanner = new Scanner(f);
//
//        scanner.nextLine();
//
////        while (scanner.hasNextLine()) {
////            String[] columns = scanner.nextLine().split(",");
////            Person p = new Person(Integer.valueOf(columns[0]), columns[1], columns[2], columns[3], columns[4], columns[5]);
////            persons.add(p);
////        }

        Spark.init();
        Spark.get(
                "/",
                ((request, response) -> {
                    String offset = request.queryParams("offset");
                    int offsetNumber = 0;
                    if (offset != null) {
                        offsetNumber = Integer.valueOf(offset);
                    }

                    ArrayList<Person> firstSet = selectPeople(conn, offsetNumber);
                    HashMap m = new HashMap();
                    m.put("persons", firstSet);
                    m.put("number", offsetNumber +20);
                    m.put("previous", offsetNumber -20);
                    boolean showPrevious = false;
                    if (offsetNumber >= 20) {
                        showPrevious = true;
                    }
                    m.put("showPrevious", showPrevious);
                    boolean showNext = false;
                    if (selectPeople(conn, offsetNumber +20).size() >= 1) {
                        showNext = true;
                    }
                    m.put("showNext", showNext);
                    return new ModelAndView(m, "main.html");
                }),
                new MustacheTemplateEngine()
        );
        Spark.get(
                "/person",
                ((request, response) -> {
                    int id = Integer.valueOf(request.queryParams("id"));
                    HashMap m = new HashMap();
                    Person person = selectPerson(conn, id);
                    m.put("person", person);
                    return new ModelAndView(m, "person.html");
                }),
                new MustacheTemplateEngine()
        );
    }
}
