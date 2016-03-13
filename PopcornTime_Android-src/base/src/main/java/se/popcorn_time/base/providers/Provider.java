package se.popcorn_time.base.providers;

public interface Provider<Params, Path, Data, Source> {

    Path createPath(Params[] params);

    Data create();

    void populate(Data data, Source source);
}