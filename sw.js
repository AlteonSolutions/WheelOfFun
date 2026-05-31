// Wheel of Fun — service worker.
// Caches the entire app on first visit so the Kindle Fire can launch it
// fully offline thereafter. Bumping CACHE_VERSION invalidates old caches.
const CACHE_VERSION = 'wof-v1';
const CORE_ASSETS = [
  './',
  './index.html',
  './manifest.webmanifest',
  './icon-192.png',
  './icon-512.png',
  './icon-maskable-512.png'
];

self.addEventListener('install', (event) => {
  // Pre-cache everything; the index.html itself contains the inlined MP3,
  // so no external audio file needs to be fetched separately.
  event.waitUntil(
    caches.open(CACHE_VERSION).then((cache) => cache.addAll(CORE_ASSETS))
  );
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  // Drop any older cache versions when this SW takes control.
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(keys.filter((k) => k !== CACHE_VERSION).map((k) => caches.delete(k)))
    ).then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (event) => {
  // Cache-first for same-origin GETs. Network requests are still allowed for
  // anything not in cache (e.g. dev iteration), but production use is offline.
  if (event.request.method !== 'GET') return;
  event.respondWith(
    caches.match(event.request).then((cached) => {
      if (cached) return cached;
      return fetch(event.request).then((response) => {
        // Opportunistically cache successful same-origin responses for next time.
        if (response && response.ok && new URL(event.request.url).origin === self.location.origin) {
          const copy = response.clone();
          caches.open(CACHE_VERSION).then((c) => c.put(event.request, copy));
        }
        return response;
      }).catch(() => cached); // offline + uncached → swallow
    })
  );
});
