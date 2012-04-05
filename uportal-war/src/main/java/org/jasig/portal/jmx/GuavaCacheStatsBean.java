package org.jasig.portal.jmx;

import org.jasig.portal.character.stream.events.CharacterDataEventImpl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;

/**
 * Base bean to expose a {@link CacheStats} object
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class GuavaCacheStatsBean {
    private volatile Cache<?, ?> cache = null;
    private volatile CacheStats cacheStats = null;
    private volatile long size;
    private volatile long nextLoad;
    private volatile long loadIterval = 100;

    protected abstract Cache<?, ?> getCache();

    private CacheStats getCachedCacheStats() {
        if (cache == null || nextLoad <= System.currentTimeMillis()) {
            cache = getCache();
            cacheStats = cache.stats();
            size = cache.size();
            nextLoad = System.currentTimeMillis() + loadIterval;
        }
        
        return cacheStats;
    }

    public final long getLoadIterval() {
        return loadIterval;
    }

    /**
     * Number of milliseconds between calls to {@link CharacterDataEventImpl#getEventCacheStats()}
     */
    public final void setLoadIterval(long loadIterval) {
        this.loadIterval = loadIterval;
    }
    
    public void cleanUp() {
        getCachedCacheStats();
        cache.cleanUp();
    }

    public final long getSize() {
        getCachedCacheStats();
        return size;
    }

    public final long getRequestCount() {
        return getCachedCacheStats().requestCount();
    }

    public final long getHitCount() {
        return getCachedCacheStats().hitCount();
    }

    public final double getHitRate() {
        return getCachedCacheStats().hitRate();
    }

    public final long getMissCount() {
        return getCachedCacheStats().missCount();
    }

    public final double getMissRate() {
        return getCachedCacheStats().missRate();
    }

    public final long getLoadCount() {
        return getCachedCacheStats().loadCount();
    }

    public final long getLoadSuccessCount() {
        return getCachedCacheStats().loadSuccessCount();
    }

    public final long getLoadExceptionCount() {
        return getCachedCacheStats().loadExceptionCount();
    }

    public final double getLoadExceptionRate() {
        return getCachedCacheStats().loadExceptionRate();
    }

    public final long getTotalLoadTime() {
        return getCachedCacheStats().totalLoadTime();
    }

    public final double getAverageLoadPenalty() {
        return getCachedCacheStats().averageLoadPenalty();
    }

    public final long getEvictionCount() {
        return getCachedCacheStats().evictionCount();
    }

    @Override
    public String toString() {
        return getCachedCacheStats().toString();
    }
}