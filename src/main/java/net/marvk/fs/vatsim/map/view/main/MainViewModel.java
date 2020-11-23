package net.marvk.fs.vatsim.map.view.main;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.CompositeCommand;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import net.marvk.fs.vatsim.map.repository.*;

public class MainViewModel implements ViewModel {
    private final Command loadAirports;
    private final Command loadFirs;
    private final Command loadFirbs;
    private final Command loadClients;

    @Inject
    public MainViewModel(
            final AirportRepository airportRepository,
            final ClientRepository clientRepository,
            final FlightInformationRegionRepository flightInformationRegionRepository,
            final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository
    ) {
        loadAirports = new ReloadRepositoryAction(airportRepository).asCommand();
        loadFirs = new ReloadRepositoryAction(flightInformationRegionRepository).asCommand();
        loadFirbs = new ReloadRepositoryAction(flightInformationRegionBoundaryRepository).asCommand();
        loadClients = new ReloadRepositoryAction(clientRepository).asCommand();

        new CompositeCommand(
                loadFirs,
                loadFirbs,
                loadAirports,
                loadClients
        ).execute();
    }

    private static final class ReloadRepositoryAction extends Action {
        private final Repository<?> repository;

        private ReloadRepositoryAction(final Repository<?> repository) {
            this.repository = repository;
        }

        @Override
        protected void action() throws RepositoryException {
            repository.reload();
        }

        private DelegateCommand asCommand() {
            return new DelegateCommand(() -> this, false);
        }
    }
}
