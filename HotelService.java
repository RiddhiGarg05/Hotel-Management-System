package com.hotel.service;

import com.hotel.model.Room;
import com.hotel.model.Customer;
import com.hotel.db.DBConnection;

import java.sql.*;
import java.util.*;

public class HotelService {


    public void resetOnStartup() {
       String resetRooms    = "UPDATE rooms SET available = 1";
        String clearCustomers = "DELETE FROM customers";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps1 = con.prepareStatement(resetRooms);
             PreparedStatement ps2 = con.prepareStatement(clearCustomers)) {

            ps1.executeUpdate();
            ps2.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void addRoom(Room room) {
        String sql = "INSERT INTO rooms VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, room.getRoomNumber());
            ps.setString(2, room.getType());
            ps.setDouble(3, room.getPrice());
            ps.setInt(4, room.isAvailable() ? 1 : 0);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean roomExists(int roomNumber) {
        String sql = "SELECT COUNT(*) FROM rooms WHERE room_number = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, roomNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isRoomAvailable(int roomNumber) {
        String sql = "SELECT available FROM rooms WHERE room_number = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, roomNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("available") == 1;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Room> getAllRoomsFromDB() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Room(
                        rs.getInt("room_number"),
                        rs.getString("type"),
                        rs.getDouble("price"),
                        rs.getInt("available") == 1
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    public List<Room> getRooms() {
        return getAllRoomsFromDB();
    }


    public boolean bookRoom(Customer customer) {
        try (Connection con = DBConnection.getConnection()) {

            String check = "SELECT available FROM rooms WHERE room_number=?";
            PreparedStatement ps1 = con.prepareStatement(check);
            ps1.setInt(1, customer.getRoomNumber());
            ResultSet rs = ps1.executeQuery();

            if (rs.next() && rs.getInt("available") == 0) return false;

            String update = "UPDATE rooms SET available=0 WHERE room_number=?";
            PreparedStatement ps2 = con.prepareStatement(update);
            ps2.setInt(1, customer.getRoomNumber());
            ps2.executeUpdate();

            String insert = "INSERT INTO customers (name, phone, room_number, days, payment_method) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps3 = con.prepareStatement(insert);
            ps3.setString(1, customer.getName());
            ps3.setString(2, customer.getPhone());
            ps3.setInt(3, customer.getRoomNumber());
            ps3.setLong(4, customer.getDays());
            ps3.setString(5, customer.getPaymentMethod());
            ps3.executeUpdate();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public Customer getCustomerByRoom(int roomNumber) {
        String sql = "SELECT * FROM customers WHERE room_number=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, roomNumber);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Customer(
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getInt("room_number"),
                        rs.getString("payment_method"),
                        rs.getLong("days")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public double checkout(int roomNumber) {
        try (Connection con = DBConnection.getConnection()) {

            Customer c = getCustomerByRoom(roomNumber);
            if (c == null) return -1;

            String roomSql = "SELECT price FROM rooms WHERE room_number=?";
            PreparedStatement ps1 = con.prepareStatement(roomSql);
            ps1.setInt(1, roomNumber);
            ResultSet rs = ps1.executeQuery();
            if (!rs.next()) return -1;

            double price = rs.getDouble("price");
            double bill  = price * c.getDays();

            String update = "UPDATE rooms SET available=1 WHERE room_number=?";
            PreparedStatement ps2 = con.prepareStatement(update);
            ps2.setInt(1, roomNumber);
            ps2.executeUpdate();

            String delete = "DELETE FROM customers WHERE room_number=?";
            PreparedStatement ps3 = con.prepareStatement(delete);
            ps3.setInt(1, roomNumber);
            ps3.executeUpdate();

            return bill;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}