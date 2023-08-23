package org.example.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class JsonData {
    private ArrayList<Ticket> tickets;

    @Getter
    @Setter
    public static class Ticket {
        private String origin;
        private String origin_name;
        private String destination;
        private String destination_name;
        private String departure_date;
        private String departure_time;
        private String arrival_date;
        private String arrival_time;
        private String carrier;
        private int stops;
        private int price;
    }
}

