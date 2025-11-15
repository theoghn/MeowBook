# MeowBook

A simple cat browser app powered by the [TheCatAPI](https://thecatapi.com/). Browse random cat images, filter them by breed, and view detailed information about each one.

### Cat Listing
- The screen has filters for :
	- Order by date or random
	- Show only cats that contain breed info
	- Filter by specific breeds
-  Proper UI states for:
	- Images failing to load
	- API errors
	- No internet connection
-  Image shimmer placeholder while loading
-  Offline caching for the first fetched page
-  Pull-to-refresh support

### Cat Details
- Displays detailed information about each breed
- Includes two external links (Wikipedia and Vetstreet)
- Handles API error and offline states
