package net.marvk.fs.vatsim.map.repository;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.map.data.DataViewModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class SimpleRepository<Model, ViewModel extends DataViewModel<Model, ViewModel>> implements Repository<ViewModel> {
    protected final ObservableList<ViewModel> list;
    protected final ObservableMap<String, ViewModel> map;
    protected final ObservableList<ViewModel> unmodifiableList;
    protected final VatsimApi vatsimApi;

    public SimpleRepository(final VatsimApi vatsimApi) {
        this.vatsimApi = vatsimApi;

        this.list = FXCollections.observableArrayList();
        this.map = FXCollections.observableHashMap();
        this.unmodifiableList = FXCollections.unmodifiableObservableList(this.list);
    }

    protected abstract ViewModel create(final Model model);

    protected abstract String extractKey(final Model model);

    protected abstract Collection<Model> extractModelList(final VatsimApi api) throws VatsimApiException;

    @Override
    public ViewModel getByKey(final String key) {
        return map.get(key);
    }

    @Override
    public ObservableList<ViewModel> list() {
        return unmodifiableList;
    }

    @Override
    public void reload() throws RepositoryException {
        try {
            final Collection<Model> models = extractModelList(vatsimApi);

            final Set<String> keysInUpdate = models
                    .stream()
                    .map(this::extractKey)
                    .collect(Collectors.toSet());

            list.removeIf(e -> keysInUpdate.contains(extractKey(e.getModel())));
            map.keySet().removeIf(keysInUpdate::contains);

            final ArrayList<ViewModel> toAdd = new ArrayList<>();

            for (final Model model : models) {
                final String key = extractKey(model);
                final ViewModel viewModel = map.get(key);
                if (viewModel == null) {
                    final ViewModel newViewModel = create(model);
                    map.put(key, newViewModel);
                    toAdd.add(newViewModel);
                } else {
                    viewModel.setModel(model);
                }
            }

            list.addAll(toAdd);
        } catch (final VatsimApiException e) {
            throw new RepositoryException(e);
        }
    }
}
