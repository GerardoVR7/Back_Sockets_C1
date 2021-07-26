module ServerPrincipal {
    requires javafx.controls;
    requires javafx.fxml;

    opens Main;
    opens Main.controller;
}