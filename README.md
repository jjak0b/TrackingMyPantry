# TrackingMyPantry
"Tracking My Pantry" is a mobile application for Android devices designed for those who wish to track the groceries they buy, starting from their own barcode, with the objective of creating a collaborative barcode database that can be used by the community.
The application uses the products registered by the user, but also interfaces with a single remote knowledge base, accessible as a web service, consisting of the products added by the community which are updated and accessed by the application.

## Features:
- Tracking user preferences and update a Remote knowledge base
- Tracking user groceries added on the device and purchases details
- Organize groceries in virtual pantries and custom tag categories
- Browse and search or filter local products
- Show or edit product details
- Automatically Add reminders of products expirations on the user calendar
- Browse product purchases places on a map and shows its price history chart
- Multiple Locations (english, italian)

## Project setup

1. Project needs a Mapbox Access token to work, so [get the public and secret access token here](https://docs.mapbox.com/help/getting-started/access-tokens/).
2. Create a file named `secrets.properties` and insert the public access token, for example you should add and update the following line:
   ``` MAPBOX_ACCESS_TOKEN=InsertHereYourMAPBOXAPIAccessToken```
3. Add your Mapbox private download api token to your `gradle.properties`, changing the value of the variable:
   ```MAPBOX_DOWNLOADS_TOKEN=InsertHereYourMAPBOXDownloadSDKToken```

## Other credits

- [©Mapbox](https://www.mapbox.com/about/maps/)
- [©OpenStreetMap](http://www.openstreetmap.org/about/)
- [Pantry icon vector created by freepik - www.freepik.com](https://www.freepik.com/vectors/food)