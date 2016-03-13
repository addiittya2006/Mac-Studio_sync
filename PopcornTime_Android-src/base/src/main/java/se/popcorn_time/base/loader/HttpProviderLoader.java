package se.popcorn_time.base.loader;

import android.content.Context;
import android.text.TextUtils;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import se.popcorn_time.base.providers.HttpProvider;
import se.popcorn_time.base.utils.Logger;

public class HttpProviderLoader<Result, Provider extends HttpProvider<Result>> extends BaseLoader<Result> {

    private List<Provider> providers;
    private Result result;
    private Call call;

    public HttpProviderLoader(Context context, Provider provider) {
        this(context, provider, null);
    }

    public HttpProviderLoader(Context context, Provider provider, Result result) {
        super(context);
        if (provider != null) {
            this.providers = new ArrayList<>();
            this.providers.add(provider);
        }
        this.result = result;
    }

    public HttpProviderLoader(Context context, List<Provider> providers) {
        this(context, providers, null);
    }

    public HttpProviderLoader(Context context, List<Provider> providers, Result result) {
        super(context);
        this.providers = providers;
        this.result = result;
    }

    @Override
    public Result loadInBackground() {
        if (providers == null || providers.size() == 0) {
            Logger.error("HttpProviderLoader<load>: Not have providers");
            return null;
        }

        OkHttpClient httpClient = new OkHttpClient();
        httpClient.setConnectTimeout(15, TimeUnit.SECONDS);

        for (HttpProvider<Result> provider : providers) {
            if (result == null) {
                result = provider.create();
            }
            String url = provider.getPath();
            Logger.debug("HttpProviderLoader<load>: " + url);
            if (!TextUtils.isEmpty(url)) {
                call = httpClient.newCall(new Request.Builder().url(url).build());
                try {
                    Response response = call.execute();
                    if (response.isSuccessful()) {
                        provider.populate(result, response.body().string());
                    }
                } catch (IOException e) {
                    Logger.error("HttpProviderLoader<load>: Error", e);
                }
            }
        }

        return result;
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        if (call != null) {
            call.cancel();
        }
    }
}