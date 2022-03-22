package net.marvk.fs.vatsim.map.view.datadetail.airportdetail;

import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import net.marvk.fs.vatsim.map.data.Airport;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.discord.DiscordClient;
import net.marvk.fs.vatsim.map.view.BindingsUtil;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailPane;
import net.marvk.fs.vatsim.map.view.datadetail.controllersdetail.ControllersDetailView;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DataDetailSubView;
import net.marvk.fs.vatsim.map.view.datadetail.metardetail.MetarDetailView;
import net.marvk.fs.vatsim.map.view.datadetail.trafficdetail.TrafficDetailView;

import java.util.List;

public class AirportDetailView extends DataDetailSubView<AirportDetailViewModel, Airport> {
    private static final String HYPERLINK_LABEL = "hyperlink-label";

    @FXML
    private Label country;

    @FXML
    private DataDetailPane status;
    @FXML
    private Label fir;
    @FXML
    private Label position;
    @FXML
    private Label name;

    @FXML
    private ControllersDetailView controllersController;
    @FXML
    private MetarDetailView metarController;
    @FXML
    private TrafficDetailView arrivalsController;
    @FXML
    private TrafficDetailView departuresController;

    @Override
    public void initialize() {
        super.initialize();
        arrivalsController.getViewModel().setType(TrafficDetailView.Type.ARRIVAL);
        departuresController.getViewModel().setType(TrafficDetailView.Type.DEPARTURE);
    }

    @Override
    protected List<Label> labels() {
        return List.of(position, name, fir, country);
    }

    @Override
    protected List<StringProperty> stringProperties() {
        return List.of(status.headerTextProperty());
    }

    @Override
    protected void setData(final Airport airport) {
        name.setText(airport.getNames().get(0).get());
        position.textProperty().bind(BindingsUtil.position(airport.positionProperty()));
        status.setHeaderText(airport.getIcao());
        final FlightInformationRegionBoundary firb = airport.getFlightInformationRegionBoundary();

        final DiscordClient discordClient = DependencyInjector.getInstance().getInstanceOf(DiscordClient.class);
        discordClient.updateActivity(name.getText(), "Viewing an airport");

        if (firb == null) {
            fir.getStyleClass().remove(HYPERLINK_LABEL);
            fir.setText(null);
        } else {
            fir.getStyleClass().add(HYPERLINK_LABEL);
            fir.setText(firb.getIcao());
        }

        if (airport.getCountry() == null) {
            country.setText("UNKNOWN");
        } else {
            country.setText(airport.getCountry().getName());
        }

        controllersController.getViewModel().setData(airport.getControllers());
        arrivalsController.getViewModel().setData(airport.getArriving());
        departuresController.getViewModel().setData(airport.getDeparting());
        metarController.getViewModel().setAirport(airport);
    }

    @Override
    protected void clear(final Airport oldValue) {
        super.clear(oldValue);
        metarController.getViewModel().setAirport(null);
        if (oldValue != null) {
            controllersController.getViewModel().setData(null);
        }
    }

    @FXML
    private void setToFir() {
        viewModel.setToFir();
    }
}
