/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.frostwire.jlibtorrent.swig;

public class cache_status {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected cache_status(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(cache_status obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        libtorrent_jni.delete_cache_status(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public cache_status() {
    this(libtorrent_jni.new_cache_status(), true);
  }

  public void setBlocks_written(long value) {
    libtorrent_jni.cache_status_blocks_written_set(swigCPtr, this, value);
  }

  public long getBlocks_written() {
    return libtorrent_jni.cache_status_blocks_written_get(swigCPtr, this);
  }

  public void setWrites(long value) {
    libtorrent_jni.cache_status_writes_set(swigCPtr, this, value);
  }

  public long getWrites() {
    return libtorrent_jni.cache_status_writes_get(swigCPtr, this);
  }

  public void setBlocks_read(long value) {
    libtorrent_jni.cache_status_blocks_read_set(swigCPtr, this, value);
  }

  public long getBlocks_read() {
    return libtorrent_jni.cache_status_blocks_read_get(swigCPtr, this);
  }

  public void setBlocks_read_hit(long value) {
    libtorrent_jni.cache_status_blocks_read_hit_set(swigCPtr, this, value);
  }

  public long getBlocks_read_hit() {
    return libtorrent_jni.cache_status_blocks_read_hit_get(swigCPtr, this);
  }

  public void setReads(long value) {
    libtorrent_jni.cache_status_reads_set(swigCPtr, this, value);
  }

  public long getReads() {
    return libtorrent_jni.cache_status_reads_get(swigCPtr, this);
  }

  public void setQueued_bytes(long value) {
    libtorrent_jni.cache_status_queued_bytes_set(swigCPtr, this, value);
  }

  public long getQueued_bytes() {
    return libtorrent_jni.cache_status_queued_bytes_get(swigCPtr, this);
  }

  public void setCache_size(int value) {
    libtorrent_jni.cache_status_cache_size_set(swigCPtr, this, value);
  }

  public int getCache_size() {
    return libtorrent_jni.cache_status_cache_size_get(swigCPtr, this);
  }

  public void setRead_cache_size(int value) {
    libtorrent_jni.cache_status_read_cache_size_set(swigCPtr, this, value);
  }

  public int getRead_cache_size() {
    return libtorrent_jni.cache_status_read_cache_size_get(swigCPtr, this);
  }

  public void setTotal_used_buffers(int value) {
    libtorrent_jni.cache_status_total_used_buffers_set(swigCPtr, this, value);
  }

  public int getTotal_used_buffers() {
    return libtorrent_jni.cache_status_total_used_buffers_get(swigCPtr, this);
  }

  public void setAverage_queue_time(int value) {
    libtorrent_jni.cache_status_average_queue_time_set(swigCPtr, this, value);
  }

  public int getAverage_queue_time() {
    return libtorrent_jni.cache_status_average_queue_time_get(swigCPtr, this);
  }

  public void setAverage_read_time(int value) {
    libtorrent_jni.cache_status_average_read_time_set(swigCPtr, this, value);
  }

  public int getAverage_read_time() {
    return libtorrent_jni.cache_status_average_read_time_get(swigCPtr, this);
  }

  public void setAverage_write_time(int value) {
    libtorrent_jni.cache_status_average_write_time_set(swigCPtr, this, value);
  }

  public int getAverage_write_time() {
    return libtorrent_jni.cache_status_average_write_time_get(swigCPtr, this);
  }

  public void setAverage_hash_time(int value) {
    libtorrent_jni.cache_status_average_hash_time_set(swigCPtr, this, value);
  }

  public int getAverage_hash_time() {
    return libtorrent_jni.cache_status_average_hash_time_get(swigCPtr, this);
  }

  public void setAverage_job_time(int value) {
    libtorrent_jni.cache_status_average_job_time_set(swigCPtr, this, value);
  }

  public int getAverage_job_time() {
    return libtorrent_jni.cache_status_average_job_time_get(swigCPtr, this);
  }

  public void setAverage_sort_time(int value) {
    libtorrent_jni.cache_status_average_sort_time_set(swigCPtr, this, value);
  }

  public int getAverage_sort_time() {
    return libtorrent_jni.cache_status_average_sort_time_get(swigCPtr, this);
  }

  public void setJob_queue_length(int value) {
    libtorrent_jni.cache_status_job_queue_length_set(swigCPtr, this, value);
  }

  public int getJob_queue_length() {
    return libtorrent_jni.cache_status_job_queue_length_get(swigCPtr, this);
  }

  public void setCumulative_job_time(long value) {
    libtorrent_jni.cache_status_cumulative_job_time_set(swigCPtr, this, value);
  }

  public long getCumulative_job_time() {
    return libtorrent_jni.cache_status_cumulative_job_time_get(swigCPtr, this);
  }

  public void setCumulative_read_time(long value) {
    libtorrent_jni.cache_status_cumulative_read_time_set(swigCPtr, this, value);
  }

  public long getCumulative_read_time() {
    return libtorrent_jni.cache_status_cumulative_read_time_get(swigCPtr, this);
  }

  public void setCumulative_write_time(long value) {
    libtorrent_jni.cache_status_cumulative_write_time_set(swigCPtr, this, value);
  }

  public long getCumulative_write_time() {
    return libtorrent_jni.cache_status_cumulative_write_time_get(swigCPtr, this);
  }

  public void setCumulative_hash_time(long value) {
    libtorrent_jni.cache_status_cumulative_hash_time_set(swigCPtr, this, value);
  }

  public long getCumulative_hash_time() {
    return libtorrent_jni.cache_status_cumulative_hash_time_get(swigCPtr, this);
  }

  public void setCumulative_sort_time(long value) {
    libtorrent_jni.cache_status_cumulative_sort_time_set(swigCPtr, this, value);
  }

  public long getCumulative_sort_time() {
    return libtorrent_jni.cache_status_cumulative_sort_time_get(swigCPtr, this);
  }

  public void setTotal_read_back(int value) {
    libtorrent_jni.cache_status_total_read_back_set(swigCPtr, this, value);
  }

  public int getTotal_read_back() {
    return libtorrent_jni.cache_status_total_read_back_get(swigCPtr, this);
  }

  public void setRead_queue_size(int value) {
    libtorrent_jni.cache_status_read_queue_size_set(swigCPtr, this, value);
  }

  public int getRead_queue_size() {
    return libtorrent_jni.cache_status_read_queue_size_get(swigCPtr, this);
  }

}
