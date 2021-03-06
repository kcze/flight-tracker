package flighttrack;

import java.util.ArrayList;
import java.util.List;

/**
 * Airports (Towers) to be crossed in one Flight.
*/
public class FlightPlan {
    
    private ArrayList<Airport> airports;

    /**
     * FlightPlan constructor
     * @param departure First airport
     * @param destination Last airport
     * @param airports All airports (should include departure and destination)
     * @throws IllegalArgumentException
     */
    public FlightPlan(Airport departure, Airport destination, List<Airport> airports) throws IllegalArgumentException {
        this.airports = new ArrayList<Airport>(airports);

        if (airports.size() < 2) {
            throw new IllegalArgumentException("Flight need to cross at least two towers.");
        } else if (airports.size() > 20) {
            throw new IllegalArgumentException("Flight cannot cross more than 20 towers.");
        } else if (departure != getDeparture() || destination != getDestination()) {
            throw new IllegalArgumentException("Departure or destination airport differ from towers crossed.");
        }
    }

    /**
     * Get all airports (control towers) of this flight.
     * @return
     */
    public ArrayList<Airport> getAirports() {
        return airports;
    }

    /**
     * Get the first Airport in the Flight.
     */
    public Airport getDeparture() {
        return airports.get(0);
    }

    /**
     * Get the last Airport in the Flight.
     */
    public Airport getDestination() {
        return airports.get(airports.size() - 1);
    }

    /**
     * Calculate distance travelled during this entire flight.
     */
    public double calculateDistance() {
        double distance = 0.0;
        for(int i = 0; i < airports.size() - 1; i++) {
            distance += airports.get(i).distanceTo(airports.get(i + 1));
        }
        return distance;
    }
}
