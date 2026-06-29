package com.hotel.model;

public class Customer {

        private String name;
        private String phone;
        private int roomNumber;
        private long days;
        private String paymentMethod;
        public Customer(String name, String phone, int roomNumber,String paymentMethod, long days) {
            this.name = name;
            this.phone = phone;
            this.roomNumber = roomNumber;
            this.paymentMethod = paymentMethod;
            this.days = days;
        }
    public String getPaymentMethod() {
        return paymentMethod;
    }

    public long getDays() {
        return days;
    }

        public String getName() { return name; }
        public String getPhone() { return phone; }
        public int getRoomNumber() { return roomNumber; }
    }


