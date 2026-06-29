package com.hotel.controller;

import com.hotel.model.Room;
import com.hotel.service.HotelService;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.*;
import com.hotel.model.Customer;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class RoomController {
    @FXML private DatePicker checkInDatePicker;
    @FXML private DatePicker checkOutDatePicker;
    @FXML private TextField roomNoField;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> typeBox;
    @FXML private Label messageLabel;
    @FXML private Label totalRoomsLabel;
    @FXML private Label availableRoomsLabel;
    @FXML private Label bookedRoomsLabel;
    @FXML private ComboBox<String> paymentBox;
    @FXML private Label addRoomMessageLabel;
    @FXML private Label bookingMessageLabel;
    @FXML private Label checkoutMessageLabel;
    @FXML private Label viewMessageLabel;
    @FXML private TableView<Room> roomTable;
    @FXML private TableColumn<Room, Integer> colRoomNo;
    @FXML private TableColumn<Room, String> colType;
    @FXML private TableColumn<Room, Double> colPrice;
    @FXML private TableColumn<Room, Boolean> colStatus;
    @FXML private TextField custNameField;
    @FXML private TextField phoneField;
    @FXML private TextField bookRoomField;
    @FXML private TextField checkoutRoomField;
    private int pendingRoomNo = -1;
    private HotelService service = new HotelService();

    @FXML
    public void initialize() {
        typeBox.getItems().addAll("Single", "Double", "Deluxe");
        paymentBox.getItems().addAll("Cash", "Card", "UPI");
        colRoomNo.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getRoomNumber()).asObject());

        colType.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getType()));

        colPrice.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getPrice()).asObject());

        colStatus.setCellValueFactory(data ->
                new SimpleBooleanProperty(data.getValue().isAvailable()));
        colStatus.setCellFactory(column -> new TableCell<Room, Boolean>() {
            @Override
            protected void updateItem(Boolean available, boolean empty) {
                super.updateItem(available, empty);
                if (empty || available == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(available ? "Available" : "Booked");
                    setStyle("-fx-text-fill: " + (available ? "green;" : "red;") +
                            "-fx-font-weight: bold;");
                }
            }
        }
        );

        loadRooms();
        updateDashboard();
    }
    private void updateDashboard() {

        List<Room> rooms = service.getAllRoomsFromDB();

        int total = rooms.size();

        long available = rooms.stream()
                .filter(Room::isAvailable)
                .count();

        int booked = total - (int) available;

        totalRoomsLabel.setText(String.valueOf(total));
        availableRoomsLabel.setText(String.valueOf(available));
        bookedRoomsLabel.setText(String.valueOf(booked));

    }

    @FXML
    private void handleAddRoom() {
        if (!validateAddRoomForm()) return;
        try {
            int roomNo = Integer.parseInt(roomNoField.getText()); //
            double price = Double.parseDouble(priceField.getText());
            if (roomNo <= 0) {
                addRoomMessageLabel.setText("Room number must be > 0!");
                return;
            }

            if (roomNo <= 0) {
                addRoomMessageLabel.setText("Room number must be > 0!");
                return;
            }
            if (service.roomExists(roomNo)) {
                addRoomMessageLabel.setText("Room already exists!");
                return;
            }
            if (price <= 0) {
                addRoomMessageLabel.setText("Price must be greater than 0!");
                priceField.setStyle("-fx-border-color: red;");
                return;
            } else {
                priceField.setStyle("");
            }
            String type = typeBox.getValue();

            if (type == null) {
                messageLabel.setText("Please select room type!");
                return;
            }

            service.addRoom(new Room(roomNo, type, price, true));

            addRoomMessageLabel.setText("Room added!");
            updateDashboard();
            roomNoField.clear();
            priceField.clear();
            typeBox.setValue(null);

        } catch (NumberFormatException e) {
            addRoomMessageLabel.setText("Room number and price must be numeric!");
        }
    }


        @FXML
        private void handleShowRooms() {
            loadRooms();
        }


    @FXML
    private void handleBooking() {
        try {
            String name = custNameField.getText();
            String phone = phoneField.getText();
            if (!phone.matches("\\d{10}")) {
                phoneField.setStyle("-fx-border-color: red;");
                bookingMessageLabel.setText("Invalid phone number!");
                return;
            } else {
                phoneField.setStyle("");
            }
            int roomNo = Integer.parseInt(bookRoomField.getText());
            LocalDate checkIn = checkInDatePicker.getValue();
            LocalDate checkOut = checkOutDatePicker.getValue();

            if (name == null || name.trim().isEmpty()) {
                bookingMessageLabel.setText("Name cannot be empty!");
                custNameField.setStyle("-fx-border-color: red;");
                return;
            }

            if (!name.matches("[a-zA-Z ]+")) {
                bookingMessageLabel.setText("Name must contain only letters!");
                custNameField.setStyle("-fx-border-color: red;");
                return;
            } else {
                custNameField.setStyle("");
            }


            if (checkIn == null || checkOut == null) {
                bookingMessageLabel.setText("Select both dates!");
                return;
            }


            if (!checkOut.isAfter(checkIn)) {
                bookingMessageLabel.setText("Checkout must be after Check-in!");
                checkOutDatePicker.setStyle("-fx-border-color: red;");
                return;
            } else {
                checkOutDatePicker.setStyle("");
            }

            if (checkIn == null || checkOut == null) {
                messageLabel.setText("Select dates!");
                return;
            }
            long days = ChronoUnit.DAYS.between(checkIn, checkOut);

            if (days <= 0) {
                messageLabel.setText("Invalid date selection!");
                return;
            }
            String paymentMethod = paymentBox.getValue();
            if (paymentMethod == null) {
                messageLabel.setText("Select payment method!");
                return;
            }



            if (!service.roomExists(roomNo)) {
                bookingMessageLabel.setText("Room does not exist!");
                return;
            }


            if (!service.isRoomAvailable(roomNo)) {
                bookingMessageLabel.setText("Room already booked!");
                return;
            }

            boolean success = service.bookRoom(
                    new Customer(name, phone, roomNo,paymentMethod,days)
            );


            if (success) {
                bookingMessageLabel.setText("Room booked!");
                refreshTable();
                updateDashboard();
                }
             else {
                bookingMessageLabel.setText("Room not available!");
            }


        } catch (Exception e) {
            bookingMessageLabel.setText("Invalid input!");
        }
    }
    private void refreshTable() {
        loadRooms();
    }



    @FXML
    private void handleCheckout() {
        try {
            int roomNo = Integer.parseInt(checkoutRoomField.getText());

            Customer c = service.getCustomerByRoom(roomNo);

            if (!service.roomExists(roomNo)) {
                checkoutMessageLabel.setText("Invalid room number!");
                return;
            }
            if (c == null) {
                checkoutMessageLabel.setText("No booking found!");
                return;
            }

            long days = c.getDays();
            double bill = service.checkout(roomNo);

            if (bill != -1) {
                checkoutMessageLabel.setText("Checkout done!");
                Room foundRoom = null;

                for (Room r : service.getRooms()) {
                    if (r.getRoomNumber() == roomNo) {
                        foundRoom = r;
                        break;
                    }
                }

                double price = foundRoom.getPrice();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Bill");
                alert.setHeaderText("Payment Details");
                String billDetails =
                        "Room No: " + roomNo + "\n" +
                                "Price per day: ₹" + price + "\n" +
                                "Days: " + days + "\n" +
                                "-------------------\n" +
                                "Total: ₹" + bill;

                alert.setContentText(billDetails);
                alert.showAndWait();
                updateDashboard();
                refreshTable();
            } else {
                checkoutMessageLabel.setText("Invalid checkout!");
            }

        } catch (NumberFormatException e) {
            checkoutMessageLabel.setText("Room number and days must be numeric!");
        }
    }
    private boolean validateAddRoomForm() {
        boolean valid = true;

        if (roomNoField.getText().trim().isEmpty()) {
            roomNoField.setStyle("-fx-border-color: red;");
            valid = false;
        } else {
            roomNoField.setStyle("");
        }

        if (priceField.getText().trim().isEmpty()) {
            priceField.setStyle("-fx-border-color: red;");
            valid = false;
        } else {
            priceField.setStyle("");
        }

        if (typeBox.getValue() == null) {
            typeBox.setStyle("-fx-border-color: red;");
            valid = false;
        } else {
            typeBox.setStyle("");
        }

        if (!valid) {
            messageLabel.setText("Please fill all required fields!");
        }

        return valid;
    }
    @FXML
    private void handleConfirmCheckout() {

        if (pendingRoomNo == -1) {
            checkoutMessageLabel.setText("Generate invoice first!");
            return;
        }

        double bill = service.checkout(pendingRoomNo);

        if (bill != -1) {
            checkoutMessageLabel.setText("Checkout completed!");

            pendingRoomNo = -1;
            updateDashboard();
            refreshTable();
        } else {
            checkoutMessageLabel.setText("Error in checkout!");
        }
    }
    @FXML
    private void handleGenerateInvoice() {
        try {
            int roomNo = Integer.parseInt(checkoutRoomField.getText());

            Customer c = service.getCustomerByRoom(roomNo);

            if (c == null) {
                checkoutMessageLabel.setText("No booking found!");
                return;
            }

            long days = c.getDays();

            Room foundRoom = null;
            for (Room r : service.getRooms()) {
                if (r.getRoomNumber() == roomNo) {
                    foundRoom = r;
                    break;
                }
            }

            if (foundRoom == null) {
                checkoutMessageLabel.setText("Room not found!");
                return;
            }

            double price = foundRoom.getPrice();
            double bill = price * days;


            pendingRoomNo = roomNo;


            String invoice = String.format("""
        ===== HOTEL INVOICE =====
        Name: %s
        Phone: %s
        Room No: %d
        ------------------------
        Price/Day: ₹%.2f
        Days: %d
        ------------------------
        TOTAL: ₹%.2f
        Payment: %s
        ========================
        """,
                    c.getName(),
                    c.getPhone(),
                    roomNo,
                    price,
                    days,
                    bill,
                    c.getPaymentMethod()
            );


            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Invoice");
            alert.setHeaderText("Review Invoice");
            alert.setContentText(invoice);
            alert.showAndWait();

            checkoutMessageLabel.setText("Click 'Confirm Checkout' to complete.");

        } catch (Exception e) {
            checkoutMessageLabel.setText("Invalid input!");
        }
    }
    private void loadRooms() {
        roomTable.getItems().clear();
        roomTable.setItems(
                FXCollections.observableArrayList(service.getAllRoomsFromDB())
        );
    }

}
