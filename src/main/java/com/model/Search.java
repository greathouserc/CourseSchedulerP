package com.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Scanner;

public class Search {
    private String query;
    private ArrayList<Course> listings = new ArrayList<>();
    private ArrayList<Course> searchResults = new ArrayList<>();

    public static Course createCourseFromJSON(JSONObject c, int id) {
        Course currCourse = new Course(id);
        int credits = c.getInt("credits");
        currCourse.setCredits(credits);

        JSONArray fac = c.getJSONArray("faculty");
        for (int j = 0; j < fac.length(); j++) {
            String faculty = fac.getString(j);
            currCourse.getProfessor().add(faculty.toLowerCase());
        }
        boolean isLab = c.getBoolean("is_lab");
        boolean isOpen = c.getBoolean("is_open");
        currCourse.setLab(isLab);
        currCourse.setOpen(isOpen);

        String location = c.getString("location");
        String name = c.getString("name");
        currCourse.setLocation(location.toLowerCase());
        currCourse.setName(name.toLowerCase());

        int number = c.getInt("number");
        int open_seats = c.getInt("open_seats");
        currCourse.setCourseCode(number);
        currCourse.setOpenSeats(open_seats);

        String section = c.getString("section");
        String semester = c.getString("semester");
        String subject = c.getString("subject");
        currCourse.setSection(section.toLowerCase());
        currCourse.setSemester(semester.toLowerCase());
        currCourse.setSubject(subject.toLowerCase());

        JSONArray times = c.getJSONArray("times");
        for (int j = 0; j < times.length(); j++) {
            JSONObject js = times.getJSONObject(j);
            String day = js.getString("day");

            String end_time = js.getString("end_time");
            LocalTime endLt = LocalTime.parse(end_time);
            Time et = Time.valueOf(endLt);

            String start_time = js.getString("start_time");
            LocalTime startLt = LocalTime.parse(start_time);
            Time st = Time.valueOf(startLt);

            MeetingTime mt = new MeetingTime(st, et, day);
            currCourse.getTimes().add(mt);
        }

        int total_seats = c.getInt("total_seats");
        currCourse.setTotalSeats(total_seats);
        return currCourse;
    }

    public Course createCourseFromCid(int cid) {
        // Make sure listings is initialized
        if (listings.isEmpty()) listings = parseJSON();
        // Make sure listings is not null
        if (listings == null) return null;
        // Find the matching course
        for (Course c : listings) {
            if (c.getCid() == cid) return c;
        }
        return null;
    }

    public static ArrayList<Course> parseJSON() {
        int id = 0;
        ArrayList<Course> courses = new ArrayList<>();
        String content = "";

        try {
            content = new String(Files.readAllBytes(Paths.get("data_wolfe.json")));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }

        JSONObject json = new JSONObject(content);
        JSONArray classes = json.getJSONArray("classes");

        for (int i = 0; i < classes.length(); i++) {
            JSONObject c = classes.getJSONObject(i);
            Course currCourse = createCourseFromJSON(c, id);
            courses.add(currCourse);
            id++;
        }
        return courses;
    }

    public static Time scanTime(String t) {
        Scanner scan = new Scanner(t);
        scan.useDelimiter(":");
        int hours = scan.nextInt();
        int mins = scan.nextInt();
        int secs = scan.nextInt();
        long ms = 0;
        ms += (3600000 * hours);
        ms += (60000 * mins);
        ms += (1000 * secs);
        Time time = new Time(ms);
        return time;
    }

    public Search(String query) {
        this.query = query;
        this.listings = parseJSON();
        this.searchResults = spellCheck(this.query);
    }

    public Search() {
        this.query = "";
        this.searchResults = parseJSON();
        //this.searchResults = new ArrayList<>(listings);
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public ArrayList<Course> getSearchResults() {
        return searchResults;
    }

    public ArrayList<Course> filter(Filter filter) {
        ArrayList<Course> filteredResults = new ArrayList<>();
        for (Course c : searchResults) {
            boolean addCourse = true;

            // Check if the professor filter is applied and if the course's professor is not in the filter
            if (!filter.getProf().isEmpty()) {
                boolean profFound = false;
                for (String prof : c.getProfessor()) {
                    if (filter.getProf().contains(prof)) {
                        profFound = true;
                        break;
                    }
                }
                if (!profFound) {
                    addCourse = false;
                }
            }

            // Checks if both department and course code filters are applied and if either does not match the course
            if (addCourse && filter.getDepartment() != null && !filter.getDepartment().isEmpty() && filter.getCourseCode() != 0) {
                if (!filter.getDepartment().equals(c.getSubject()) || filter.getCourseCode() != c.getCourseCode()) {
                    addCourse = false;
                }
            }

            // Check if the course name filter is applied and if the course's name does not match the filter
            if (addCourse && filter.getName() != null && filter.getName().isEmpty() && !c.getName().equals(filter.getName())) {
                addCourse = false;
            }

            // Check to make sure at least one of the filter's days are part of course's days
            if (addCourse && !filter.getDays().isEmpty()) {
                boolean dayFound = isDayFound(filter, c);
                if (!dayFound) {
                    addCourse = false;
                }
            }
            if(filter.getStartTime() != null && filter.getEndTime() != null){
                for(MeetingTime mt : c.getTimes()){
                    Time start = mt.getStartTime();
                    Time end = mt.getEndTime();
                    boolean tooEarly = start.before(filter.getStartTime()) || end.before(filter.getStartTime()) || end.equals(filter.getStartTime());
                    boolean tooLate = end.after(filter.getEndTime()) || start.after(filter.getEndTime()) || start.equals(filter.getEndTime());
                    if( tooEarly || tooLate){
                        addCourse = false;
                        break;
                    }
                }

            }
            if (addCourse && !filteredResults.contains(c)) {
                filteredResults.add(c);
            }
        }
        return filteredResults;
    }

    private static boolean isDayFound(Filter filter, Course c) {
        boolean dayFound = false;
        // Iterate through the days specified in the filter
        for (Day day : filter.getDays()) {
            // Iterate through the meeting times of the course
            for (MeetingTime mt : c.getTimes()) {
                // Check if the meeting time's day matches the filter's day
                if (mt.getDay().equals(day)) {
                    return true;
                }
            }
        }
        return false;
    }

    public ArrayList<Course> spellCheck(String s) {
        // If s is empty or null, return listings
        if (s == null || s.isEmpty()) {
            return listings;
        }
        // Create a list to store courses that match the spell check criteria
        ArrayList<Course> hits = new ArrayList<>();
        // Convert the input string to lowercase
        s = s.toLowerCase();
        // Initialize the longer string as the input string
        // Iterate through all courses in the listings
        for(Course c : listings){
            // If the keyword is a semester add classes offered in that semester
            if(s.equals(c.getSemester())){
                hits.add(c);
                continue;
            }
            // Get course name
            String courseName = c.getName();
            // If the courseName matches exactly add the course
            if (s.equals(courseName)){
                hits.add(c);
                continue;
            }
            // If the keyword is a professor name, add the courses that have that professor
            if(c.getProfessor().contains(s)){
                hits.add(c);
                continue;
            }

            // If the keyword is a department, add all the courses in that department
            if(s.equals(c.getSubject())){
                hits.add(c);
                continue;
            }

            // If the course name contains the string, add it
            if (courseName.contains(s)){
                hits.add(c);
                continue;
            }
            // See how different the strings are
            String shorter;
            String longer;
            if (s.length() < courseName.length()) {
                longer = courseName;
                shorter = s;
            }
            else {
                longer = s;
                shorter = courseName;
            }
            // Calculate the length of the longer string
            int longerLength = longer.length();
            // Calculate the difference ratio using edit distance
            double difference =  (longerLength - editDistance(longer, shorter)) / (double) longerLength;
            // If the difference ratio is greater than 0.4, add the course to the hits list
            if(difference > 0.5) {
                hits.add(c);
            }
            // If the keyword is a course code, add all the courses with that course code
            try{
                if(Integer.parseInt(s) == c.getCourseCode()){
                    hits.add(c);
                }
            } catch(NumberFormatException nfe){

            }

        }
        // Return the list of courses that match the spell check criteria
        return hits;
    }

    public static int editDistance(String s1, String s2){
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();
        int[] costs = new int[s2.length() + 1];
        for(int i = 0; i <= s1.length(); i++){
            int lastValue = i;
            for(int j = 0; j <= s2.length(); j++){
                if(i == 0){
                    costs[j] = j;
                } else{
                    if(j > 0){
                        int newValue = costs[j-1];
                        if(s1.charAt(i-1) != s2.charAt(j -1)){
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        }
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if(i > 0){
                costs[s2.length()] = lastValue;
            }
        }
        return costs[s2.length()];
    }
}