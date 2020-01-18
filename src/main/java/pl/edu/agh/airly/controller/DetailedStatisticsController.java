package pl.edu.agh.airly.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import org.apache.commons.lang3.tuple.Pair;
import pl.edu.agh.airly.model.*;

import java.util.List;
import java.util.stream.Collectors;


public class DetailedStatisticsController implements BasicStatisticsController {
    private AppController appController;
    private Monitor monitor;

    @FXML
    private TextArea statisticsText;

    @FXML
    private CheckBox top10InstaCheckBox;

    @FXML
    private ComboBox<Parameter> parameterComboBox1;

    @FXML
    private ComboBox<Parameter> parameterComboBox2;

    @FXML
    private ComboBox<Parameter> parameterComboBox3;

    @FXML
    private ComboBox<String> fromDateTimeComboBox;

    @FXML
    private ComboBox<String> tillDateTimeComboBox;

    @FXML
    private ComboBox<City> cityComboBox;

    @FXML
    private CheckBox hourCheckBox;

    @FXML
    private CheckBox top10CitiesCheckBox;

    @Override
    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    @Override
    public void setData(Monitor monitor) {
        this.monitor = monitor;
        System.out.println("HERE");
        cityComboBox.setItems(FXCollections.observableList(City.getAll().stream().collect(Collectors.toList())));
        parameterComboBox1.setItems(FXCollections.observableList(Parameter.getAll()
                .stream()
                .filter(parameter -> parameter.hasStandard())
                .collect(Collectors.toList())));
        parameterComboBox2.setItems(FXCollections.observableList(Parameter.getAll().stream().collect(Collectors.toList())));
        parameterComboBox3.setItems(FXCollections.observableList(Parameter.getAll().stream().collect(Collectors.toList())));
        fromDateTimeComboBox.setItems(FXCollections.observableList(monitor.getAllDates()));
        tillDateTimeComboBox.setItems(FXCollections.observableList(monitor.getAllDates()));

        cityComboBox.setVisible(false);
        parameterComboBox1.setVisible(false);
        parameterComboBox2.setVisible(false);
        parameterComboBox3.setVisible(false);
        fromDateTimeComboBox.setVisible(false);
        tillDateTimeComboBox.setVisible(false);


        top10InstaCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    parameterComboBox1.setVisible(true);
                    fromDateTimeComboBox.setVisible(true);
                    tillDateTimeComboBox.setVisible(true);
                    hourCheckBox.setSelected(false);
                    top10CitiesCheckBox.setSelected(false);
                    parameterComboBox2.setVisible(false);
                    parameterComboBox3.setVisible(false);
                    cityComboBox.setVisible(false);
                } else {
                    parameterComboBox1.setVisible(false);
                    fromDateTimeComboBox.setVisible(false);
                    tillDateTimeComboBox.setVisible(false);
                }
            }
        });

        hourCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    parameterComboBox2.setVisible(false);
                    top10CitiesCheckBox.setSelected(false);
                    top10InstaCheckBox.setSelected(false);
                    parameterComboBox1.setVisible(false);
                    parameterComboBox3.setVisible(true);
                    cityComboBox.setVisible(true);
                } else {
                    parameterComboBox3.setVisible(false);
                    cityComboBox.setVisible(false);
                }
            }
        });

        top10CitiesCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    parameterComboBox1.setVisible(false);
                    fromDateTimeComboBox.setVisible(false);
                    tillDateTimeComboBox.setVisible(false);
                    hourCheckBox.setSelected(false);
                    top10InstaCheckBox.setSelected(false);
                    parameterComboBox2.setVisible(true);
                    parameterComboBox3.setVisible(false);
                    cityComboBox.setVisible(false);
                } else {
                    parameterComboBox2.setVisible(false);
                }
            }
        });

    }


    @FXML
    protected void handleShowAction(ActionEvent event) {
        if (top10InstaCheckBox.isSelected()) {
            Parameter parameter = parameterComboBox1.getSelectionModel().getSelectedItem();
            String fromDateTime = fromDateTimeComboBox.getSelectionModel().getSelectedItem();
            String tillDateTime = tillDateTimeComboBox.getSelectionModel().getSelectedItem();
            if (parameter != null && fromDateTime != null && tillDateTime != null) {
                List<Pair<Installation, Measurement>> topInstallations = monitor.findInstallationsWithValuesAboveStandard(parameter, fromDateTime, tillDateTime);
                StringBuilder stringBuilder = new StringBuilder();
                topInstallations
                        .forEach(inst ->
                            stringBuilder.append(inst.getLeft()+" "+inst.getRight()+"\n")
                        );
                System.out.println(stringBuilder.toString());
                statisticsText.setText(stringBuilder.toString());
            }
        } else if (top10CitiesCheckBox.isSelected()) {
            Parameter parameter = parameterComboBox2.getSelectionModel().getSelectedItem();
            if (parameter != null) {
                List<Pair<City, Double>> topCities = monitor.findCitiesWithHighestAverageValues(parameter);
                StringBuilder stringBuilder = new StringBuilder();
                topCities
                        .forEach(city ->
                                stringBuilder.append(city.getLeft()+" "+city.getRight()+"\n"));
                System.out.println(stringBuilder.toString());
                statisticsText.setText(stringBuilder.toString());
            }
        } else if (hourCheckBox.isSelected()) {
            Parameter parameter = parameterComboBox3.getSelectionModel().getSelectedItem();
            City city = cityComboBox.getSelectionModel().getSelectedItem();

            if (city != null && parameter != null) {
                Pair<Integer, Double> hourAndValue = monitor.getHourWithTheHighestAverage(city, parameter);
                System.out.println(hourAndValue.getLeft()+" "+hourAndValue.getRight());
                statisticsText.setText(hourAndValue.getLeft()+" "+hourAndValue.getRight());
            }
        }
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
