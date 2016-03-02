package com.theironyard;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by Caroline on 3/2/16.
 */
public class PeopleWebTest {
    public Connection startConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        PeopleWeb.createTables(conn);
        return conn;
    }

    public void endConnection(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE people");
        conn.close();
    }

    @Test
    public void testPerson() throws SQLException {
        Connection conn = startConnection();
        PeopleWeb.insertPerson(conn, "Caroline", "Vail", "carolineevail@gmail.com", "US", "ipaddress");
        Person person = PeopleWeb.selectPerson(conn, 1);
        endConnection(conn);
        assertTrue(person != null);
    }

    @Test
    public void testPeople() throws SQLException {
        Connection conn = startConnection();
        PeopleWeb.insertPerson(conn, "Caroline", "Vail", "carolineevail@gmail.com", "US", "ip");
        ArrayList<Person> people = PeopleWeb.selectPeople(conn, 1);
        endConnection(conn);
        assertTrue(people != null);
    }
}



//insert person and select person