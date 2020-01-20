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

import java.util.ArrayList;
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
        cityComboBox.setItems(FXCollections.observableList(new ArrayList<>(City.getAll())));
        parameterComboBox1.setItems(FXCollections.observableList(Parameter.getAll()
                .stream()
                .filter(Parameter::hasStandard)
                .collect(Collectors.toList())));
        parameterComboBox2.setItems(FXCollections.observableList(new ArrayList<>(Parameter.getAll())));
        parameterComboBox3.setItems(FXCollections.observableList(new ArrayList<>(Parameter.getAll())));
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
            if (parameter != null && fromDateTime != null && tillDateTime != null && CharSequence.compare(fromDateTime, tillDateTime) < 0) {
                List<Pair<Installation, Measurement>> topInstallations = monitor.findInstallationsWithValuesAboveStandard(parameter, fromDateTime, tillDateTime);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("PARAMETER: " + parameter.getName() + "\nSTANDARD: " + parameter.getStandard() + "\n\n");
                statisticsText.setText(stringBuilder.toString());
                topInstallations
                        .forEach(inst ->
                                stringBuilder.append(inst.getLeft().showInStatistics()).append(inst.getRight().showInStatistics())
                        );
                System.out.println(stringBuilder.toString());
                statisticsText.setText(stringBuilder.toString());
            }
        } else if (top10CitiesCheckBox.isSelected()) {
            Parameter parameter = parameterComboBox2.getSelectionModel().getSelectedItem();
            if (parameter != null) {
                List<Pair<City, Double>> topCities = monitor.findCitiesWithHighestAverageValues(parameter);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("PARAMETER: ").append(parameter.getName()).append("\nSTANDARD: ").append(parameter.getStandard()).append("\n\n");
                topCities
                        .forEach(city ->
                                stringBuilder.append(city.getLeft()).append(" - ").append(String.format("%.2f\n", city.getRight())));
                System.out.println(stringBuilder.toString());
                statisticsText.setText(stringBuilder.toString());
            }
        } else if (hourCheckBox.isSelected()) {
            Parameter parameter = parameterComboBox3.getSelectionModel().getSelectedItem();
            City city = cityComboBox.getSelectionModel().getSelectedItem();

            if (city != null && parameter != null) {
                Pair<Integer, Double> hourAndValue = monitor.getHourWithTheHighestAverage(city, parameter);
                String result;
                if (parameter.hasStandard()) {
                    result = String.format("CITY: %s\nPARAMETER: %s\nSTANDARD: %.2f\n\n" +
                            "%d:00 - %.2f", city, parameter.getName(), parameter.getStandard(), hourAndValue.getLeft(), hourAndValue.getRight());
                } else {
                    result = String.format("CITY: %s\nPARAMETER: %s\n\n" +
                            "%d:00 - %.2f", city, parameter.getName(), hourAndValue.getLeft(), hourAndValue.getRight());
                }

                System.out.println(result);
                statisticsText.setText(result);
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
