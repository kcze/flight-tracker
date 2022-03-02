package tracker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.DataLine;

/**Represents and holds data of a single Flight. */
public class Flight {
    
    private String number;
    private Aeroplane plane;
    private LocalDateTime dateTime;
    private FlightPlan flightPlan;

    /**Default constructor. */
    public Flight(String number, Aeroplane plane, LocalDateTime dateTime, FlightPlan flightPlan) {
        //todo checks
        this.number = number;
        this.plane = plane;
        this.dateTime = dateTime;
        this.flightPlan = flightPlan;
    }

    /**Load all flights from a file. */
    public static ArrayList<Flight> loadFlights(FlightManager manager) {
        ArrayList<Flight> flights = new ArrayList<Flight>();
        List<String> lines = new ArrayList<String>();

        // Read file
        try {
            lines = Files.readAllLines(Paths.get("data/flights.txt"));
        } catch (IOException e) {
            System.out.println("Could not read flights file.");
            return flights;
        }

        // Create Flight from each line
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd:MM:yyyy HH:mm");
        for (String line : lines) {
            try {
                String[] flight = line.split(";");
                String number = flight[0].trim();
                Aeroplane plane = manager.getAeroplane(flight[1].trim());
                Airport departure = manager.getAirport(flight[2].trim());
                Airport destination = manager.getAirport(flight[3].trim());
                LocalDateTime dateTime = LocalDateTime.parse(flight[4].trim() + " " + flight[5].trim(),
                    format);
                ArrayList<Airport> towers = new ArrayList<Airport>();
                for(int i = 6; i < flight.length; i++) {
                    towers.add(manager.getAirport(flight[i].trim()));
                }
                FlightPlan plan = new FlightPlan(departure, destination, towers);
                flights.add(new Flight(number, plane, dateTime, plan));
                
            } catch (Exception e) {
                System.out.println("Could not parse airport data: " +
                    e.getMessage());
            }
        }

        return flights;
    }
}