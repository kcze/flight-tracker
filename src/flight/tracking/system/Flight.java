package flight.tracking.system;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents and holds data of a single Flight.
 */
public class Flight extends Thread {
    
    private String number;
    private Aeroplane plane;
    private LocalDateTime dateTime;
    private FlightPlan flightPlan;
    private double distance;
    private Instant startTime;
    private boolean landed;

    /**
     * Flight constructor.
     */
    public Flight(String number, Aeroplane plane, LocalDateTime dateTime, FlightPlan flightPlan) {
        //todo checks
        this.number = number;
        this.plane = plane;
        this.dateTime = dateTime;
        this.flightPlan = flightPlan;
        this.distance = 0f;
        this.startTime = Instant.now();
        this.landed = false;
        start();
    }

    public void run() {
        while(!landed) {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Calculate delta time from the start time of the thread
            //todo temp speed up time and * 0.1
            long deltaTime = Duration.between(startTime, Instant.now()).toSeconds();
            // Calculate total distance travelled
            distance = deltaTime * 0.1 * plane.getSpeed();
            System.out.println(number + ": " + distance + "km");
            // Calculate current position
            ArrayList<Airport> towers = flightPlan.getAirports();
            for(int i = 0; i < towers.size(); i++) {
                // Check if reached the last tower
                if (i == towers.size() - 1) {
                    landed = true;
                    break;
                }
                double delta = towers.get(0).distanceTo(towers.get(1));
                distance -= delta;
                // If distance reached 0 or below that means the last crossed tower is at i index
                if (distance <= 0f) {
                    // Calculate current GPSCoordinate
                    double fraction = Math.abs(distance);
                    GPSCoordinate tower0Coords = towers.get(i).getCoordinate();
                    GPSCoordinate tower1Coords = towers.get(i+1).getCoordinate();
                    GPSCoordinate position = new GPSCoordinate(
                        (tower0Coords.getLatitude() - tower1Coords.getLatitude()) * fraction,
                        (tower0Coords.getLongitude() - tower1Coords.getLongitude()) * fraction
                    );
                    // abs(distance) is how much km is left to the next tower
                    // if it's less than delta/2 that means the tower i+1 is closer than tower i
                    if (Math.abs(distance) < delta / 2f) {
                        towers.get(i+1).updatePosition(this, position);
                        System.out.println(number + ": " + towers.get(i+1).getCode());
                    } else {
                        //todo notify tower i
                        towers.get(i).updatePosition(this, position);
                        System.out.println(number + ": " + towers.get(i).getCode());
                    }
                    // Break from the loop to skip other towers ahead
                    break;
                }
            }
        }
        
    }

    /**
     * Get two-letter Airline code of this flight.
     */
    public String getAirlineCode() {
        return number.substring(0, 2);
    }

    /**
     * Get distance travelled during this entire flight.
     */
    public double getDistance() {
        return flightPlan.calculateDistance();
    }

    /**
     * Get total fuel used on this flight.
     */
    public double getConsumption() {
        return (getDistance() / 100.0) * plane.getConsumption();
    }

    /**
     * Load all flights from a file.
     */
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
