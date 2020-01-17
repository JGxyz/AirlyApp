package pl.edu.agh.airly.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import pl.edu.agh.airly.model.Monitor;

import java.io.IOException;


public class AppController {

    private Stage primaryStage;
    private Monitor monitor;

    public AppController(Stage primaryStage, Monitor monitor) {
        this.primaryStage = primaryStage;
        this.monitor = monitor;
    }

    public AppController() {}

    public void initRootLayout() {
        showView("/view/MainView.fxml");
    }

    public void showView(String viewName) {
        try {
            this.primaryStage.setTitle("AirlyApp");

            // load layout from FXML file
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource(viewName));
            AnchorPane rootLayout = (AnchorPane) loader.load();

            //set initial data into pl.edu.agh.airly.controller
            BasicStatisticsController controller = loader.getController();
            controller.setAppController(this);
            controller.setData(monitor);

            // add layout to a scene and show them all
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            // don't do this in common apps
            e.printStackTrace();
        }

    }

    public void closeView() {
        primaryStage.close();
    }

    /*public boolean showTransactionEditDialog(Transaction transaction) {
        try {
            // Load the fxml file and create a new stage for the dialog
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(pl.edu.agh.airly.Main.class.getResource("view/TransactionEditDialog.fxml"));
            BorderPane page = (BorderPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit transaction");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the transaction into the presenter.
            TransactionEditDialogPresenter presenter = loader.getController();
            presenter.setDialogStage(dialogStage);
            presenter.setData(transaction);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();
            return presenter.isApproved();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
     */
}
