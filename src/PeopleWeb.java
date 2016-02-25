import com.sun.org.apache.xpath.internal.operations.Mod;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class PeopleWeb {

    static ArrayList<Person> persons = new ArrayList<>();

    public static void main(String[] args) throws IOException {

        File f = new File("persons.csv");
        Scanner scanner = new Scanner(f);

        scanner.nextLine();

        while (scanner.hasNextLine()) {
            String[] pieces = scanner.nextLine().split(",");
            Person p = new Person(Integer.valueOf(pieces[0]), pieces[1], pieces[2], pieces[3], pieces[4], pieces[5]);
            persons.add(p);
        }

        Spark.init();
        Spark.get(
                "/",
                ((request, response) -> {
                    String offset = request.queryParams("offset");
                    int offsetNumber = 0;
                    if (offset != null) {
                        offsetNumber = Integer.valueOf(offset);
                    }

                    ArrayList<Person> firstSet = new ArrayList<>(persons.subList(offsetNumber, 20 + offsetNumber));
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
                    if (offsetNumber < persons.size() -20) {
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
                    Person person = persons.get(id - 1);
                    m.put("person", person);
                    return new ModelAndView(m, "person.html");
                }),
                new MustacheTemplateEngine()
        );
    }
}





//        Create a GET route for / that simply lists the names of each person in ArrayList<Person>. It should only
// display 20 names, and should have a "Previous" and "Next" button at the bottom only if necessary (don't show the
// "Previous" button on the first page, and don't show the "Next" button on the last page). It should take a GET
// parameter which is the offset it is supposed to start at, like this: /?offset=20.

//        Create another GET route called /person which displays all the data about a single person. It should take
// a GET parameter which is the id for that person, like this: /person?id=1

//        Make all the people shown on the main page link to their /person page, so I can click on their names for
// additional information.