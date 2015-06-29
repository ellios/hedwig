package me.ellios.jedis.transcoders;

// Copyright (c) 2006  Dustin Sallings <dustin@spy.net>


/**
 * Transcoder is an interface for classes that convert between byte arrays and
 * objects for storage in the cache.
 */
public interface Transcoder {

	/**
	 * Encode the given object for storage.
	 * 
	 * @param o
	 *            the object
	 * @return the CachedData representing what should be sent
	 */
    <T> CachedData encode(T o);

	/**
	 * Decode the cached object into the object it represents.
	 * 
	 * @param d
	 *            the data
	 * @return the return value
	 */
    <T> T decode(CachedData d);

	/**
	 * Set whether store primitive type as string.
	 * 
	 * @param primitiveAsString
	 */
	public void setPrimitiveAsString(boolean primitiveAsString);

	
	public void setCompressionThreshold(int to);

	public boolean isPrimitiveAsString();
}
