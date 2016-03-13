package se.popcorn_time.base.providers;


public abstract class BaseProvider<Params, Path, Data, Source> implements Provider<Params, Path, Data, Source> {

    protected Params[] params;

    public Path getPath() {
        return createPath(params);
    }
}