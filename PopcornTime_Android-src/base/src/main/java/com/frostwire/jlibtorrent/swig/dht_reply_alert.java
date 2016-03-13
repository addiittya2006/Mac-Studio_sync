/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.frostwire.jlibtorrent.swig;

public class dht_reply_alert extends tracker_alert {
  private long swigCPtr;

  protected dht_reply_alert(long cPtr, boolean cMemoryOwn) {
    super(libtorrent_jni.dht_reply_alert_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(dht_reply_alert obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        libtorrent_jni.delete_dht_reply_alert(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public dht_reply_alert(torrent_handle h, int np) {
    this(libtorrent_jni.new_dht_reply_alert(torrent_handle.getCPtr(h), h, np), true);
  }

  public int type() {
    return libtorrent_jni.dht_reply_alert_type(swigCPtr, this);
  }

  public int category() {
    return libtorrent_jni.dht_reply_alert_category(swigCPtr, this);
  }

  public String what() {
    return libtorrent_jni.dht_reply_alert_what(swigCPtr, this);
  }

  public String message() {
    return libtorrent_jni.dht_reply_alert_message(swigCPtr, this);
  }

  public void setNum_peers(int value) {
    libtorrent_jni.dht_reply_alert_num_peers_set(swigCPtr, this, value);
  }

  public int getNum_peers() {
    return libtorrent_jni.dht_reply_alert_num_peers_get(swigCPtr, this);
  }

  public final static int alert_type = libtorrent_jni.dht_reply_alert_alert_type_get();
}
