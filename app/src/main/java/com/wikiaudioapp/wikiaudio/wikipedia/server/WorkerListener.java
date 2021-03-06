package com.wikiaudioapp.wikiaudio.wikipedia.server;

/**
 * a interface of a worker listener.
 */
public interface WorkerListener {

    /**
     * what to do if the worker is successful.
     */
    public void onSuccess();

    /**
     * what to do if worker fails.
     */
    public void onFailure();

}
