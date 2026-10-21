package org.example;

import javafx.application.Application;
import org.example.Views.ContainerApp;

/** Ponto de entrada (separado da classe Application para facilitar empacotamento). */
public class Main {
    public static void main(String[] args) {
        Application.launch(ContainerApp.class, args);
    }
}
