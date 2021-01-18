package net.marvk.fs.vatsim.map.view.about;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import net.marvk.fs.vatsim.map.data.Dependency;
import net.marvk.fs.vatsim.map.view.ListNoneSelectionModel;

import java.time.LocalDateTime;

public class AboutView implements FxmlView<AboutViewModel> {
    @FXML
    private Label version;

    @FXML
    private ListView<Dependency> dependenciesList;

    @FXML
    private Label createdBy;

    @InjectViewModel
    private AboutViewModel viewModel;

    public void initialize() {
        setVersion();

        dependenciesList.setItems(viewModel.dependencies());
        dependenciesList.setCellFactory(param -> new DependencyListCell());
        dependenciesList.setSelectionModel(new ListNoneSelectionModel<>());
        dependenciesList.setFocusTraversable(false);
        version.requestFocus();
        createdBy.setText("©2020-%s Marvin Kuhnke".formatted(LocalDateTime.now().getYear()));
    }

    private void setVersion() {
        final String versionString = viewModel.getVersion();
        if (versionString != null) {
            this.version.setText(versionString);
        }
    }

    @FXML
    private void openIssuePage() {
        viewModel.openIssuePage();
    }

    private class DependencyListCell extends ListCell<Dependency> {
        @Override
        protected void updateItem(final Dependency item, final boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setGraphic(getPane(item));
            }
        }

        private Pane pane;

        private Label name;
        private Label version;
        private Label license;

        private Pane getPane(final Dependency item) {
            if (pane == null) {
                name = new Label();
                version = new Label();
                license = new Label();

                final HBox hBox = new HBox(name, version);
                hBox.setSpacing(5);
                pane = new VBox(hBox, license);
            }

            name.setText(item.getProjectName());
            final String url = item.getProjectUrl();
            if (url != null && !url.isBlank()) {
                if (!name.getStyleClass().contains("hl")) {
                    name.getStyleClass().add("hl");
                }
                name.setOnMouseClicked(e -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        viewModel.openDependencyUrlInBrowser(item);
                        e.consume();
                    }
                });
            } else {
                name.getStyleClass().remove("hl");
                name.setOnMouseClicked(null);
            }
            version.setText(item.getVersion());
            license.setText(item.getLicenseName());

            return pane;
        }
    }

}
