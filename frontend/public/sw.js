const CACHE_PREFIX = 'reday-offline-'
const CACHE_NAME = CACHE_PREFIX + 'v1'
const OFFLINE_PAGE = '/offline.html'

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches
      .open(CACHE_NAME)
      .then((cache) => cache.add(OFFLINE_PAGE))
      .then(() => self.skipWaiting()),
  )
})

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches
      .keys()
      .then((names) =>
        Promise.all(
          names
            .filter(
              (name) => name.startsWith(CACHE_PREFIX) && name !== CACHE_NAME,
            )
            .map((name) => caches.delete(name)),
        ),
      )
      .then(() => self.clients.claim()),
  )
})

self.addEventListener('fetch', (event) => {
  const { request } = event
  const url = new URL(request.url)
  if (
    request.method !== 'GET' ||
    request.mode !== 'navigate' ||
    url.origin !== self.location.origin ||
    url.pathname.startsWith('/api/')
  ) {
    return
  }

  event.respondWith(
    fetch(request).catch(async () => {
      const offlinePage = await caches.match(OFFLINE_PAGE, {
        cacheName: CACHE_NAME,
      })
      return offlinePage ?? Response.error()
    }),
  )
})
