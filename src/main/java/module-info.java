module Jarkanoid {

  requires slf4j.api;
  requires java.desktop;

  requires javafx.fxml;
  requires javafx.controls;
  requires javafx.graphics;
  requires javafx.media;

  opens fko.jarkanoid;
  opens fko.jarkanoid.controller;

}
