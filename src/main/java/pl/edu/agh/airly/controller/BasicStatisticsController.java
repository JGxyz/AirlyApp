package pl.edu.agh.airly.controller;

import pl.edu.agh.airly.model.Monitor;

public interface BasicStatisticsController {
    void setAppController(AppController appController);

    void setData(Monitor monitor);
}
