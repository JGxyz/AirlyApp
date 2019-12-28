package pl.edu.agh.airly.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import org.apache.spark.api.java.JavaSparkContext;
import pl.edu.agh.airly.model.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GeneralStatisticsController {
    private AppController appController;
    private Monitor monitor;
    private Installation currentInstallation;

    @FXML
    private ComboBox<City> cityComboBox;

    @FXML
    private ComboBox<Installation> installationComboBox;

    @FXML
    private ComboBox<Parameter> parameterComboBox;

    @FXML
    private LineChart<?, ?> lineChart;

    @FXML
    private CategoryAxis categoryAxis;

    @FXML
    private NumberAxis numberAxis;


    public void setData(JavaSparkContext sparkContext) {
        cityComboBox.setItems(FXCollections.observableList(City.getAll().stream().collect(Collectors.toList())));
        this.monitor = new Monitor(sparkContext);
        monitor.readInstallations();
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
                parameterChanged(newValue);
            }
        });
    }

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    private void cityChanged(City city) {
        if (city == null) return;
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
        } else {
            parameterComboBox.setItems(FXCollections.emptyObservableList());
        }
    }

    private void parameterChanged(Parameter parameter) {
        if (parameter == null) return;
        System.out.println("SELECTED PARAMETER: "+parameter);
        XYChart.Series[] allSeries = lineChart.getData().toArray(new XYChart.Series[0]);
        for (XYChart.Series series : allSeries)
            lineChart.getData().remove(series);
        List<Measurement> measurements = monitor.getParameterMeasurementsFromInstallation(parameter, currentInstallation);
        measurements.forEach(System.out::println);
        List<String> dates = measurements.stream().map(measurement -> measurement.getFromDateTime()).collect(Collectors.toList());
        categoryAxis.setCategories(FXCollections.observableList(dates));

        categoryAxis.setTickLabelRotation(-45);
        numberAxis.setTickLabelRotation(-45);

        XYChart.Series series = new XYChart.Series();

        series.setName(parameter.getName());

       measurements.forEach(measurement -> {
            Double value = measurement.getValue();
            String time = measurement.getFromDateTime();
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
