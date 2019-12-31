package pl.edu.agh.airly.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import org.apache.spark.api.java.JavaSparkContext;
import pl.edu.agh.airly.comparator.MeasurementDateComparator;
import pl.edu.agh.airly.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GeneralStatisticsController {
    private AppController appController;
    private Monitor monitor;
    private Installation currentInstallation;
    private City currentCity;
    private Parameter currentParameter;
    private ObservableList<String> dates;
    private String currentTillDateTime;
    private String currentFromDateTime;

    @FXML
    private ComboBox<City> cityComboBox;

    @FXML
    private ComboBox<Installation> installationComboBox;

    @FXML
    private ComboBox<Parameter> parameterComboBox;

    @FXML
    private ComboBox<String> fromDateTime;

    @FXML
    private ComboBox<String> tillDateTime;

    @FXML
    private LineChart<?, ?> lineChart;

    @FXML
    private CategoryAxis categoryAxis;

    @FXML
    private NumberAxis numberAxis;

    public void setData(JavaSparkContext sparkContext) {
        cityComboBox.setItems(FXCollections.observableList(City.getAll().stream().collect(Collectors.toList())));
        this.monitor = new Monitor(sparkContext);
        monitor.readInstallationAndDownloadMeasurements();
        monitor.readMeasuremensts();

        cityComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<City>() {
            @Override
            public void changed(ObservableValue<? extends City> observable,
                                final City oldValue, final City newValue) {
                cityChanged(newValue);
            }
        });

        installationComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Installation>() {
            @Override
            public void changed(ObservableValue<? extends Installation> observable,
                                final Installation oldValue, final Installation newValue) {
                installationChanged(newValue);
            }
        });

        parameterComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Parameter>() {
            @Override
            public void changed(ObservableValue<? extends Parameter> observable, Parameter oldValue, Parameter newValue) {
                parameterChanged(oldValue, newValue);
            }
        });

        fromDateTime.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                fromDateTimeChanged(newValue);
            }
        });

        tillDateTime.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                tillDateTimeChanged(newValue);
            }
        });

    }

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    private void cityChanged(City city) {
        if (city == null) return;
        currentCity = city;
        System.out.println("SELECTED CITY: "+city);
        List<Installation> installations = monitor.getInstallationsFromCity(city);
        installationComboBox.setItems(FXCollections.observableList(installations));
    }

    private void installationChanged(Installation installation) {
        if (installation == null) return;
        System.out.println("SELECTED INSTALLATION: "+installation);
        Optional<List<Parameter>> parameters = monitor.getParametersFromInstallation(installation);
        if (parameters.isPresent()) {
            parameterComboBox.setItems(FXCollections.observableList(parameters.get()));
            this.currentInstallation = installation;
            List<String> setDates = monitor.getDistinctDatesFromInstallation(installation);
            if (setDates != null) {
                fromDateTime.setItems(FXCollections.observableList(setDates));
                tillDateTime.setItems(FXCollections.observableList(setDates));
            }
        } else {
            parameterComboBox.setItems(FXCollections.emptyObservableList());
        }

        XYChart.Series[] allSeries = lineChart.getData().toArray(new XYChart.Series[0]);

        for (XYChart.Series series : allSeries)
            lineChart.getData().remove(series);
    }

    private void fromDateTimeChanged(String fromDateTime) {
        if (fromDateTime == null) return;
        currentFromDateTime = fromDateTime;
        parameterChanged(currentParameter, currentParameter);
    }

    private void tillDateTimeChanged(String tillDateTime) {
        if (tillDateTime == null) return;
        currentTillDateTime = tillDateTime;
        parameterChanged(currentParameter, currentParameter);
    }


    private void parameterChanged(Parameter oldParam, Parameter parameter) {
        if (parameter == null) return;
        if (currentParameter == null) currentParameter = parameter;
        System.out.println("SELECTED PARAMETER: "+parameter);

        XYChart.Series[] allSeries = lineChart.getData().toArray(new XYChart.Series[0]);

        for (XYChart.Series series : allSeries)
            lineChart.getData().removeAll(series);

        List<Measurement> measurements = new ArrayList(monitor.getParameterMeasurementsFromInstallation(parameter, currentInstallation));
        measurements.forEach(System.out::println);
        Collections.sort(measurements, new MeasurementDateComparator());
        List<String> newDates = measurements.stream().map(measurement -> measurement.getFromDateTime()).collect(Collectors.toList());

        if (currentFromDateTime != null)
            System.out.println("F: "+currentFromDateTime);

        if (currentTillDateTime != null)
            System.out.println("T: "+currentTillDateTime);

        if (currentFromDateTime != null) {
            if (currentTillDateTime != null) {
                if (CharSequence.compare(currentFromDateTime, currentTillDateTime) < 0) {
                    newDates = newDates.stream()
                            .filter(date -> CharSequence.compare(date, currentFromDateTime) >= 0 && CharSequence.compare(date, currentTillDateTime) <= 0)
                            .collect(Collectors.toList()); }
            } else {
                newDates = newDates.stream()
                        .filter(date -> CharSequence.compare(date, currentFromDateTime) >= 0)
                        .collect(Collectors.toList()); }
        } else if (currentTillDateTime != null) {
            newDates = newDates.stream()
                    .filter(date -> CharSequence.compare(date, currentTillDateTime) <= 0)
                    .collect(Collectors.toList());
        }

        newDates.forEach(System.out::println);

        if (dates == null)
            dates = FXCollections.observableList(newDates);
        else {
            while (dates.size() > 0)
                dates.remove(0);
            newDates.forEach(date -> {
                dates.add(date);
            });
        }

        categoryAxis.setCategories(dates);

        categoryAxis.setTickLabelRotation(-45);
        numberAxis.setTickLabelRotation(-45);

        XYChart.Series series = new XYChart.Series();

        series.setName(parameter.getName());

        String firstDate = newDates.get(0);
        String lastDate = newDates.get(newDates.size()-1);

       measurements.forEach(measurement -> {
            Double value = measurement.getValue();
            String time = measurement.getFromDateTime();
            if (CharSequence.compare(time, firstDate)!=-1 && CharSequence.compare(time,lastDate)!=1)
                series.getData().add(new XYChart.Data(time, value));
        });

        lineChart.getData().add(series);

        Measurement maxMeasurement = monitor.getMaxMeasurement(parameter, currentInstallation);
        System.out.println("Max mesurement: "+maxMeasurement.getValue());
        if (parameter.hasStandard())
            System.out.println("Standard: "+parameter.getStandard());

        System.out.println();
        if (parameter.hasStandard() && monitor.getMaxMeasurement(parameter, currentInstallation).getValue()>=parameter.getStandard()) {
            XYChart.Series standard = new XYChart.Series();
            Double standardValue = parameter.getStandard();
            standard.setName("STANDARD");
            standard.getData().add(new XYChart.Data(dates.get(0), standardValue));
            standard.getData().add(new XYChart.Data(dates.get(dates.size()-1), standardValue));
            lineChart.getData().add(standard);
        }

    }

}
