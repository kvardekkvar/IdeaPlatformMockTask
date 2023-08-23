package org.example;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import lombok.SneakyThrows;
import org.example.models.JsonData;

import java.io.FileReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {
    @SneakyThrows
    public static void main(String[] args) {


        Gson gson = new Gson();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        String filename = classloader.getResource("tickets.json").getFile();
        JsonReader reader = new JsonReader(new FileReader(filename));
        JsonData data = gson.fromJson(reader, JsonData.class);

        List<JsonData.Ticket> allFlights = data.getTickets();
        List<JsonData.Ticket> flightsBetweenVladivostokAndTelAviv = filterFlightsByOriginAndDestination(allFlights, "Владивосток", "Тель-Авив");

        List<String> carriers = flightsBetweenVladivostokAndTelAviv.stream().map(JsonData.Ticket::getCarrier).distinct().toList();

        TimeZone timeZoneTelAviv = TimeZone.getTimeZone("Asia/Jerusalem");
        TimeZone timeZoneVladivostok = TimeZone.getTimeZone("Asia/Vladivostok");
        long timeDifferenceMillis = timeZoneVladivostok.getRawOffset() - timeZoneTelAviv.getRawOffset() + timeZoneVladivostok.getDSTSavings() - timeZoneTelAviv.getDSTSavings();


        System.out.println("The shortest flight from Vladivostok to Telaviv:");
        carriers.forEach(
                carrier -> {

                    Duration shortestDuration = calculateMinimalFlightTimeForCarrier(flightsBetweenVladivostokAndTelAviv, carrier);

                    System.out.printf(
                            " for carrier %s:\n " +
                                    "%s [if timezones are not counted, the answer would be %s] \n", carrier,
                            convertDurationToHumanReadableTime(shortestDuration.plusMillis(timeDifferenceMillis)),
                            convertDurationToHumanReadableTime(shortestDuration)
                    );

                }
        );
        System.out.println("-------------------------------------");
        System.out.printf("The average price among flights from Vladivostok to Telaviv is greater than median price by: %s \n", calculateAveragePrice(flightsBetweenVladivostokAndTelAviv) - calculateMedianPrice(flightsBetweenVladivostokAndTelAviv));
    }


    public static long convertTimeToEpochSeconds(String date, String timeWithoutSeconds) {
        return parseDateTimeFromStrings(date, timeWithoutSeconds).toEpochSecond(ZoneOffset.UTC);
    }

    public static LocalDateTime parseDateTimeFromStrings(String date, String timeWithoutSeconds) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy H:mm");
        return LocalDateTime.parse(date + " " + timeWithoutSeconds, formatter);
    }

    public static Duration calculateMinimalFlightTimeForCarrier(List<JsonData.Ticket> tickets, String carrier) {
        tickets = filterFlightsByCarrier(tickets, carrier);

        Optional<JsonData.Ticket> minFlight = tickets.stream().min(
                Comparator.comparing(x -> Math.toIntExact(convertTimeToEpochSeconds(x.getArrival_date(), x.getArrival_time()) - convertTimeToEpochSeconds(x.getDeparture_date(), x.getDeparture_time())))
        );

        if (minFlight.isEmpty()) {
            return null;
        } else {
            JsonData.Ticket flight = minFlight.get();
            LocalDateTime startDateTime = parseDateTimeFromStrings(flight.getDeparture_date(), flight.getDeparture_time());
            LocalDateTime endDateTime = parseDateTimeFromStrings(flight.getArrival_date(), flight.getArrival_time());

            Duration diff = Duration.between(startDateTime, endDateTime);
            return diff;
        }
    }

    public static double calculateAveragePrice(List<JsonData.Ticket> flights) {
        return flights.stream().reduce(0, (x, y) -> x + y.getPrice(), Integer::sum) / (double) flights.size();
    }

    public static double calculateMedianPrice(List<JsonData.Ticket> flights) {
        List<JsonData.Ticket> sortedFlights = flights.stream().sorted(Comparator.comparing(JsonData.Ticket::getPrice)).toList();
        if (sortedFlights.size() % 2 == 1) {
            return sortedFlights.get(sortedFlights.size() / 2).getPrice();
        } else {
            return (sortedFlights.get((sortedFlights.size() - 1) / 2).getPrice() + sortedFlights.get(sortedFlights.size() / 2).getPrice()) / 2.0;
        }
    }


    public static String convertDurationToHumanReadableTime(Duration diff) {
        return String.format("%d days %d hours %02d minutes %02d seconds",
                diff.toDays(),
                diff.toHoursPart(),
                diff.toMinutesPart(),
                diff.toSecondsPart());
    }

    public static List<JsonData.Ticket> filterFlightsByCarrier(List<JsonData.Ticket> flights, String carrier) {
        return flights.stream().filter(x -> Objects.equals(x.getCarrier(), carrier)).toList();
    }

    public static List<JsonData.Ticket> filterFlightsByOriginAndDestination(List<JsonData.Ticket> flights, String origin, String destination) {
        return flights.stream().filter(x -> Objects.equals(x.getDestination_name(), destination) && Objects.equals(x.getOrigin_name(), origin)).toList();
    }

}