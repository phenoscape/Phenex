package org.phenoscape.io;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

/**
 * A URLProxy can be used to maintain a local cache of URL resources. For HTTP 
 * URLs, it uses the "Last-Modified" and "ETag" headers to determine if the file 
 * needs to be re-downloaded.
 * @author Jim Balhoff
 */
public class URLProxy {
  
  private final File cacheFolder;
  private static final String METADATA_FILENAME = ".proxy_metadata";
  public enum CacheOption {
    FORCE_CACHE,
    USE_CACHE,
    NO_CACHE;
  }

  /**
   * Create a URLProxy which uses the given folder as its cache location.
   */
  public URLProxy(File cacheLocation) {
    if (!cacheLocation.exists()) {
      cacheLocation.mkdirs();
    }
    if (!cacheLocation.isDirectory()) {
      throw new IllegalArgumentException("Cache location must be a directory.");
    }
    this.cacheFolder = cacheLocation;
  }
  
  /**
   * Returns true if the given URL is either not cached, or is cached and 
   * the cached data is out of date.
   */
  public boolean isOutOfDate(URL url) throws IOException {
    if (!this.isCached(url)) { return true; }
    final URLConnection connection = url.openConnection();
    final long lastModifiedRaw = connection.getLastModified();
    final Date cacheDate = this.getCacheDate(connection.getURL());
    if ((lastModifiedRaw > 0) && (cacheDate != null)) {
      final Date lastModified = new Date(lastModifiedRaw);
      return lastModified.after(cacheDate);
    }
    final String eTag = connection.getHeaderField("ETag");
    final String cacheETag = this.getCacheETag(connection.getURL());
    if ((eTag != null) && (cacheETag != null)) {
      return !eTag.equals(cacheETag);
    }
    return false;
  }
  
  /**
   * Returns a local file containing the data at the given URL. 
   * A cached version will be used if it is not out of date. This calls
   * get(URL url, CacheOption option) with CacheOption.USE_CACHE.
   */
  public File get(URL url) throws IOException {
    return this.get(url, CacheOption.USE_CACHE);
  }
  
  /**
   * Returns a local file containing the data at the given URL.  The data will 
   * be downloaded depending on the passed CacheOption. NO_CACHE will force a new 
   * version to be downloaded. USE_CACHE will download the file only if the cache 
   * is out of date.  FORCE_CACHE will use the cached version even if it is out of 
   * date.
   */
  public File get(URL url, CacheOption option) throws IOException {
    if ((option.equals(CacheOption.NO_CACHE)) || ((option.equals(CacheOption.USE_CACHE)) && (this.isOutOfDate(url)))) {
      log().info("Need to download from web: " + url);
      this.downloadToCache(url);
    }
    return this.getCacheFile(url);
  }
  
  /**
   * Returns true if a cached version of the given URL is available. 
   */
  public boolean isCached(URL url) {
    return this.getCacheFile(url).exists();
  }
  
  /**
   * Request the URLProxy to download and cache the given URL, regardless
   * of its current cached status.
   */
  public void downloadToCache(URL url) throws IOException {
    final URLConnection connection = url.openConnection();
    if (connection instanceof HttpURLConnection) {
      this.setCacheETag(url, connection.getHeaderField("ETag"));
      this.setCacheDate(url, new Date(connection.getDate()));
    } else {
      this.setCacheDate(url, new Date(connection.getLastModified()));
    }
    final InputStream input = connection.getInputStream();
    final ReadableByteChannel readChannel = Channels.newChannel(input);
    final FileChannel writeChannel = (new FileOutputStream(this.getCacheFile(url))).getChannel();
    final long BIG_NUM = 99999999999999l;
    writeChannel.transferFrom(readChannel, 0, BIG_NUM);
    writeChannel.close();
  }
  
  private File getCacheFile(URL url) {
    return new File(this.cacheFolder, this.getCacheFileName(url));
  }
  
  private String getCacheFileName(URL url) {
    return (String)this.getCacheMetadata(url).get("uuid");
  }
  
  private File getCacheMetadataFile() throws IOException {
    final File file = new File(this.cacheFolder, METADATA_FILENAME);
    if (!file.exists()) { 
      final FileWriter writer = new FileWriter(file);
      writer.write("");
      writer.close();
    }
    return file;
  }
  
  @SuppressWarnings("unchecked")
  private Map<String, Object> getCacheMetadata() {
    try {
      final ObjectInputStream input = new ObjectInputStream(new FileInputStream(this.getCacheMetadataFile()));
      return (Map<String, Object>)(input.readObject());
    } catch (EOFException e){
      // haven't serialized a map before
      return new HashMap<String, Object>();
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }
  
  @SuppressWarnings("unchecked")
  private Map<String, Object> getCacheMetadata(URL url) {
    final Map<String, Object> metadata = this.getCacheMetadata();
    if (!metadata.containsKey(url.toString())) {
      this.writeCacheMetadata(url, "uuid", UUID.randomUUID().toString());
    }
    return (Map<String, Object>)this.getCacheMetadata().get(url.toString());    
  }
  
  /**
   * All edits to the metadata dictionaries should be done through this method, 
   * which handles persistence.  After editing the metadata, it should be refetched 
   * using getCacheMetadata.
   */
  @SuppressWarnings("unchecked")
  private void writeCacheMetadata(URL url, String key, Object value) {
    try {
      final Map<String, Object> metadata = this.getCacheMetadata();
      final String urlKey = url.toString();
      final Map<String, Object> urlMetadata;
      if (metadata.containsKey(urlKey)) {
       urlMetadata = (Map<String, Object>)(metadata.get(urlKey));
      } else {
        urlMetadata =  new HashMap<String, Object>();
        metadata.put(urlKey, urlMetadata);
      }
      urlMetadata.put(key, value);
      final ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(this.getCacheMetadataFile()));
      output.writeObject(metadata);
      output.close();
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  private Date getCacheDate(URL url) {
    return (Date)(this.getCacheMetadata(url).get("date"));
  }
  
  private void setCacheDate(URL url, Date date) {
    this.writeCacheMetadata(url, "date", date);
  }
  
  private String getCacheETag(URL url) {
    return (String)(this.getCacheMetadata(url).get("etag"));
  }
  
  private void setCacheETag(URL url, String eTag) {
    this.writeCacheMetadata(url, "etag", eTag);
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

}
