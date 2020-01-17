package pl.edu.agh.airly.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import pl.edu.agh.airly.model.Monitor;


public class DetailedStatisticsController implements BasicStatisticsController {
    private AppController appController;
    private Monitor monitor;

    @Override
    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    @Override
    public void setData(Monitor monitor) {
        this.monitor = monitor;
    }

    @FXML
    protected void handleExitAction(ActionEvent e) {
        appController.closeView();
    }

    @FXML
    protected void handleGeneralStatisticsAction(ActionEvent e) {
        appController.showView("/view/MainView.fxml");
    }
}
